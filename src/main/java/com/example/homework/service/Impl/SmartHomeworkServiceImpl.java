package com.example.homework.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.homework.common.R;
import com.example.homework.common.UserContextHolder;
import com.example.homework.constant.AiConstants;
import com.example.homework.dto.SmartHomeworkDTO;
import com.example.homework.entity.Homework;
import com.example.homework.entity.HomeworkQuestion;
import com.example.homework.entity.Question;
import com.example.homework.mapper.CourseMapper;
import com.example.homework.mapper.HomeworkMapper;
import com.example.homework.mapper.HomeworkQuestionMapper;
import com.example.homework.mapper.QuestionMapper;
import com.example.homework.service.SmartHomeworkService;
import com.example.homework.utils.PermissionUtil;
import com.example.homework.vo.CourseVo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 智能发布作业业务层实现
 * 流程：题型数量分配 → AI 生成题目 → 分值精确配平 → 题库/作业/关联表落库
 */
@Service
public class SmartHomeworkServiceImpl implements SmartHomeworkService {

    private final ChatClient chatClient;

    @Autowired
    private HomeworkMapper homeworkMapper;
    @Autowired
    private HomeworkQuestionMapper homeworkQuestionMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private CourseMapper courseMapper;

    // 前端题型名 -> 系统题型（选择题统一按单选题生成，可自动判分）
    private static final Map<String, String> TYPE_MAPPING = Map.ofEntries(
            Map.entry("选择题", "单选"),
            Map.entry("单选题", "单选"),
            Map.entry("多选", "多选"),
            Map.entry("多选题", "多选"),
            Map.entry("判断题", "判断"),
            Map.entry("判断", "判断"),
            Map.entry("简答题", "简答"),
            Map.entry("简答", "简答")
    );

    // 各题型单题分值权重（简答题分值更高）
    private static final Map<String, Integer> TYPE_WEIGHT = Map.of(
            "单选", 1,
            "多选", 2,
            "判断", 1,
            "简答", 3
    );

    public SmartHomeworkServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> smartPublish(SmartHomeworkDTO dto) {
        // 1. 权限校验
        if (!PermissionUtil.isAdminOrTeacher()) {
            return R.error("权限不足！仅教师可发布作业");
        }

        // 2. 参数校验
        if (dto.getClazzId() == null || dto.getCourseId() == null || dto.getDeadline() == null
                || !StringUtils.hasText(dto.getTitle()) || !StringUtils.hasText(dto.getContent())
                || dto.getQuestionCount() == null || dto.getQuestionCount() <= 0
                || dto.getTotalScore() == null || dto.getTotalScore() <= 0
                || dto.getTypePercent() == null || dto.getTypePercent().isEmpty()) {
            return R.error("作业信息不完整！班级、课程、标题、要求、题量、总分、题型占比、截止时间均不能为空");
        }

        // 3. 题型数量分配（最大余数法，保证题数精确）
        LinkedHashMap<String, Integer> requiredCount =
                distributeCount(dto.getTypePercent(), dto.getQuestionCount());
        if (requiredCount.isEmpty()) {
            return R.error("题型占比无法识别，请使用选择题、判断题、简答题");
        }

        // 4. 查询课程名 + 难度兜底
        String courseName = null;
        for (CourseVo c : courseMapper.listAllCourseName()) {
            if (c.getId().equals(dto.getCourseId())) {
                courseName = c.getCourseName();
                break;
            }
        }
        String difficulty = StringUtils.hasText(dto.getDifficulty()) ? dto.getDifficulty() : "中等";

        // 5. 优先从题库选题（严格匹配作业标题范围防超纲，难度精确匹配优先，随机抽取）
        List<Question> bankQuestions = selectFromBank(dto.getCourseId(), difficulty, requiredCount,
                dto.getTitle(), dto.getContent());

        // 6. 统计题库已满足的数量，计算各题型缺口
        Map<String, Integer> bankCountByType = new HashMap<>();
        for (Question q : bankQuestions) {
            bankCountByType.merge(q.getType(), 1, Integer::sum);
        }
        LinkedHashMap<String, Integer> missingCount = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : requiredCount.entrySet()) {
            int miss = e.getValue() - bankCountByType.getOrDefault(e.getKey(), 0);
            if (miss > 0) {
                missingCount.put(e.getKey(), miss);
            }
        }

        // 7. 题库不足的部分，调用 AI 生成补齐（解析或校验失败时重试一次）
        List<Question> aiQuestions = new ArrayList<>();
        if (!missingCount.isEmpty()) {
            int totalMissing = missingCount.values().stream().mapToInt(Integer::intValue).sum();
            String requirement = buildRequirementText(dto.getTitle(), courseName, difficulty,
                    missingCount, bankQuestions);

            for (int attempt = 1; attempt <= 2; attempt++) {
                String userMsg = requirement;
                if (attempt == 2) {
                    userMsg += "\n注意：你上一次的输出不符合要求。本次必须严格只输出JSON数组，"
                            + "题目总数必须是 " + totalMissing + " 道，题型数量与要求完全一致。";
                }
                String aiOutput = chatClient.prompt()
                        .system(AiConstants.SMART_QUESTION_PROMPT)
                        .user(userMsg)
                        .call()
                        .content();

                List<Question> parsed = parseQuestions(aiOutput);
                if (parsed != null && validateQuestions(parsed, missingCount)) {
                    aiQuestions = parsed;
                    break;
                }
            }
            if (aiQuestions.size() < totalMissing) {
                return R.error("题库题目不足，且AI补充出题失败，请稍后重试，或调整题量、题型比例");
            }
        }

        // 8. 合并题库题 + AI题，按题型顺序排列（单选→判断→简答）
        List<Question> questions = new ArrayList<>(bankQuestions);
        questions.addAll(aiQuestions);
        sortByType(questions, requiredCount);

        // 9. 分值精确配平（各题分数之和严格等于 totalScore）
        List<Integer> scores = distributeScores(questions, dto.getTotalScore());

        // 10. 保存作业主表
        Homework homework = new Homework();
        homework.setTitle(dto.getTitle());
        homework.setContent(dto.getContent());
        homework.setTotalScore(dto.getTotalScore());
        homework.setTeacherId(Long.valueOf(UserContextHolder.getUserId()));
        homework.setClazzId(dto.getClazzId());
        homework.setCourseId(dto.getCourseId());
        homework.setStartTime(LocalDateTime.now());
        homework.setDeadline(dto.getDeadline());
        if (homeworkMapper.insert(homework) <= 0) {
            return R.error("作业发布失败！");
        }

        // 11. 建立作业-题目关联：题库题已存在直接关联，AI题先写入题库再关联
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);

            if (q.getId() == null) {
                // AI 新生成的题：补全课程、分值、难度后写入题库
                q.setCourseId(dto.getCourseId());
                q.setScore(scores.get(i));
                q.setDifficulty(difficulty);
                questionMapper.insert(q);
            }

            HomeworkQuestion hq = new HomeworkQuestion();
            hq.setHomeworkId(homework.getId());
            hq.setQuestionId(q.getId());
            hq.setScore(scores.get(i));
            hq.setSort(i + 1);
            hq.setCreateTime(LocalDateTime.now());
            homeworkQuestionMapper.insert(hq);
        }

        return R.success("作业智能发布成功！共 " + questions.size() + " 道题（题库选取 "
                + bankQuestions.size() + " 道，AI生成 " + aiQuestions.size() + " 道），满分 "
                + dto.getTotalScore() + " 分");
    }

    /**
     * 从题库优先选题：
     * 限定同课程、同题型 → AI按作业标题筛选相关题目（防超纲）
     * → 难度精确匹配的题目优先，不足时用同题型其他难度补充 → 随机抽取
     */
    private List<Question> selectFromBank(Long courseId, String difficulty,
                                          LinkedHashMap<String, Integer> requiredCount,
                                          String homeworkTitle, String homeworkContent) {
        List<Question> bank = questionMapper.selectListByCourseId(courseId);

        // 只保留本次作业需要的题型
        List<Question> candidates = new ArrayList<>();
        for (Question q : bank) {
            if (q.getType() != null && requiredCount.containsKey(q.getType())) {
                candidates.add(q);
            }
        }

        // AI 按作业标题筛选相关题目，排除超纲题
        Set<Long> relevantIds = filterRelevantByAI(homeworkTitle, homeworkContent, candidates);
        candidates.removeIf(q -> !relevantIds.contains(q.getId()));

        // 按题型分组
        Map<String, List<Question>> candidatesByType = new HashMap<>();
        for (Question q : candidates) {
            candidatesByType.computeIfAbsent(q.getType(), k -> new ArrayList<>()).add(q);
        }

        List<Question> selected = new ArrayList<>();
        Random random = new Random();

        // 按题型需求顺序逐组抽题
        for (Map.Entry<String, Integer> e : requiredCount.entrySet()) {
            String type = e.getKey();
            int need = e.getValue();

            List<Question> exactDifficulty = new ArrayList<>();
            List<Question> otherDifficulty = new ArrayList<>();
            for (Question q : candidatesByType.getOrDefault(type, Collections.emptyList())) {
                if (difficulty.equals(q.getDifficulty())) {
                    exactDifficulty.add(q);
                } else {
                    otherDifficulty.add(q);
                }
            }
            Collections.shuffle(exactDifficulty, random);
            Collections.shuffle(otherDifficulty, random);

            int picked = 0;
            for (Question q : exactDifficulty) {
                if (picked >= need) {
                    break;
                }
                selected.add(q);
                picked++;
            }
            for (Question q : otherDifficulty) {
                if (picked >= need) {
                    break;
                }
                selected.add(q);
                picked++;
            }
        }
        return selected;
    }

    /**
     * AI 根据作业标题/要求，从候选题目中筛选知识点相关的题目ID。
     * 仅发送ID+题型+截断后的题干以控制token；
     * 调用或解析失败时降级保留全部候选，避免流程中断。
     */
    private Set<Long> filterRelevantByAI(String homeworkTitle, String homeworkContent,
                                         List<Question> candidates) {
        // 降级结果：全部候选ID
        Set<Long> fallback = new HashSet<>();
        for (Question q : candidates) {
            fallback.add(q.getId());
        }
        if (candidates.isEmpty()) {
            return fallback;
        }

        // 题目清单：限制数量与题干长度
        int limit = Math.min(candidates.size(), 150);
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            Question q = candidates.get(i);
            String t = q.getTitle();
            if (t != null && t.length() > 80) {
                t = t.substring(0, 80) + "...";
            }
            list.append(q.getId()).append(". [").append(q.getType()).append("] ").append(t).append("\n");
        }
        if (candidates.size() > limit) {
            list.append("（其余 ").append(candidates.size() - limit).append(" 道题省略，默认全部相关）\n");
        }

        try {
            String output = chatClient.prompt()
                    .system(AiConstants.RELEVANCE_FILTER_PROMPT)
                    .user("作业标题：" + homeworkTitle
                            + "\n作业要求：" + (StringUtils.hasText(homeworkContent) ? homeworkContent : "无")
                            + "\n题库候选题目：\n" + list
                            + "请输出相关题目ID的JSON整数数组。")
                    .call()
                    .content();

            int start = output.indexOf('[');
            int end = output.lastIndexOf(']');
            if (start < 0 || end <= start) {
                return fallback; // 模型未按格式输出，降级
            }

            Set<Long> relevant = new HashSet<>();
            JSONArray arr = JSONUtil.parseArray(output.substring(start, end + 1));
            for (Object o : arr) {
                relevant.add(Long.valueOf(String.valueOf(o)));
            }
            // 超出150道未参与筛选的题目默认保留
            for (int i = limit; i < candidates.size(); i++) {
                relevant.add(candidates.get(i).getId());
            }
            return relevant;
        } catch (Exception e) {
            return fallback; // AI调用异常，降级保留全部
        }
    }

    /**
     * 按 requiredCount 的题型顺序排序题目
     */
    private void sortByType(List<Question> questions, LinkedHashMap<String, Integer> requiredCount) {
        Map<String, Integer> typeOrder = new HashMap<>();
        int order = 0;
        for (String type : requiredCount.keySet()) {
            typeOrder.put(type, order++);
        }
        questions.sort(Comparator.comparingInt(q ->
                typeOrder.getOrDefault(q.getType(), Integer.MAX_VALUE)));
    }

    /**
     * 题型数量分配：最大余数法
     * 例：10题 × {选择题76%,判断题12%,简答题12%} → 单选8、判断1、简答1
     */
    private LinkedHashMap<String, Integer> distributeCount(Map<String, Integer> typePercent, int totalCount) {
        // 映射为系统题型并累加百分比
        LinkedHashMap<String, Integer> pct = new LinkedHashMap<>();
        int pctSum = 0;
        for (Map.Entry<String, Integer> e : typePercent.entrySet()) {
            String inner = TYPE_MAPPING.get(e.getKey().trim());
            Integer p = e.getValue();
            if (inner == null || p == null || p <= 0) {
                continue;
            }
            pct.merge(inner, p, Integer::sum);
            pctSum += p;
        }
        if (pct.isEmpty() || pctSum <= 0) {
            return new LinkedHashMap<>();
        }

        double[] exact = new double[pct.size()];
        int[] floorCount = new int[pct.size()];
        int floorSum = 0;
        int idx = 0;
        for (Integer p : pct.values()) {
            exact[idx] = (double) totalCount * p / pctSum;
            floorCount[idx] = (int) Math.floor(exact[idx]);
            floorSum += floorCount[idx];
            idx++;
        }

        // 剩余名额按小数余数从大到小补（同余数保持原顺序）
        int remaining = totalCount - floorSum;
        Integer[] order = new Integer[pct.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) ->
                Double.compare(exact[b] - floorCount[b], exact[a] - floorCount[a]));
        for (int k = 0; k < remaining; k++) {
            floorCount[order[k % order.length]]++;
        }

        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        idx = 0;
        for (String t : pct.keySet()) {
            if (floorCount[idx] > 0) {
                result.put(t, floorCount[idx]);
            }
            idx++;
        }
        return result;
    }

    /**
     * 构建 AI 出题需求文本
     */
    private String buildRequirementText(String homeworkTitle, String courseName, String difficulty,
                                        LinkedHashMap<String, Integer> requiredCount,
                                        List<Question> bankQuestions) {
        StringBuilder sb = new StringBuilder();
        // 作业标题 = 本次考查范围，置于最前作为硬约束
        sb.append("【作业标题（本次考查范围）】").append(homeworkTitle).append("\n");
        sb.append("课程：").append(courseName != null ? courseName : "未指定课程").append("\n");
        sb.append("题目整体难度：").append(difficulty).append("\n");
        sb.append("【超纲约束】新命制的每道题，考查知识点必须严格落在上述作业标题对应的知识范围内，"
                + "不得出现标题范围之外的任何知识点。\n");
        sb.append("以下题目已从题库中选出，你新命制的题目不得与它们内容或考点重复：\n");
        if (bankQuestions.isEmpty()) {
            sb.append("（暂无，题库中没有可用题目）\n");
        } else {
            for (Question q : bankQuestions) {
                sb.append("- [").append(q.getType()).append("] ").append(q.getTitle()).append("\n");
            }
        }
        sb.append("请严格按以下题型和数量补充命题，题型顺序保持一致：\n");
        for (Map.Entry<String, Integer> e : requiredCount.entrySet()) {
            sb.append("- ").append(e.getKey()).append("：").append(e.getValue()).append(" 道\n");
        }
        int total = requiredCount.values().stream().mapToInt(Integer::intValue).sum();
        sb.append("需要补充的题目总数：").append(total).append(" 道");
        return sb.toString();
    }

    /**
     * 解析 AI 输出为题目列表（容错：剥离 markdown 代码块和多余文字）
     */
    private List<Question> parseQuestions(String aiOutput) {
        if (aiOutput == null) {
            return null;
        }
        int start = aiOutput.indexOf('[');
        int end = aiOutput.lastIndexOf(']');
        if (start < 0 || end <= start) {
            return null;
        }
        try {
            JSONArray arr = JSONUtil.parseArray(aiOutput.substring(start, end + 1));
            List<Question> list = new ArrayList<>();
            for (Object o : arr) {
                JSONObject jo = (JSONObject) o;
                Question q = new Question();
                String type = jo.getStr("type");
                if (type == null) {
                    return null;
                }
                q.setType(type.trim());
                q.setTitle(jo.getStr("title"));
                q.setAnswer(jo.getStr("answer"));
                if (jo.containsKey("options")) {
                    // options 必须是JSON对象 {"A":"...","B":"..."}，原样序列化为字符串存储
                    Object optionsObj = jo.get("options");
                    if (!(optionsObj instanceof Map)) {
                        return null;
                    }
                    q.setOptions(JSONUtil.toJsonStr(optionsObj));
                }
                list.add(q);
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验题目：题数、各题型数量、字段完整性、答案合法性
     */
    private boolean validateQuestions(List<Question> questions, LinkedHashMap<String, Integer> requiredCount) {
        int totalRequired = requiredCount.values().stream().mapToInt(Integer::intValue).sum();
        if (questions.size() != totalRequired) {
            return false;
        }

        Map<String, Integer> countByType = new HashMap<>();
        for (Question q : questions) {
            String type = q.getType();
            if (!requiredCount.containsKey(type) || !StringUtils.hasText(q.getTitle())) {
                return false;
            }
            // 单选/判断的 answer 必填在各自分支校验；简答题允许不带 answer（参考格式约定）

            String answer = q.getAnswer() != null ? q.getAnswer().trim() : null;
            if ("单选".equals(type)) {
                // options 必须恰好包含 A、B、C、D 四个键
                Map<String, String> optionMap = parseOptionMap(q.getOptions());
                if (optionMap.size() != 4
                        || !optionMap.containsKey("A") || !optionMap.containsKey("B")
                        || !optionMap.containsKey("C") || !optionMap.containsKey("D")) {
                    return false;
                }
                if (!optionMap.containsKey(answer)) {
                    return false;
                }
            } else if ("判断".equals(type)) {
                // options 必须恰好包含 T、F 两个键
                Map<String, String> optionMap = parseOptionMap(q.getOptions());
                if (optionMap.size() != 2
                        || !optionMap.containsKey("T") || !optionMap.containsKey("F")) {
                    return false;
                }
                if (!"T".equals(answer) && !"F".equals(answer)) {
                    return false;
                }
            } else if ("简答".equals(type)) {
                // 参考答案为文本，无额外限制
            } else {
                return false;
            }
            countByType.merge(type, 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> e : requiredCount.entrySet()) {
            if (!e.getValue().equals(countByType.get(e.getKey()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 解析 options JSON 对象字符串为 Map，如 {"A":"...","B":"..."}
     */
    private LinkedHashMap<String, String> parseOptionMap(String options) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (!StringUtils.hasText(options)) {
            return map;
        }
        try {
            JSONObject obj = JSONUtil.parseObj(options);
            for (String key : obj.keySet()) {
                map.put(key, String.valueOf(obj.get(key)));
            }
            return map;
        } catch (Exception e) {
            return map;
        }
    }

    /**
     * 分值精确配平：按题型权重分配，取整后余数按小数大小补，总分严格相等
     */
    private List<Integer> distributeScores(List<Question> questions, int totalScore) {
        int totalWeight = 0;
        int[] weight = new int[questions.size()];
        for (int i = 0; i < questions.size(); i++) {
            weight[i] = TYPE_WEIGHT.getOrDefault(questions.get(i).getType(), 1);
            totalWeight += weight[i];
        }

        double[] exact = new double[questions.size()];
        int[] floorScore = new int[questions.size()];
        int sum = 0;
        for (int i = 0; i < questions.size(); i++) {
            exact[i] = (double) totalScore * weight[i] / totalWeight;
            floorScore[i] = (int) Math.floor(exact[i]);
            sum += floorScore[i];
        }

        int remaining = totalScore - sum;
        Integer[] order = new Integer[questions.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) ->
                Double.compare(exact[b] - floorScore[b], exact[a] - floorScore[a]));
        for (int k = 0; k < remaining; k++) {
            floorScore[order[k % order.length]]++;
        }

        List<Integer> result = new ArrayList<>();
        for (int s : floorScore) {
            result.add(s);
        }
        return result;
    }
}

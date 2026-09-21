package com.example.homework.tool;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.homework.common.R;
import com.example.homework.entity.*;
import com.example.homework.mapper.*;
import com.example.homework.service.HomeworkService;
import com.example.homework.vo.HomeworkAnswerVO;
import com.example.homework.vo.StudentAnswerDetailVO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 作业分析 AI 工具集
 * 大模型通过 Function Calling 自动调用这些方法查询数据库真实数据
 * 全部复用项目已有 Mapper，不新增任何 Mapper
 */
@Component
public class HomeworkAnalysisTools {

    @Autowired
    private HomeworkMapper homeworkMapper;
    @Autowired
    private HomeworkQuestionMapper homeworkQuestionMapper;
    @Autowired
    private HomeworkSubmitMapper homeworkSubmitMapper;
    @Autowired
    private HomeworkSubmitDetailMapper homeworkSubmitDetailMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private ClazzMapper clazzMapper;
    @Autowired
    private HomeworkService homeworkService;

    /**
     * 工具1：作业概览 —— 基本信息 + 提交/批改统计
     */
    @Tool(description = "根据作业ID查询作业基本信息和提交、批改统计，包括作业标题、所属班级、满分、开始时间、截止时间、班级总人数、已提交人数、未提交人数、已批改人数、未批改人数。分析作业时应首先调用此工具。")
    public String getHomeworkOverview(@ToolParam(description = "作业ID") Long homeworkId) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            return JSONUtil.toJsonStr(Map.of("error", "作业不存在，ID：" + homeworkId));
        }

        // 班级信息
        String clazzName = null;
        Integer totalStudent = null;
        if (homework.getClazzId() != null) {
            Clazz clazz = clazzMapper.getClazzById(homework.getClazzId());
            clazzName = clazz != null ? clazz.getClazzName() : null;
            totalStudent = homeworkMapper.getClazzStudentCount(homework.getClazzId());
        }

        // 提交、批改统计
        Integer submitCount = homeworkMapper.getHomeworkSubmitCount(homeworkId);
        Integer correctedCount = homeworkMapper.getHomeworkCorrectedCount(homeworkId);
        int total = totalStudent != null ? totalStudent : 0;
        int submitted = submitCount != null ? submitCount : 0;
        int corrected = correctedCount != null ? correctedCount : 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("homeworkId", homework.getId());
        result.put("title", homework.getTitle());
        result.put("clazzId", homework.getClazzId());
        result.put("clazzName", clazzName);
        result.put("totalScore", homework.getTotalScore());
        result.put("startTime", homework.getStartTime());
        result.put("deadline", homework.getDeadline());
        result.put("totalStudent", total);
        result.put("submitCount", submitted);
        result.put("unsubmitCount", total - submitted);
        result.put("correctedCount", corrected);
        result.put("uncorrectedCount", submitted - corrected);
        return JSONUtil.toJsonStr(result);
    }

    /**
     * 工具2：成绩统计 —— 平均/最高/最低/及格率/分数段
     */
    @Tool(description = "查询已批改学生的成绩统计，包括有效份数、平均分、最高分、最低分、及格率以及优秀、良好、中等、及格、不及格各分数段的人数。仅统计已批改出分的学生。")
    public String getScoreStatistics(@ToolParam(description = "作业ID") Long homeworkId) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            return JSONUtil.toJsonStr(Map.of("error", "作业不存在，ID：" + homeworkId));
        }

        // 复用已有查询：班级全部学生 + 提交记录
        List<HomeworkAnswerVO.StudentAnswerVO> studentList =
                homeworkMapper.selectStudentAnswerByHomeworkId(homeworkId);

        int fullScore = homework.getTotalScore() != null ? homework.getTotalScore() : 0;

        // 只统计已出分（totalScore 非空）的学生
        List<Integer> scores = new ArrayList<>();
        for (HomeworkAnswerVO.StudentAnswerVO s : studentList) {
            if (s.getSubmitTime() != null && s.getTotalScore() != null) {
                scores.add(s.getTotalScore());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fullScore", fullScore);
        result.put("gradedCount", scores.size());

        if (scores.isEmpty()) {
            result.put("message", "该作业还没有已批改出分的学生，暂无法进行成绩统计");
            return JSONUtil.toJsonStr(result);
        }

        int max = Collections.max(scores);
        int min = Collections.min(scores);
        double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0);

        // 按得分占满分比例划分分数段
        int excellent = 0, good = 0, medium = 0, pass = 0, fail = 0;
        for (int sc : scores) {
            double ratio = fullScore > 0 ? (double) sc / fullScore : 0;
            if (ratio >= 0.9) excellent++;
            else if (ratio >= 0.8) good++;
            else if (ratio >= 0.7) medium++;
            else if (ratio >= 0.6) pass++;
            else fail++;
        }

        result.put("averageScore", Math.round(avg * 100) / 100.0);
        result.put("maxScore", max);
        result.put("minScore", min);
        result.put("passRate", Math.round((scores.size() - fail) * 10000.0 / scores.size()) / 100.0);
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("优秀(>=90%)", excellent);
        distribution.put("良好(80%-89%)", good);
        distribution.put("中等(70%-79%)", medium);
        distribution.put("及格(60%-69%)", pass);
        distribution.put("不及格(<60%)", fail);
        result.put("scoreDistribution", distribution);
        return JSONUtil.toJsonStr(result);
    }

    /**
     * 工具3：未提交学生名单
     */
    @Tool(description = "查询尚未提交该作业的学生名单，包括未提交人数和学生姓名列表。")
    public String getUnsubmittedStudents(@ToolParam(description = "作业ID") Long homeworkId) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            return JSONUtil.toJsonStr(Map.of("error", "作业不存在，ID：" + homeworkId));
        }

        List<HomeworkAnswerVO.StudentAnswerVO> studentList =
                homeworkMapper.selectStudentAnswerByHomeworkId(homeworkId);

        List<String> unsubmitted = new ArrayList<>();
        for (HomeworkAnswerVO.StudentAnswerVO s : studentList) {
            if (s.getSubmitTime() == null) {
                unsubmitted.add(s.getStudentName());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unsubmitCount", unsubmitted.size());
        result.put("students", unsubmitted);
        return JSONUtil.toJsonStr(result);
    }

    /**
     * 工具4：题目得分分析 —— 每题平均得分率，识别薄弱题目
     */
    @Tool(description = "查询作业每道题目的批改得分情况，包括题目标题、题型、满分、已批改份数、平均得分和平均得分率。可用于找出学生错误率高、掌握薄弱的具体题目。")
    public String getQuestionAnalysis(@ToolParam(description = "作业ID") Long homeworkId) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            return JSONUtil.toJsonStr(Map.of("error", "作业不存在，ID：" + homeworkId));
        }

        // 作业关联题目（含本题分值、排序）
        List<HomeworkQuestion> homeworkQuestions = homeworkQuestionMapper.selectByHomeworkId(homeworkId);

        // 有效提交记录（只统计有提交时间的）
        List<HomeworkSubmit> submits = homeworkSubmitMapper.selectByHomeworkId(homeworkId);
        List<Long> validSubmitIds = new ArrayList<>();
        for (HomeworkSubmit s : submits) {
            if (s.getSubmitTime() != null) {
                validSubmitIds.add(s.getId());
            }
        }

        // 聚合每题得分：questionId -> 已批改分数列表
        Map<Long, List<Integer>> scoresByQuestion = new HashMap<>();
        for (Long submitId : validSubmitIds) {
            List<HomeworkSubmitDetail> details = homeworkSubmitDetailMapper.selectBySubmitId(submitId);
            for (HomeworkSubmitDetail d : details) {
                if (d.getScore() != null) {
                    scoresByQuestion.computeIfAbsent(d.getQuestionId(), k -> new ArrayList<>()).add(d.getScore());
                }
            }
        }

        List<Map<String, Object>> questions = new ArrayList<>();
        for (HomeworkQuestion hq : homeworkQuestions) {
            Question q = questionMapper.selectById(hq.getQuestionId());
            List<Integer> qs = scoresByQuestion.getOrDefault(hq.getQuestionId(), Collections.emptyList());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("questionId", hq.getQuestionId());
            item.put("title", q != null ? q.getTitle() : null);
            item.put("type", q != null ? q.getType() : null);
            item.put("fullScore", hq.getScore());
            item.put("gradedCount", qs.size());
            if (!qs.isEmpty()) {
                double avg = qs.stream().mapToInt(Integer::intValue).average().orElse(0);
                int full = hq.getScore() != null ? hq.getScore() : 0;
                item.put("averageScore", Math.round(avg * 100) / 100.0);
                item.put("scoreRate", full > 0 ? Math.round(avg * 10000 / full) / 100.0 : null);
            }
            questions.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("questionCount", questions.size());
        result.put("questions", questions);
        return JSONUtil.toJsonStr(result);
    }

    // ==================== 细节增强：5 表关联纯内存聚合（仍只复用已有 Mapper） ====================

    /**
     * 有效提交记录（有提交时间）
     */
    private List<HomeworkSubmit> loadValidSubmits(Long homeworkId) {
        List<HomeworkSubmit> result = new ArrayList<>();
        for (HomeworkSubmit s : homeworkSubmitMapper.selectByHomeworkId(homeworkId)) {
            if (s.getSubmitTime() != null) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * 加载每题全部作答明细：questionId -> 明细列表
     */
    private Map<Long, List<HomeworkSubmitDetail>> loadDetailsByQuestion(List<HomeworkSubmit> validSubmits) {
        Map<Long, List<HomeworkSubmitDetail>> map = new HashMap<>();
        for (HomeworkSubmit s : validSubmits) {
            for (HomeworkSubmitDetail d : homeworkSubmitDetailMapper.selectBySubmitId(s.getId())) {
                map.computeIfAbsent(d.getQuestionId(), k -> new ArrayList<>()).add(d);
            }
        }
        return map;
    }

    /**
     * 答案归一化：去空格标点、字母大写、字符排序，用于多选答案比对
     */
    private String normalizeAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        String cleaned = answer.replaceAll("[\\s,，、;；/]", "").toUpperCase();
        char[] chars = cleaned.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    /**
     * 工具5：题型 / 难度维度得分分析
     */
    @Tool(description = "按题型（单选/多选/判断/简答）和题目难度两个维度，分别统计每类题目的题量和加权平均得分率。用于判断学生在不同题型、不同难度题目上的掌握差异，例如是否多选题失分严重、高难度题目是否超出学生能力。")
    public String getQuestionTypeAndDifficultyAnalysis(@ToolParam(description = "作业ID") Long homeworkId) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            return JSONUtil.toJsonStr(Map.of("error", "作业不存在，ID：" + homeworkId));
        }

        List<HomeworkQuestion> homeworkQuestions = homeworkQuestionMapper.selectByHomeworkId(homeworkId);
        List<HomeworkSubmit> validSubmits = loadValidSubmits(homeworkId);
        Map<Long, List<HomeworkSubmitDetail>> detailMap = loadDetailsByQuestion(validSubmits);

        // 维度值 -> [累计满分(题满分×批改份数), 累计得分, 题量]
        Map<String, double[]> typeAgg = new LinkedHashMap<>();
        Map<String, double[]> diffAgg = new LinkedHashMap<>();

        for (HomeworkQuestion hq : homeworkQuestions) {
            Question q = questionMapper.selectById(hq.getQuestionId());
            String type = q != null && q.getType() != null ? q.getType() : "未知题型";
            String difficulty = q != null && q.getDifficulty() != null ? q.getDifficulty() : "未标注难度";
            int full = hq.getScore() != null ? hq.getScore() : 0;

            List<HomeworkSubmitDetail> details = detailMap.getOrDefault(hq.getQuestionId(), Collections.emptyList());
            int graded = 0;
            int gotSum = 0;
            for (HomeworkSubmitDetail d : details) {
                if (d.getScore() != null) {
                    graded++;
                    gotSum += d.getScore();
                }
            }

            accumulateDim(typeAgg, type, full * graded, gotSum);
            accumulateDim(diffAgg, difficulty, full * graded, gotSum);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("submittedCount", validSubmits.size());
        result.put("byType", buildDimStats(typeAgg));
        result.put("byDifficulty", buildDimStats(diffAgg));
        return JSONUtil.toJsonStr(result);
    }

    private void accumulateDim(Map<String, double[]> agg, String key, int fullWeighted, int gotSum) {
        double[] arr = agg.computeIfAbsent(key, k -> new double[3]);
        arr[0] += fullWeighted;
        arr[1] += gotSum;
        arr[2] += 1;
    }

    private List<Map<String, Object>> buildDimStats(Map<String, double[]> agg) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, double[]> e : agg.entrySet()) {
            double[] arr = e.getValue();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", e.getKey());
            item.put("questionCount", (int) arr[2]);
            item.put("avgScoreRate", arr[0] > 0 ? Math.round(arr[1] * 10000 / arr[0]) / 100.0 : null);
            list.add(item);
        }
        return list;
    }

    /**
     * 工具6：高频错误答案分布 + 评语聚合
     */
    @Tool(description = "查询每道题学生答案的具体分布：客观题（单选/多选/判断）统计每个选项被选择人数、最多人错选的答案及对应选项内容、未作答人数；简答题统计未作答人数并汇总老师的批改评语。用于精准定位全班的共性错误和认知误区。")
    public String getWrongAnswerDistribution(@ToolParam(description = "作业ID") Long homeworkId) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            return JSONUtil.toJsonStr(Map.of("error", "作业不存在，ID：" + homeworkId));
        }

        List<HomeworkQuestion> homeworkQuestions = homeworkQuestionMapper.selectByHomeworkId(homeworkId);
        List<HomeworkSubmit> validSubmits = loadValidSubmits(homeworkId);
        Map<Long, List<HomeworkSubmitDetail>> detailMap = loadDetailsByQuestion(validSubmits);

        List<Map<String, Object>> questions = new ArrayList<>();
        for (HomeworkQuestion hq : homeworkQuestions) {
            Question q = questionMapper.selectById(hq.getQuestionId());
            List<HomeworkSubmitDetail> details = detailMap.getOrDefault(hq.getQuestionId(), Collections.emptyList());

            // 解析选项，建立 键 -> 选项内容 映射（单选 A-D，判断 T-F）
            Map<String, String> optionMap = parseOptionMap(q != null ? q.getOptions() : null);
            boolean objective = q != null && q.getType() != null
                    && (q.getType().contains("单选") || q.getType().contains("多选") || q.getType().contains("判断"));

            // 答案计数（保持选项顺序）
            Map<String, Integer> answerCount = new LinkedHashMap<>();
            int blankCount = 0;
            List<String> remarks = new ArrayList<>();

            for (HomeworkSubmitDetail d : details) {
                String ans = d.getStudentAnswer();
                if (ans == null || ans.isBlank()) {
                    blankCount++;
                } else {
                    answerCount.merge(ans.trim(), 1, Integer::sum);
                }
                if (d.getRemark() != null && !d.getRemark().isBlank()) {
                    remarks.add(d.getRemark().trim());
                }
            }

            // 构建答案分布明细
            List<Map<String, Object>> distribution = new ArrayList<>();
            String correctNorm = normalizeAnswer(q != null ? q.getAnswer() : null);
            String mostCommonWrong = null;
            int mostCommonWrongCount = 0;

            for (Map.Entry<String, Integer> e : answerCount.entrySet()) {
                String ans = e.getKey();
                Map<String, Object> dist = new LinkedHashMap<>();
                dist.put("answer", ans);
                dist.put("answerContent", objective ? matchOptionContent(ans, optionMap) : null);
                boolean isCorrect = normalizeAnswer(ans).equals(correctNorm) && !correctNorm.isEmpty();
                dist.put("isCorrect", isCorrect);
                dist.put("count", e.getValue());
                distribution.add(dist);

                if (!isCorrect && e.getValue() > mostCommonWrongCount) {
                    mostCommonWrongCount = e.getValue();
                    mostCommonWrong = ans;
                }
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("questionId", hq.getQuestionId());
            item.put("title", q != null ? q.getTitle() : null);
            item.put("type", q != null ? q.getType() : null);
            item.put("correctAnswer", q != null ? q.getAnswer() : null);
            item.put("answeredCount", answerCount.values().stream().mapToInt(Integer::intValue).sum());
            item.put("blankCount", blankCount);
            item.put("answerDistribution", distribution);
            item.put("mostCommonWrongAnswer", mostCommonWrong);
            item.put("mostCommonWrongAnswerContent",
                    mostCommonWrong != null && objective ? matchOptionContent(mostCommonWrong, optionMap) : null);
            item.put("mostCommonWrongCount", mostCommonWrongCount);

            // 简答题：评语去重（保留出现次数），最多给 10 条
            if (!objective) {
                Map<String, Integer> remarkCount = new LinkedHashMap<>();
                for (String r : remarks) {
                    remarkCount.merge(r, 1, Integer::sum);
                }
                List<Map<String, Object>> remarkStats = new ArrayList<>();
                remarkCount.entrySet().stream().limit(10).forEach(en -> {
                    Map<String, Object> rm = new LinkedHashMap<>();
                    rm.put("remark", en.getKey());
                    rm.put("count", en.getValue());
                    remarkStats.add(rm);
                });
                item.put("remarkSummary", remarkStats);
            }
            questions.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("submittedCount", validSubmits.size());
        result.put("questions", questions);
        return JSONUtil.toJsonStr(result);
    }

    /**
     * 解析题目选项为 键->内容 的有序Map。
     * 新格式：{"A":"...","B":"..."} / {"T":"正确","F":"错误"}；
     * 兼容旧数组格式：["...","..."] 自动补 A/B/C/D 键
     */
    private LinkedHashMap<String, String> parseOptionMap(String options) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (options == null || options.isBlank()) {
            return map;
        }
        try {
            Object parsed = JSONUtil.parse(options);
            if (parsed instanceof cn.hutool.json.JSONObject jsonObject) {
                for (String key : jsonObject.keySet()) {
                    map.put(key, String.valueOf(jsonObject.get(key)));
                }
            } else if (parsed instanceof JSONArray array) {
                for (int i = 0; i < array.size(); i++) {
                    map.put(String.valueOf((char) ('A' + i)), String.valueOf(array.get(i)));
                }
            }
            return map;
        } catch (Exception e) {
            return map;
        }
    }

    /**
     * 将答案键映射为选项内容（单选按 A/B/C/D，判断按 T/F）
     */
    private String matchOptionContent(String answer, Map<String, String> optionMap) {
        if (answer == null || optionMap.isEmpty()) {
            return null;
        }
        return optionMap.get(answer.trim());
    }

    /**
     * 工具7：学生个体表现 —— 排名、得分率、薄弱题、未作答题
     */
    @Tool(description = "查询每位已提交学生的个体表现：总分、班级排名、得分率、得分率低于60%的薄弱题目和未作答题目，按得分从低到高排列。用于精准识别需要重点辅导的学生及其具体薄弱环节，支持分层教学。")
    public String getStudentPerformanceDetail(@ToolParam(description = "作业ID") Long homeworkId) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            return JSONUtil.toJsonStr(Map.of("error", "作业不存在，ID：" + homeworkId));
        }
        int fullScore = homework.getTotalScore() != null ? homework.getTotalScore() : 0;

        // 有效提交：studentId -> submit
        List<HomeworkSubmit> validSubmits = loadValidSubmits(homeworkId);
        Map<Long, HomeworkSubmit> submitByStudent = new HashMap<>();
        for (HomeworkSubmit s : validSubmits) {
            submitByStudent.put(s.getStudentId(), s);
        }

        // 作业题目（含本题分值、排序）
        List<HomeworkQuestion> homeworkQuestions = homeworkQuestionMapper.selectByHomeworkId(homeworkId);

        // 班级全部学生（含未提交）
        List<HomeworkAnswerVO.StudentAnswerVO> allStudents =
                homeworkMapper.selectStudentAnswerByHomeworkId(homeworkId);

        List<Map<String, Object>> students = new ArrayList<>();
        for (HomeworkAnswerVO.StudentAnswerVO vo : allStudents) {
            HomeworkSubmit submit = submitByStudent.get(vo.getStudentId());
            if (submit == null || submit.getTotalScore() == null) {
                continue; // 未提交或未批改，个体成绩分析中跳过
            }

            List<HomeworkSubmitDetail> details =
                    homeworkSubmitDetailMapper.selectBySubmitId(submit.getId());
            Map<Long, HomeworkSubmitDetail> detailByQuestion = new HashMap<>();
            for (HomeworkSubmitDetail d : details) {
                detailByQuestion.put(d.getQuestionId(), d);
            }

            List<String> weakQuestions = new ArrayList<>();
            List<String> blankQuestions = new ArrayList<>();
            for (HomeworkQuestion hq : homeworkQuestions) {
                Question q = questionMapper.selectById(hq.getQuestionId());
                String title = q != null ? q.getTitle() : ("题目ID:" + hq.getQuestionId());
                HomeworkSubmitDetail d = detailByQuestion.get(hq.getQuestionId());

                if (d == null || d.getStudentAnswer() == null || d.getStudentAnswer().isBlank()) {
                    blankQuestions.add(title);
                } else if (d.getScore() != null && hq.getScore() != null && hq.getScore() > 0
                        && (double) d.getScore() / hq.getScore() < 0.6) {
                    weakQuestions.add(title);
                }
            }

            int total = submit.getTotalScore();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("studentId", vo.getStudentId());
            item.put("studentName", vo.getStudentName());
            item.put("totalScore", total);
            item.put("scoreRate", fullScore > 0 ? Math.round(total * 10000.0 / fullScore) / 100.0 : null);
            item.put("weakQuestions", weakQuestions);
            item.put("blankQuestions", blankQuestions);
            students.add(item);
        }

        // 按总分从低到高排序，并赋予班级排名（基于分数从高到低的名次）
        students.sort(Comparator.comparingInt(s -> (int) s.get("totalScore")));
        List<Integer> rankedScores = new ArrayList<>();
        for (Map<String, Object> s : students) {
            rankedScores.add((int) s.get("totalScore"));
        }
        rankedScores.sort(Comparator.reverseOrder());
        for (Map<String, Object> s : students) {
            s.put("rank", rankedScores.indexOf(s.get("totalScore")) + 1);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fullScore", fullScore);
        result.put("gradedCount", students.size());
        result.put("students", students);
        return JSONUtil.toJsonStr(result);
    }

    /**
     * 工具8：单个学生完整逐题作答详情（直接复用 queryStudentAnswerDetail 接口逻辑）
     */
    @Tool(description = "查询指定学生在这份作业上的完整逐题作答详情，包括每道题的题干、题型、选项、该学生的具体答案、正确答案、本题满分与得分、批改评语。仅用于深入查看少数重点学生（如低分学生）的作答细节，不要对全班学生逐一调用；全班错题明细请使用 getClassWrongAnswerDetails 工具。")
    public String getStudentAnswerDetail(@ToolParam(description = "作业ID") Long homeworkId,
                                         @ToolParam(description = "学生ID") Long studentId) {
        if (homeworkId == null || studentId == null) {
            return JSONUtil.toJsonStr(Map.of("error", "作业ID和学生ID不能为空"));
        }
        R<StudentAnswerDetailVO> r = homeworkService.queryStudentAnswerDetail(homeworkId, studentId);
        if (r.getData() != null) {
            return JSONUtil.toJsonStr(r.getData());
        }
        return JSONUtil.toJsonStr(Map.of("message",
                r.getMsg() != null ? r.getMsg() : "未查询到该学生的作答详情（可能未提交）"));
    }

    /**
     * 工具9：全班错题 / 未作答逐生明细
     */
    @Tool(description = "一次性查询全班已提交学生的错题和未作答明细：按学生逐个列出每道未得满分的题目，包括学生具体答案及对应选项内容、正确答案及选项内容、本题得分与满分、教师批改评语、是否未作答。可全面掌握“谁、在哪道题、具体错答成什么”。已得满分的题目不包含在结果中，学生按错题数量从多到少排列。")
    public String getClassWrongAnswerDetails(@ToolParam(description = "作业ID") Long homeworkId) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            return JSONUtil.toJsonStr(Map.of("error", "作业不存在，ID：" + homeworkId));
        }

        List<HomeworkQuestion> homeworkQuestions = homeworkQuestionMapper.selectByHomeworkId(homeworkId);

        // 学生ID -> 姓名
        Map<Long, String> nameMap = new HashMap<>();
        for (HomeworkAnswerVO.StudentAnswerVO vo :
                homeworkMapper.selectStudentAnswerByHomeworkId(homeworkId)) {
            nameMap.put(vo.getStudentId(), vo.getStudentName());
        }

        List<Map<String, Object>> students = new ArrayList<>();
        int totalWrongItems = 0;

        for (HomeworkSubmit submit : loadValidSubmits(homeworkId)) {
            Map<Long, HomeworkSubmitDetail> detailByQuestion = new HashMap<>();
            for (HomeworkSubmitDetail d : homeworkSubmitDetailMapper.selectBySubmitId(submit.getId())) {
                detailByQuestion.put(d.getQuestionId(), d);
            }

            List<Map<String, Object>> wrongItems = new ArrayList<>();
            for (HomeworkQuestion hq : homeworkQuestions) {
                Question q = questionMapper.selectById(hq.getQuestionId());
                HomeworkSubmitDetail d = detailByQuestion.get(hq.getQuestionId());

                Integer score = d != null ? d.getScore() : null;
                Integer full = hq.getScore();
                boolean noAnswer = d == null
                        || d.getStudentAnswer() == null || d.getStudentAnswer().isBlank();
                boolean notFull = score == null || (full != null && score < full);
                if (!noAnswer && !notFull) {
                    continue; // 已作答且满分，跳过
                }

                Map<String, String> optionMap = parseOptionMap(q != null ? q.getOptions() : null);
                String studentAnswer = d != null ? d.getStudentAnswer() : null;
                String correctAnswer = q != null ? q.getAnswer() : null;

                // 学生答案：客观题映射选项内容；简答题长文本截断
                String studentAnswerContent = null;
                if (studentAnswer != null && !studentAnswer.isBlank()) {
                    studentAnswerContent = optionMap.get(studentAnswer.trim());
                    if (studentAnswerContent == null) {
                        studentAnswer = truncate(studentAnswer, 200);
                    }
                }
                String correctAnswerContent = correctAnswer != null
                        ? optionMap.get(correctAnswer.trim()) : null;

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("questionId", hq.getQuestionId());
                item.put("title", q != null ? q.getTitle() : null);
                item.put("type", q != null ? q.getType() : null);
                item.put("studentAnswer", noAnswer ? null : studentAnswer);
                item.put("studentAnswerContent", studentAnswerContent);
                item.put("correctAnswer", correctAnswer);
                item.put("correctAnswerContent", correctAnswerContent);
                item.put("score", score);
                item.put("fullScore", full);
                item.put("remark", d != null && d.getRemark() != null
                        ? truncate(d.getRemark(), 100) : null);
                item.put("blank", noAnswer);
                wrongItems.add(item);
            }

            if (!wrongItems.isEmpty()) {
                totalWrongItems += wrongItems.size();
                Map<String, Object> sm = new LinkedHashMap<>();
                sm.put("studentId", submit.getStudentId());
                sm.put("studentName", nameMap.get(submit.getStudentId()));
                sm.put("totalScore", submit.getTotalScore());
                sm.put("wrongCount", wrongItems.size());
                sm.put("wrongQuestions", wrongItems);
                students.add(sm);
            }
        }

        // 按错题数量从多到少排列，问题最多的学生在前
        students.sort((a, b) ->
                Integer.compare((int) b.get("wrongCount"), (int) a.get("wrongCount")));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("submittedCount", loadValidSubmits(homeworkId).size());
        result.put("studentsWithWrongCount", students.size());
        result.put("totalWrongItems", totalWrongItems);
        result.put("students", students);
        return JSONUtil.toJsonStr(result);
    }

    /**
     * 文本超长截断（用于简答答案、评语，控制返回数据量）
     */
    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}

package com.example.homework.controller;

import com.example.homework.common.R;
import com.example.homework.dto.HomeworkQueryDTO;
import com.example.homework.dto.HomeworkSubmitDTO;
import com.example.homework.dto.QuestionSubmitDTO;
import com.example.homework.dto.SmartHomeworkDTO;
import com.example.homework.dto.SubmitWorkDTO;
import com.example.homework.dto.TeacherCorrectionDTO;
import com.example.homework.entity.Homework;
import com.example.homework.entity.HomeworkReport;
import com.example.homework.mapper.HomeworkReportMapper;
import com.example.homework.service.AiAnalysisService;
import com.example.homework.service.CorrectionService;
import com.example.homework.service.HomeworkService;
import com.example.homework.service.HomeworkStatService;
import com.example.homework.service.HomeworkSubmitService;
import com.example.homework.service.SmartHomeworkService;
import com.example.homework.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 作业控制层
 * 处理作业相关的HTTP请求
 * 基础路径：/homework
 */
@RestController
@RequestMapping("/homework")
public class HomeworkController {

    @Autowired
    private HomeworkService homeworkService;
    @Autowired
    private HomeworkSubmitService homeworkSubmitService;
    @Autowired
    private CorrectionService correctionService;
    @Autowired
    private HomeworkStatService homeworkStatService;
    @Autowired
    private AiAnalysisService aiAnalysisService;
    @Autowired
    private HomeworkReportMapper homeworkReportMapper;
    @Autowired
    private SmartHomeworkService smartHomeworkService;

    /**
     * 发布新作业（教师）
     * 请求方式：POST
     * 请求路径：/homework/add
     * 请求体：Homework对象（标题、要求、课程/班级、时间、满分等）
     * 返回值：操作结果
     */
    @PostMapping("/add")
    public R<String> addHomework(@RequestBody Homework homework) {
        return homeworkService.addHomework(homework);
    }

    /**
     * 查询发布的作业列表（分页）（教师）
     * 请求方式：GET
     * 请求路径：/homework/list
     * 请求参数：teacherId（教师ID）、clazzId（班级ID，可选）、pageNum（页码，默认1）、pageSize（每页条数，默认10）
     * 返回值：作业列表+分页信息
     */
    @GetMapping("/list")
    public R<List<Homework>> getHomeworkList(
            @RequestParam(required = false) Long clazzId,
            @RequestParam(required = false) Long courseId,  // 新增
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return homeworkService.getHomeworkList(clazzId, courseId, pageNum, pageSize);
    }

    /**
     * 作业详情（含提交统计）（老师/学生）
     * 请求方式：GET
     * 请求路径：/homework/detail/{id}
     * 路径参数：id（作业ID）
     * 返回值：作业详情
     */
    @GetMapping("/detail/{id}")
    public R<Homework> getHomeworkDetail(@PathVariable Long id) {
        return homeworkService.getHomeworkDetail(id);
    }

    /**
     * 修改作业信息（截止时间、要求）（教师）
     * 请求方式：PUT
     * 请求路径：/homework/update
     * 请求体：Homework对象（ID、截止时间、要求）
     * 返回值：操作结果
     */
    @PutMapping("/update")
    public R<String> updateHomework(@RequestBody Homework homework) {
        return homeworkService.updateHomework(homework);
    }

    /**
     * 删除作业（需判断是否有学生提交记录）（教师）
     * 请求方式：DELETE
     * 请求路径：/homework/delete/{id}
     * 路径参数：id（作业ID）
     * 返回值：操作结果
     */
    @DeleteMapping("/delete/{id}")
    public R<String> deleteHomework(@PathVariable Long id) {
        return homeworkService.deleteHomework(id);
    }

    /**
     * 学生当前课程下的作业列表（可过滤 未提交/已提交）（学生）
     * 请求方式：GET
     * 请求路径：/homework/student/list
     * 请求参数：studentId（学生ID）、courseId（课程ID）、status（提交状态：unsubmit/submit）、pageNum（页码）、pageSize（每页条数）
     * 返回值：学生作业列表
     */
    @GetMapping("/student/list")
    public R<List<HomeworkVO>> getStudentHomeworkList(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return homeworkService.getStudentHomeworkList(courseId, status, pageNum, pageSize);
    }

    /**
     * 学生查看作业详情（含截止时间、是否提交）（学生）
     * 请求方式：GET
     * 请求路径：/homework/student/detail/{id}
     * 路径参数：id（作业ID）
     * 请求参数：studentId（学生ID）
     * 返回值：学生视角的作业详情
     */
    @GetMapping("/student/detail/{id}")
    public R<HomeworkDetailVO> getStudentHomeworkDetail(@PathVariable Long id) {
        return homeworkService.getStudentHomeworkDetail(id);
    }

    @PostMapping("/submit/question")
    public R<String> submitQuestion(@RequestBody QuestionSubmitDTO dto
    ) {
        return  homeworkSubmitService.submitQuestion(dto);
    }

    @PostMapping("/submitWork")
    public R<String> submitWork(@RequestBody HomeworkSubmitDTO homeworkId) {
        return homeworkSubmitService.submitWork(homeworkId.getHomeworkId());
    }
    @GetMapping("/Correction/{homeworkId}")
    public R<HomeworkPendingCorrectionVO> getPendingCorrection(@PathVariable Long homeworkId) {
        return R.success(correctionService.getPendingCorrection(homeworkId));
    }
    @PostMapping("/saveCorrection")
    public R<String> saveCorrection(@RequestBody TeacherCorrectionDTO dto) {
        return correctionService.saveCorrection(dto);
    }
    @GetMapping("/dashboard")
    public R<HomeworkStatVO> dashboard() {
        return homeworkStatService.getDashboardData();
    }
    /**
     * 查询作业下所有学生作答情况
     * @param queryDTO 前端传入参数DTO
     * @return 返回VO给前端
     */
    @PostMapping("/queryStudentAnswer")
    public R<HomeworkAnswerVO> queryStudentAnswer(@RequestBody HomeworkQueryDTO queryDTO){
        return homeworkService.queryStudentAnswer(queryDTO.getHomeworkId());
    }

    /**
     * 查询单个学生作业作答详情（含顶部元信息 + 题目列表）
     * 请求方式：POST
     * 请求路径：/homework/queryStudentAnswerDetail
     * 请求体：{homeworkId, studentId}
     * 返回值：作答详情（作业标题、学生姓名、学号、提交时间、得分、题目列表）
     */
    @PostMapping("/queryStudentAnswerDetail")
    public R<StudentAnswerDetailVO> queryStudentAnswerDetail(@Valid @RequestBody HomeworkQueryDTO queryDTO) {
        return homeworkService.queryStudentAnswerDetail(queryDTO.getHomeworkId(), queryDTO.getStudentId());
    }

    /**
     * AI 智能分析作业情况（Function Calling 自动查询数据库）
     * 请求方式：POST
     * 请求路径：/homework/analyze
     * 请求体：{homeworkId}
     * 返回值：Markdown 格式的作业分析报告与教学建议
     */
    @PostMapping("/analyze")
    public R<String> analyzeHomework(@RequestBody SubmitWorkDTO dto) {
        return aiAnalysisService.analyzeHomework(dto.getHomeworkId());
    }

    /**
     * 查询作业分析报告记录
     * 请求方式：POST
     * 请求路径：/homework/queryHomeworkReport
     * 请求体：{homeworkId}
     * 返回值：该作业最新的分析报告（无记录时 data 为 null）
     */
    @PostMapping("/queryHomeworkReport")
    public R<HomeworkReport> queryHomeworkReport(@RequestBody SubmitWorkDTO dto) {
        if (dto.getHomeworkId() == null) {
            return R.error("作业ID不能为空！");
        }
        return R.success(homeworkReportMapper.selectByHomeworkId(dto.getHomeworkId()));
    }

    /**
     * 智能发布作业（AI 按题型比例、难度、题量自动生成题目并发布）
     * 请求方式：POST
     * 请求路径：/homework/smartPublish
     * 请求体：{title,content,clazzId,courseId,deadline,difficulty,questionCount,totalScore,typePercent}
     * 返回值：发布结果
     */
    @PostMapping("/smartPublish")
    public R<String> smartPublish(@RequestBody SmartHomeworkDTO dto) {
        return smartHomeworkService.smartPublish(dto);
    }
}
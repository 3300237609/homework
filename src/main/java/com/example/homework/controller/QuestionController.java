package com.example.homework.controller;

import com.example.homework.common.R;
import com.example.homework.entity.Question;
import com.example.homework.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题库管理控制器
 * 提供题目增删改查接口
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * 添加单个题目
     */
    @PostMapping("/add")
    public R<String> addQuestion(@RequestBody Question question) {
        return questionService.addQuestion(question);
    }

    /**
     * 批量添加题目
     */
    @PostMapping("/batchAdd")
    public R<String> batchAddQuestion(@RequestBody List<Question> questionList) {
        return questionService.batchAddQuestion(questionList);
    }

    /**
     * 分页条件查询题目列表
     * @param courseId 课程ID
     * @param type 题型
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页条数
     */
    @GetMapping("/list")
    public R<List<Question>> getQuestionList(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return questionService.getQuestionList(courseId, type, keyword, pageNum, pageSize);
    }

    /**
     * 查询题目详情
     */
    @GetMapping("/detail")
    public R<Question> getQuestionDetail(@RequestParam Long questionId) {
        return questionService.getQuestionDetail(questionId);
    }

    /**
     * 修改题目
     */
    @PostMapping("/update")
    public R<String> updateQuestion(@RequestBody Question question) {
        return questionService.updateQuestion(question);
    }

    /**
     * 删除题目
     */
    @GetMapping("/delete")
    public R<String> deleteQuestion(@RequestParam Long questionId) {
        return questionService.deleteQuestion(questionId);
    }

    /**
     * 根据课程ID获取所有题目（发布作业时使用）
     */
    @GetMapping("/course")
    public R<List<Question>> getQuestionByCourseId(@RequestParam Long courseId) {
        return questionService.getQuestionByCourseId(courseId);
    }
}
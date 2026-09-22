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
     * 请求方式：POST
     * 请求路径：/question/add
     * 请求体：Question对象（题干、题型、答案、难度等）
     * 返回值：操作结果
     */
    @PostMapping("/add")
    public R<String> addQuestion(@RequestBody Question question) {
        return questionService.addQuestion(question);
    }

    /**
     * 批量添加题目
     * 请求方式：POST
     * 请求路径：/question/batchAdd
     * 请求体：Question对象列表
     * 返回值：操作结果
     */
    @PostMapping("/batchAdd")
    public R<String> batchAddQuestion(@RequestBody List<Question> questionList) {
        return questionService.batchAddQuestion(questionList);
    }

    /**
     * 分页条件查询题目列表
     * 请求方式：GET
     * 请求路径：/question/list
     * 请求参数：courseId（课程ID，可选）、type（题型，可选）、keyword（关键词，可选）、difficulty（难度，可选）、pageNum（页码，默认1）、pageSize（每页条数，默认10）
     * 返回值：题目列表
     */
    @GetMapping("/list")
    public R<List<Question>> getQuestionList(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return questionService.getQuestionList(courseId, type, keyword, difficulty, pageNum, pageSize);
    }

    /**
     * 查询题目详情
     * 请求方式：GET
     * 请求路径：/question/detail
     * 请求参数：questionId（题目ID）
     * 返回值：题目详情
     */
    @GetMapping("/detail")
    public R<Question> getQuestionDetail(@RequestParam Long questionId) {
        return questionService.getQuestionDetail(questionId);
    }

    /**
     * 修改题目
     * 请求方式：POST
     * 请求路径：/question/update
     * 请求体：Question对象（ID、题干、答案等）
     * 返回值：操作结果
     */
    @PostMapping("/update")
    public R<String> updateQuestion(@RequestBody Question question) {
        return questionService.updateQuestion(question);
    }

    /**
     * 删除题目
     * 请求方式：GET
     * 请求路径：/question/delete
     * 请求参数：questionId（题目ID）
     * 返回值：操作结果
     */
    @GetMapping("/delete")
    public R<String> deleteQuestion(@RequestParam Long questionId) {
        return questionService.deleteQuestion(questionId);
    }

    /**
     * 根据课程ID获取所有题目（发布作业时使用）
     * 请求方式：GET
     * 请求路径：/question/course
     * 请求参数：courseId（课程ID）
     * 返回值：题目列表
     */
    @GetMapping("/course")
    public R<List<Question>> getQuestionByCourseId(@RequestParam Long courseId) {
        return questionService.getQuestionByCourseId(courseId);
    }
}
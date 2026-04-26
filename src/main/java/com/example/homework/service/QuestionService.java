package com.example.homework.service;

import com.example.homework.common.R;
import com.example.homework.entity.Question;
import java.util.List;

public interface QuestionService {

    R<String> addQuestion(Question question);

    R<String> batchAddQuestion(List<Question> questionList);

    R<List<Question>> getQuestionList(Long courseId, String type, String keyword, Integer pageNum, Integer pageSize);

    R<Question> getQuestionDetail(Long questionId);

    R<String> updateQuestion(Question question);

    R<String> deleteQuestion(Long questionId);

    R<List<Question>> getQuestionByCourseId(Long courseId);
}
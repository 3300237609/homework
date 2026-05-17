package com.example.homework.service;

import com.example.homework.common.R;
import com.example.homework.dto.QuestionSubmitDTO;

public interface HomeworkSubmitService {
    R<String> submitQuestion(QuestionSubmitDTO dto);
    R<String> submitWork(Long homeworkId);
}

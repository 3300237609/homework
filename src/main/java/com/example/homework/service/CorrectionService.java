package com.example.homework.service;

import com.example.homework.common.R;
import com.example.homework.dto.TeacherCorrectionDTO;
import com.example.homework.vo.HomeworkPendingCorrectionVO;

public interface CorrectionService {
    HomeworkPendingCorrectionVO getPendingCorrection(Long homeworkId);
    R<String> saveCorrection(TeacherCorrectionDTO dto);
}

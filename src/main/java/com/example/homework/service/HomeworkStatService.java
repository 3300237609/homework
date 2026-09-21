package com.example.homework.service;

import com.example.homework.common.R;
import com.example.homework.vo.HomeworkStatVO;

public interface HomeworkStatService {
    R<HomeworkStatVO> getDashboardData();
}
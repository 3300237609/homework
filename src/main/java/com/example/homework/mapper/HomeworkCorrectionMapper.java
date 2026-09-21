package com.example.homework.mapper;

import com.example.homework.vo.HomeworkPendingCorrectionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HomeworkCorrectionMapper {
    HomeworkPendingCorrectionVO getPendingCorrectionByHomeworkId(@Param("homeworkId") Long homeworkId);
    // 插入评语
    int insertComment(@Param("submitDetailId") Long submitDetailId,
                      @Param("comment") String comment);
}

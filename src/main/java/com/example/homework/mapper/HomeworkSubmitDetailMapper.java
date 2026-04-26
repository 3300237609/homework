package com.example.homework.mapper;

import com.example.homework.entity.HomeworkSubmitDetail;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface HomeworkSubmitDetailMapper {
    List<HomeworkSubmitDetail> selectBySubmitId(Long submitId);
    int insert(HomeworkSubmitDetail detail);
    int updateScore(HomeworkSubmitDetail detail);
}
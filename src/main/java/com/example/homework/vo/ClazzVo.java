package com.example.homework.vo;

import com.example.homework.entity.Clazz;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class ClazzVo extends Clazz {
    // 教师姓名，由联表查询自动填充
    private String teacherName;
}
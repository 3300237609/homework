package com.example.homework.vo;

import com.example.homework.entity.Course;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class CourseVo extends Course {
    private String teacherName;
}

package com.example.homework.mapper;

import com.example.homework.entity.Homework;
import com.example.homework.vo.HomeworkAnswerVO;
import com.example.homework.vo.HomeworkDetailVO;
import com.example.homework.vo.HomeworkQuestionVO;
import com.example.homework.vo.HomeworkVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeworkMapper {
    List<Homework> selectList();

    Homework selectById(Long id);

    int insert(Homework homework);

    int update(Homework homework);

    int updateTotalScore(Homework homework);

    int delete(Long id);

    // 统计班级学生总数
    Integer getClazzStudentCount(Long clazzId);

    // 统计作业提交人数
    Integer getHomeworkSubmitCount(Long homeworkId);

    // 统计作业已批改人数
    Integer getHomeworkCorrectedCount(Long homeworkId);

    //按条件查询教师发布的作业列表
    List<Homework> selectListByCondition(@Param("teacherId") Long teacherId,
                                         @Param("clazzId") Long clazzId,
                                         @Param("courseId") Long courseId);

    //查询学生视角的作业列表（过滤提交状态）
    List<HomeworkVO> selectStudentHomeworkList(
            @Param("courseId") Long courseId,
            @Param("status") String status,
            @Param("studentId") Long studentId
    );

    HomeworkDetailVO getHomeworkDetail(@Param("homeworkId") Long homeworkId,
                                       @Param("studentId") Long studentId);

    List<HomeworkQuestionVO> listHomeworkQuestionDetail(@Param("homeworkId") Long homeworkId,
                                                        @Param("studentId") Long studentId);
    /**
     * 根据作业id查询作业实体
     */
    Homework selectHomeworkById(@Param("homeworkId") Long homeworkId);

    /**
     * 查询班级所有学生 + 作业提交记录
     */
    List<HomeworkAnswerVO.StudentAnswerVO> selectStudentAnswerByHomeworkId(@Param("homeworkId") Long homeworkId);
}
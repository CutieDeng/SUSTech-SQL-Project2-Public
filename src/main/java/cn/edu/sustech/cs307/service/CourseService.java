package cn.edu.sustech.cs307.service;

import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

/**
 * 提供课程服务的接口，里面包括如下方法：<br>
 * {@link CourseService#addCourse(String, String, int, int, Course.CourseGrading, Prerequisite)} 添加一门课程。<br>
 * {@link CourseService#addCourseSection(String, int, String, int)} 添加一个课段。<br>
 * {@link CourseService#addCourseSectionClass(int, int, DayOfWeek, Set, short, short, String)} 添加一个课时。<br>
 * {@link CourseService#getAllCourses()} 获取所有课程，以列表呈现。<br>
 * 消歧义：<br>
 * 课程应当指的是某种类型的课程，课程大类。<br>
 * 课段指的是一门具体的课程，也是学生选择课程的最小单位。<br>
 * 课时则是课段的具体描述，也是学生进行课程的时间安排。<br>
 */
@ParametersAreNonnullByDefault
public interface CourseService {
    /**
     * 根据传入的参数添加一门课程。<br>
     * 如果某些参数是非法的，请丢出错误 {@link cn.edu.sustech.cs307.exception.IntegrityViolationException}。<br>
     * Add one course according to following parameters. <br>
     * If some of parameters are invalid, throw {@link cn.edu.sustech.cs307.exception.IntegrityViolationException}
     *
     * @param courseId represents the id of course. For example, CS307, CS309
     * @param courseName the name of course
     * @param credit the credit of course
     * @param classHour The total teaching hour that the course spends.
     * @param grading the grading type of course
     * @param prerequisite The root of a {@link cn.edu.sustech.cs307.dto.prerequisite.Prerequisite} expression tree.
     */
    void addCourse(String courseId, String courseName, int credit, int classHour,
                   Course.CourseGrading grading, @Nullable Prerequisite prerequisite);

    /**
     * 根据传入参数，添加一个课段。<br>
     * 如果有非法的参数，丢出错误 {@link cn.edu.sustech.cs307.exception.IntegrityViolationException}. <br>
     * Add one course section according to following parameters:
     * If some of parameters are invalid, throw {@link cn.edu.sustech.cs307.exception.IntegrityViolationException}
     *
     * @param courseId represents the id of course. For example, CS307, CS309
     * @param semesterId the id of semester
     * @param sectionName the name of section {@link cn.edu.sustech.cs307.dto.CourseSection}
     * @param totalCapacity the total capacity of section
     * @return the CourseSection id of new inserted line, if adding process is successful.
     */
    int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity);


    /**
     * 添加一个课段的具体课程安排。<br>
     * 如果有非法的传入参数，丢出错误 {@link cn.edu.sustech.cs307.exception.IntegrityViolationException}. <br>
     * Add one course section class according to following parameters:
     * If some of parameters are invalid, throw {@link cn.edu.sustech.cs307.exception.IntegrityViolationException}
     * @param sectionId 课段 id.
     * @param instructorId 教授 id.
     * @param dayOfWeek 星期几的课程。
     * @param weekList 课程活跃的周目情况。
     * @param classStart 当天课程开始的具体时间。
     * @param classEnd 当天课程结束的具体时间。
     * @param location 该课的授课地点。
     * @return the CourseSectionClass id of new inserted line.
     */
    int addCourseSectionClass(int sectionId, int instructorId, DayOfWeek dayOfWeek, Set<Short> weekList,
                              short classStart, short classEnd, String location);

    /**
     * 获取所有课程信息。
     * @return 一个列表，存储所有课程。
     */
    List<Course> getAllCourses();

}

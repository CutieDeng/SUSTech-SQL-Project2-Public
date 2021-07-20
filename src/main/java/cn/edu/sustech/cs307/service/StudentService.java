package cn.edu.sustech.cs307.service;

import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

/**
 * Student Service 学生服务接口<br>
 * 有以下公开方法：<br>
 * {@link #addStudent(int, int, String, String, Date)} 添加一个学生<br>
 * {@link #addEnrolledCourseWithGrade(int, int, Grade)}添加一个学生的课段成绩<br>
 * {@link #dropCourse(int, int)} 学生进行退课<br>
 * {@link #enrollCourse(int, int)} 学生进行选课<br>
 * {@link #getCourseTable(int, Date)} 学生获取当周的课程表<br>
 * {@link #searchCourse(int, int, String, String, String, DayOfWeek, Short, List, CourseType, boolean, boolean, boolean, boolean, int, int)}
 * 根据(非常多的参数)搜索相关课程<br>
 */
@SuppressWarnings("all")
@ParametersAreNonnullByDefault
public interface StudentService {
    /**
     * 如果选课失败，结果优先级在注释中描述。<br>
     * 原文：
     * The priority of EnrollResult should be (if not SUCCESS):
     *
     * COURSE_NOT_FOUND > ALREADY_ENROLLED > ALREADY_PASSED > PREREQUISITES_NOT_FULFILLED > COURSE_CONFLICT_FOUND > COURSE_IS_FULL > UNKNOWN_ERROR
     */
    enum EnrollResult {
        /**
         * 添加成功。<br>
         * 原文：
         * Enrolled successfully
         */
        SUCCESS,
        /**
         * 找不到课段。<br>
         * 原文：
         * Cannot found the course section
         */
        COURSE_NOT_FOUND,
        /**
         * 课段容量已满。<br>
         * 原文：
         * The course section is full
         */
        COURSE_IS_FULL,
        /**
         * 课段已选。<br>
         * 原文：
         * The course section is already enrolled by the student
         */
        ALREADY_ENROLLED,
        /**
         * 课程已通过。<br>
         * 原文：
         * The course (of the section) is already passed by the student
         */
        ALREADY_PASSED,
        /**
         * 前置课程未完成。<br>
         * 原文：
         * The student misses prerequisites for the course
         */
        PREREQUISITES_NOT_FULFILLED,
        /**
         * 课段冲突。<br>
         * 造成原因：<br>
         * 1. 重复的课程选择。<br>
         * 2. 课段时间冲突。<br>
         * 原文：
         * The student's enrolled courses has time conflicts with the section,
         * or has course conflicts (same course) with the section.
         */
        COURSE_CONFLICT_FOUND,
        /**
         * 未知的错误发生。<br>
         * 原文：
         * Other (unknown) errors
         */
        UNKNOWN_ERROR
    }

    enum CourseType {
        /**
         * [笔者认为]通识必修课<br>
         * 原文：
         * All courses
         */
        ALL,
        /**
         * 专业必修课<br>
         * 原文：
         * Courses in compulsory courses of the student's major
         */
        MAJOR_COMPULSORY,
        /**
         * 专业选修课<br>
         * 原文：
         * Courses in elective courses of the student's major
         */
        MAJOR_ELECTIVE,
        /**
         * 学生专业之外的其他课程。<br>
         * 原文：
         * Courses only in other majors than the student's major
         */
        CROSS_MAJOR,
        /**
         * 公开的课程，并不隶属于任何专业要求。<br>
         * 原文：
         * Courses not belong to any major's requirements
         */
        PUBLIC
    }

    /**
     * 创建一个学生实例<br>
     * 传入参数非法会抛出 {@link cn.edu.sustech.cs307.exception.IntegrityViolationException}. <br>
     * 原文：
     * Add one student according to following parameters.
     * If some of parameters are invalid, throw {@link cn.edu.sustech.cs307.exception.IntegrityViolationException}
     *
     * @param userId todo: 这是啥？
     * @param majorId 专业 ID
     * @param firstName 学生名称
     * @param lastName 学生姓氏
     * @param enrolledDate 加入时间
     */
    void addStudent(int userId, int majorId, String firstName, String lastName, Date enrolledDate);

    /**
     * 我从来没听说过 15 个传入参数的方法，今日一见，名不虚传。😀 <br>
     * 搜索可行的所有课段。<br>
     *
     * 原文：
     * Search available courses (' sections) for the specified student in the semester with extra conditions.
     * The result should be first sorted by course ID, and then sorted by course full name (course.name[section.name]).
     * Ignore all course sections that have no sub-classes.
     * Note: All ignore* arguments are about whether or not the result should ignore such cases.
     * i.e. when ignoreFull is true, the result should filter out all sections that are full.
     *
     * @param studentId 学生 ID
     * @param semesterId 学期 ID
     * @param searchCid 查询的课程 ID<br>
     *                  原文：search course id. Rule: searchCid in course.id
     * @param searchName 查询的课程名 <br>
     *   原文： search course name. Rule: searchName in "course.name[section.name]"
     * @param searchInstructor 查询课程的教师名约束<br>
     *               原文：search instructor name.
     *                                   Rule: firstName + lastName begins with searchInstructor
     *                                   or firstName + ' ' + lastName begins with searchInstructor
     *                                   or firstName begins with searchInstructor
     *                                   or lastName begins with searchInstructor.
     * @param searchDayOfWeek 星期几的课程 <br>
     *                        原文：search day of week. Matches *any* class in the section in the search day of week.
     * @param searchClassTime todo: 这是啥呀？<br>
     *             原文：search class time. Matches *any* class in the section contains the search class time.
     * @param searchClassLocations 课程上课的地点<br>
     *                       原文：search class locations.
     *                            Matches *any* class in the section contains *any* location
     *                             from the search class locations.
     * @param searchCourseType 课程类型<br>
     *               原文：search course type. See {@link cn.edu.sustech.cs307.service.StudentService.CourseType}
     * @param ignoreFull 忽略课段<br>
     *   原文：whether or not to ignore full course sections.
     * @param ignoreConflict 忽略(课程/时间)冲突<br>
     *           原文：whether or not to ignore course or time conflicting course sections.
     *                                   Note that a section is both course and time conflicting with itself.
     *                                   See {@link cn.edu.sustech.cs307.dto.CourseSearchEntry#conflictCourseNames}
     * @param ignorePassed 忽略已经通过的课程<br>
     *       原为：whether or not to ignore the student's passed courses.
     * @param ignoreMissingPrerequisites 忽略不满足先修条件的课程<br>
     *                                   原文：whether or not to ignore courses with missing prerequisites.
     * @param pageSize 显示页大小<br>
     * 原文：the page size, effectively `limit pageSize`.
     *                                   It is the number of {@link cn.edu.sustech.cs307.dto.CourseSearchEntry}
     * @param pageIndex 第几页<br>
     * 原文：the page index, effectively `offset pageIndex * pageSize`.
     *                                   If the page index is so large that there is no message,return an empty list
     * @return 搜索到的实体列表<br>
     * 原文：a list of search entries. See {@link cn.edu.sustech.cs307.dto.CourseSearchEntry}
     */
    List<CourseSearchEntry> searchCourse(int studentId, int semesterId, @Nullable String searchCid,
                                         @Nullable String searchName, @Nullable String searchInstructor,
                                         @Nullable DayOfWeek searchDayOfWeek, @Nullable Short searchClassTime,
                                         @Nullable List<String> searchClassLocations,
                                         CourseType searchCourseType,
                                         boolean ignoreFull, boolean ignoreConflict,
                                         boolean ignorePassed, boolean ignoreMissingPrerequisites,
                                         int pageSize, int pageIndex);

    /**
     * 学生选课<br>
     * 原文：
     * It is the course selection function according to the studentId and courseId.
     * The test case can be invalid data or conflict info, so that it can return 8 different
     * types of enroll results.
     *
     * It is possible for a student-course have ALREADY_SELECTED and ALREADY_PASSED or PREREQUISITES_NOT_FULFILLED.
     * Please make sure the return priority is the same as above in similar cases.
     * {@link cn.edu.sustech.cs307.service.StudentService.EnrollResult}
     *
     * To check whether prerequisite courses are available for current one, only check the
     * grade of prerequisite courses are >= 60 or PASS
     *
     * @param studentId 学生 ID
     * @param sectionId the id of CourseSection
     * @return See {@link cn.edu.sustech.cs307.service.StudentService.EnrollResult}
     */
    EnrollResult enrollCourse(int studentId, int sectionId);

    /**
     * 退课<br>
     * Drop a course section for a student
     *
     * @param studentId 学生 ID
     * @param sectionId 课段 ID
     * @throws IllegalStateException 如果学生已经完成了该课段的学习(并获得了成绩)。<br>
     * 原文：if the student already has a grade for the course section.
     */
    void dropCourse(int studentId, int sectionId) throws IllegalStateException;

    /**
     * 添加学生成绩<br>
     * 该加课方式将会跳过先修课判断，直接为学生选择一门课并安排成绩。<br>
     * 课程应当接受对应类型的成绩。<br>
     * 课程的剩余容量不会因此发生改变。<br>
     * 原文：
     * It is used for importing existing data from other sources.
     * <p>
     * With this interface, staff for teaching affairs can bypass the
     * prerequisite fulfillment check to directly enroll a student in a course
     * and assign him/her a grade.
     *
     * If the scoring scheme of a course is one type in pass-or-fail and hundredmark grade,
     * your system should not accept the other type of grade.
     *
     * Course section's left capacity should remain unchanged after this method.
     *
     * @param studentId 学生 ID
     * @param sectionId 课段 ID<br>
     *                  原文：We will get the sectionId of one section first
     *                  and then invoke the method by using the sectionId.
     * @param grade     Can be null
     */
    void addEnrolledCourseWithGrade(int studentId, int sectionId, @Nullable Grade grade);

    /**
     * 根据日期返回课时表<br>
     * Return a course table in current week according to the date.
     *
     * @param studentId 学生 ID
     * @param date 参考日期
     * @return 根据给出的时间，学生整个周时(Monday-to-Sunday)学习的课程表<br>
     * 原文：the student's course table for the entire week of the date.
     * Regardless which day of week the date is, return Monday-to-Sunday course table for that week.
     */
    CourseTable getCourseTable(int studentId, Date date);

}

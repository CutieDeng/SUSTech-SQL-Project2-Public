package cn.edu.sustech.cs307.service;

import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;

import javax.annotation.Nullable;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

public interface AllService extends CourseService, DepartmentService, InstructorService, MajorService,
SemesterService, StudentService, UserService{
    @Override
    void addCourse(String courseId, String courseName, int credit, int classHour, Course.CourseGrading grading, @Nullable Prerequisite prerequisite);

    @Override
    int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity);

    @Override
    int addCourseSectionClass(int sectionId, int instructorId, DayOfWeek dayOfWeek, Set<Short> weekList, short classStart, short classEnd, String location);

    @Override
    List<Course> getAllCourses();

    @Override
    int addDepartment(String name);

    @Override
    void removeDepartment(int departmentId);

    @Override
    List<Department> getAllDepartments();

    @Override
    void addInstructor(int userId, String firstName, String lastName);

    @Override
    int addMajor(String name, int departmentId);

    @Override
    void addMajorCompulsoryCourse(int majorId, String courseId);

    @Override
    void addMajorElectiveCourse(int majorId, String courseId);

    @Override
    int addSemester(String name, Date begin, Date end);

    @Override
    void removeSemester(int semesterId);

    @Override
    List<Semester> getAllSemesters();

    @Override
    void addStudent(int userId, int majorId, String firstName, String lastName, Date enrolledDate);

    @Override
    List<CourseSearchEntry> searchCourse(int studentId, int semesterId, @Nullable String searchCid, @Nullable String searchName, @Nullable String searchInstructor, @Nullable DayOfWeek searchDayOfWeek, @Nullable Short searchClassTime, @Nullable List<String> searchClassLocations, CourseType searchCourseType, boolean ignoreFull, boolean ignoreConflict, boolean ignorePassed, boolean ignoreMissingPrerequisites, int pageSize, int pageIndex);

    @Override
    EnrollResult enrollCourse(int studentId, int sectionId);

    @Override
    void dropCourse(int studentId, int sectionId) throws IllegalStateException;

    @Override
    void addEnrolledCourseWithGrade(int studentId, int sectionId, @Nullable Grade grade);

    @Override
    CourseTable getCourseTable(int studentId, Date date);

    @Override
    void removeUser(int userId);

    @Override
    List<User> getAllUsers();

    @Override
    User getUser(int userId);
}

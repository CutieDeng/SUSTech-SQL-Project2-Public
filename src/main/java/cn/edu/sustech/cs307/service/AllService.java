package cn.edu.sustech.cs307.service;

import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;

import javax.annotation.Nullable;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
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
    void removeCourse(String courseId);

    @Override
    void removeCourseSection(int sectionId);

    @Override
    void removeCourseSectionClass(int classId);

    @Override
    List<Course> getAllCourses();

    @Override
    List<CourseSection> getCourseSectionsInSemester(String courseId, int semesterId);

    @Override
    Course getCourseBySection(int sectionId);

    @Override
    List<CourseSectionClass> getCourseSectionClasses(int sectionId);

    @Override
    CourseSection getCourseSectionByClass(int classId);

    @Override
    List<Student> getEnrolledStudentsInSemester(String courseId, int semesterId);

    @Override
    int addDepartment(String name);

    @Override
    void removeDepartment(int departmentId);

    @Override
    List<Department> getAllDepartments();

    @Override
    Department getDepartment(int departmentId);

    @Override
    void addInstructor(int userId, String firstName, String lastName);

    @Override
    List<CourseSection> getInstructedCourseSections(int instructorId, int semesterId);

    @Override
    int addMajor(String name, int departmentId);

    @Override
    void removeMajor(int majorId);

    @Override
    List<Major> getAllMajors();

    @Override
    Major getMajor(int majorId);

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
    Semester getSemester(int semesterId);

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
    void setEnrolledCourseGrade(int studentId, int sectionId, Grade grade);

    @Override
    Map<Course, Grade> getEnrolledCoursesAndGrades(int studentId, @Nullable Integer semesterId);

    @Override
    CourseTable getCourseTable(int studentId, Date date);

    @Override
    boolean passedPrerequisitesForCourse(int studentId, String courseId);

    @Override
    Major getStudentMajor(int studentId);

    @Override
    void removeUser(int userId);

    @Override
    List<User> getAllUsers();

    @Override
    User getUser(int userId);
}

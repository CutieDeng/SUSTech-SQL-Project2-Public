package CutieImplementation;

import cn.edu.sustech.cs307.dto.Course;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.service.CourseService;

import javax.annotation.Nullable;
import java.time.DayOfWeek;
import java.util.*;
import java.util.function.IntFunction;

@SuppressWarnings("all")
public class CutieCourseService implements CourseService {

    private IntFunction<Boolean> courseCreditLimit = i -> true;

    private IntFunction<Boolean> courseClassHourLimit = i -> true;

    public void setCourseCreditLimit(IntFunction<Boolean> courseCreditLimit) {
        if (courseCreditLimit != null) {
            this.courseCreditLimit = courseCreditLimit;
        }
    }

    public void setCourseClassHourLimit(IntFunction<Boolean> courseClassHourLimit) {
        if (courseClassHourLimit != null) {
            this.courseClassHourLimit = courseClassHourLimit;
        }
    }

    /**
     * 
     * @param courseId represents the id of course. For example, CS307, CS309
     * @param courseName the name of course
     * @param credit the credit of course
     * @param classHour The total teaching hour that the course spends.
     * @param grading the grading type of course
     * @param prerequisite The root of a {@link cn.edu.sustech.cs307.dto.prerequisite.Prerequisite} expression tree.
     */
    @Override
    public void addCourse(String courseId, String courseName, int credit, int classHour, Course.CourseGrading grading, @Nullable Prerequisite prerequisite) {
        Map<String, Object> objectMap = new HashMap<>();
        StringBuilder errorInfo = new StringBuilder();
        boolean errorFlag = false;
        if (courseId == null) {
            errorInfo.append("Unexpected courseId is null. \n");
            errorFlag = true;
        }
        objectMap.put("courseId", courseId);
        if (courseName == null) {
            errorInfo.append("Unexpected courseName is null. \n");
            errorFlag = true;
        }
        objectMap.put("courseName", courseName);
        if (!courseCreditLimit.apply(credit)) {
            errorInfo.append("Credit value invalid: ").append(credit).append("\n");
            errorFlag = true;
        }
        objectMap.put("credit", credit);
        if (!this.courseClassHourLimit.apply(classHour)) {
            errorInfo.append("ClassHour value invalid: ").append(classHour).append("\n");
            errorFlag = true;
        }
        objectMap.put("classHour", classHour);
        // grading 应该可以是 null, 就不做相关判断了。
        objectMap.put("grading", grading);
        if (prerequisite == null) {
            errorInfo.append("Prerequisite: null. \n");
            errorFlag = true;
        }
        objectMap.put("coursePrerequisite", prerequisite);
        if (errorFlag) {
            throw new RuntimeException(errorInfo.toString());
        }
        addCourse(objectMap);
    }

    private void addCourse(Map<String, Object> objectMap) {

    }

    @Override
    public int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity) {
        return 0;
    }

    @Override
    public int addCourseSectionClass(int sectionId, int instructorId, DayOfWeek dayOfWeek, Set<Short> weekList, short classStart, short classEnd, String location) {
        return 0;
    }

    @Override
    public List<Course> getAllCourses() {
        return null;
    }
}

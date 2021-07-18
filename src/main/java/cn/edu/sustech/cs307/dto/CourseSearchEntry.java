package cn.edu.sustech.cs307.dto;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CourseSearchEntry {
    /**
     * 课程<br>
     * The course of the searched section
     */
    public Course course;

    /**
     * 课段<br>
     * The searched course section
     */
    public CourseSection section;

    /**
     * 该课段的所有课时<br>
     * All classes of the section
     */
    public Set<CourseSectionClass> sectionClasses;

    /**
     * 与该课段产生时间冲突的所有其他课段<br>
     * 课程全名描述：courseName[sectionName]. <br>
     * 冲突的课段来源：该学生已经选择的课段<br>
     * 课段冲突：多个课段属于同一个课程<br>
     * 时间冲突：多个课段挤占同一个时间点<br>
     * tip: 任何一个课段都与它自己发生课段、时间冲突<br>
     * List all course or time conflicting courses' full name, sorted alphabetically.
     * Course full name: String.format("%s[%s]", course.name, section.name)
     * <p>
     * The conflict courses come from the student's enrolled courses (' sections).
     * <p>
     * Course conflict is when multiple sections belong to the same course.
     * Time conflict is when multiple sections have time-overlapping classes.
     * Note that a section is both course and time conflicting with itself!
     */
    public List<String> conflictCourseNames;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CourseSearchEntry entry = (CourseSearchEntry) o;
        return course.equals(entry.course) && section.equals(entry.section)
                && sectionClasses.equals(entry.sectionClasses)
                && conflictCourseNames.equals(entry.conflictCourseNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, section, sectionClasses, conflictCourseNames);
    }
}

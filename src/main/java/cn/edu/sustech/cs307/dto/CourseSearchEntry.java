package cn.edu.sustech.cs307.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 课段实体<br>
 * 该实例的意义是用于向即将选课的学生展示可供选择的课程。<br>
 * 它一共包含四个成员变量：<br>
 * {@link #course} 对应课程<br>
 * {@link #section} 对应课段<br>
 * {@link #sectionClasses} 课段的所有学时<br>
 * {@link #conflictCourseNames} 冲突的所有课段的全名描述<br>
 */
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
//        return course.equals(entry.course) && section.equals(entry.section)
//                && sectionClasses.equals(entry.sectionClasses)
//                && conflictCourseNames.equals(entry.conflictCourseNames);
        return Objects.equals(course, entry.course) &&
                Objects.equals(section, entry.section) &&
                Objects.equals(sectionClasses, entry.sectionClasses) &&
                // 最后这个条件判断并不好，也许两个不同的 List 里面的元素相同但因为某种原因造成了位置相错，而导致判断错误。
                // 不过仔细想想，这个判断大可不必，明明 course 和 section 一样，这个课程实例就一样了？
                // 不对，这个描述是没有意义的……按道理来说，这个所谓的课程搜索实体的结果是针对某个学生的，
                // 应该是即用即销毁的玩意，重写它的 equals 方法其实……意义不是那么大。
                Objects.equals(conflictCourseNames, entry.conflictCourseNames);
    }

    /**
     * 用于测试我们的 List 的相等方法重写情况。
     */
    @Test
    public void testListEquals() {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 0);
        List<Integer> other = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            other.add(i);other.add(i);
        }
        System.out.println(list);
        System.out.println(other);
        System.out.println(list.equals(other));
        System.out.println(list.containsAll(other)
        && other.containsAll(list));
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, section, sectionClasses, conflictCourseNames);
    }
}

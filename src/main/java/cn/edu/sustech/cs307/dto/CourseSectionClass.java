package cn.edu.sustech.cs307.dto;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

/**
 * The relationship between {@code CourseSectionClass} with {@code CourseSection} is:
 * One CourseSection usually has two CourseSectionClass
 * the one is theory class, the other is lab class
 */
public class CourseSectionClass {
    public int id;
    public Instructor instructor;
    public DayOfWeek dayOfWeek; // We ensure the test semesters begin with Monday.
    // The given elements in weekList are sorted.
    // CourseSectionClasses in same courseSection may have different week list.
    public Set<Short> weekList;
    // The time quantum of start and end (closed interval).
    // For example: classStart is 3 while classEnd is 4
    public short classBegin, classEnd;
    public String location;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CourseSectionClass that = (CourseSectionClass) o;
        return id == that.id && classBegin == that.classBegin && classEnd == that.classEnd &&
                instructor.equals(that.instructor) && dayOfWeek == that.dayOfWeek && weekList.equals(that.weekList) &&
                location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, instructor, dayOfWeek, weekList, classBegin, classEnd, location);
    }

    @Override
    public String toString() {
        return "CourseSectionClass{" +
                "id=" + id +
                ", instructor=" + instructor +
                ", dayOfWeek=" + dayOfWeek +
                ", weekList=" + weekList +
                ", classBegin=" + classBegin +
                ", classEnd=" + classEnd +
                ", location='" + location + '\'' +
                '}';
    }
}

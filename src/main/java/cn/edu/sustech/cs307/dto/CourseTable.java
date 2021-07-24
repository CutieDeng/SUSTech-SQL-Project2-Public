package cn.edu.sustech.cs307.dto;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;


public class CourseTable {

    public static class CourseTableEntry {
        /**
         * Course full name: String.format("%s[%s]", course.name, section.name)
         */
        public String courseFullName;
        /**
         * The section class's instructor
         */
        public Instructor instructor;
        /**
         * The class's begin and end time (e.g. 3 and 4).
         */
        public short classBegin, classEnd;
        /**
         * The class location.
         */
        public String location;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CourseTableEntry entry = (CourseTableEntry) o;
            return classBegin == entry.classBegin && classEnd == entry.classEnd &&
                    Objects.equals(courseFullName, entry.courseFullName) &&
                    Objects.equals(instructor, entry.instructor) &&
                    Objects.equals(location, entry.location);
        }


        @Override
        public int hashCode() {
            return Objects.hash(courseFullName, instructor, classBegin, classEnd, location);
        }

        @Override
        public String toString() {
            return "CourseTableEntry{" +
                    "courseFullName='" + courseFullName + '\'' +
                    ", instructor=" + instructor +
                    ", classBegin=" + classBegin +
                    ", classEnd=" + classEnd +
                    ", location='" + location + '\'' +
                    '}';
        }
    }




    /**
     * Stores all courses(encapsulated by CourseTableEntry) according to DayOfWeek.
     * The key should always be from MONDAY to SUNDAY, if the student has no course for any of the days, put an empty list.
     */
    public Map<DayOfWeek, Set<CourseTableEntry>> table;

    /**
     * 判断该周课表中是否存在课程。<br>
     * @return True 表示不存在课程。
     */
    public boolean isEmpty() {
        for (DayOfWeek value : DayOfWeek.values()) {
            if (!table.get(value).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断该周课表是否存在冲突课程。
     * @return True 表示存在冲突课程。
     */
    public boolean isConflicted() {
        Set<Integer> timeUsedSet = new HashSet<>();
        AtomicBoolean conflict = new AtomicBoolean(false);
        for (DayOfWeek value : DayOfWeek.values()) {
            for (CourseTableEntry entry : table.get(value)) {
                IntStream.range(entry.classBegin, entry.classEnd+1).forEach(
                        i -> {
                            if (timeUsedSet.contains(i)) {
                                conflict.set(true);
                            }
                            else {
                                timeUsedSet.add(i);
                            }
                        }
                );
                if (conflict.get()) {
                    return true;
                }
            }
            timeUsedSet.clear();
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CourseTable that = (CourseTable) o;
        return table.equals(that.table);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table);
    }

    @Override
    public String toString() {
        return "CourseTable{" +
                "table=" + table +
                '}';
    }
}

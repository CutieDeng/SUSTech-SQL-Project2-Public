package Implementation;

import cn.edu.sustech.cs307.config.Config;
import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.grade.HundredMarkGrade;
import cn.edu.sustech.cs307.dto.grade.PassOrFailGrade;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.SemesterService;
import cn.edu.sustech.cs307.service.StudentService;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@ParametersAreNonnullByDefault
public class StudentServiceImplementation implements StudentService {

    private final String replace = "X";
    private final String original = "-";

    private final static Grade.Cases<String> properStringCase = new Grade.Cases<>() {
        @Override
        public String match(PassOrFailGrade self) {
            return self.name();
        }

        @Override
        public String match(HundredMarkGrade self) {
            return String.valueOf(self.mark);
        }
    };

    /**
     * 计算机默认的初始日期啦： 1970-01-01, 星期四。
     */
    private final static Date originDate = Date.valueOf(LocalDate.EPOCH);

    /**
     * 将学生的相关信息添加入数据库中。<br>
     * @param userId 学生 ID<br>
     *               老师和学生都共同用这一套 ID, 表示人物。
     * @param majorId 专业 ID
     * @param firstName 学生名称
     * @param lastName 学生姓氏
     * @param enrolledDate 加入时间
     * @throws IntegrityViolationException 传入了 NULL 参数，或 sql 语句执行发生错误。
     */
    @Override
    public void addStudent(int userId, int majorId, String firstName, String lastName, Date enrolledDate) {
        if (Objects.isNull(firstName)) {
            throw new IntegrityViolationException("执行 addStudent 参数有误，cause by: firstName = NULL. ");
        }
        if (Objects.isNull(lastName)) {
            throw new IntegrityViolationException("执行 addStudent 参数有误，cause by: lastName = NULL. ");
        }
        if (Objects.isNull(enrolledDate)) {
            throw new IntegrityViolationException("执行 addStudent 参数有误，cause by: enrolledDate = NULL. ");
        }
        String fullName = firstName + lastName;
        boolean alphabet = !fullName.matches(".*[\u4e00-\u9fa5].*");
        if (alphabet) {
            fullName = firstName + " " + lastName;
        }
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT add_student(?, ?, ?, ?, ?, ?)")){
            statement.setInt(1, userId);
            statement.setInt(2, majorId);
            statement.setString(3, firstName);
            statement.setString(4, lastName);
            statement.setString(5, fullName);
            statement.setDate(6, enrolledDate);
            statement.execute();
        } catch (SQLException throwable) {
            // 根据 report 分析，易知发生错误的原因极可能是外键约束导致数据插入错误，直接抛出对应错误即可。
            throw new IntegrityViolationException(throwable.getMessage());
        }
    }

    /**
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
     * @param searchClassTime 匹配课时的对应进行时段<br>
     *             原文：search class time. Matches *any* class in the section contains the search class time.
     * @param searchClassLocations 课程上课的地点<br>
     *                       原文：search class locations.
     *                            Matches *any* class in the section contains *any* location
     *                             from the search class locations.
     * @param searchCourseType 课程类型<br>
     *               原文：search course type. See {@link cn.edu.sustech.cs307.service.StudentService.CourseType}
     * @param ignoreFull 忽略课程没有剩余容量的课段<br>
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
     * @return
     */
    @Override
    public List<CourseSearchEntry> searchCourse(int studentId, int semesterId,
                                                @Nullable String searchCid,
                                                @Nullable String searchName,
                                                @Nullable String searchInstructor,
                                                @Nullable DayOfWeek searchDayOfWeek,
                                                @Nullable Short searchClassTime,
                                                @Nullable List<String> searchClassLocations,
                                                CourseType searchCourseType,
                                                boolean ignoreFull, boolean ignoreConflict,
                                                boolean ignorePassed, boolean ignoreMissingPrerequisites,
                                                int pageSize, int pageIndex) {
        if (Objects.nonNull(searchCid)) {
            searchCid = searchCid.replaceAll("-", "X");
        }
        List<CourseSearchEntry> searchEntries = new ArrayList<>();
        HashMap<Integer, CourseSearchEntry> fastEntry = new HashMap<>();
        int nowPage = 0;
        int nowPageIndex = 0;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM search_course(? :: int, ? :: int," +
                    "? :: varchar, ? :: varchar, ? :: varchar, ? :: varchar," +
                    "? :: smallint, ? :: varchar[], ? :: varchar, " +
                    "? :: bool, ? :: bool, ? :: bool, ? :: bool," +
                    "? :: int, ? :: int);")){
            int nowIndex = 0;
            // SQL 内部 function 签名
            // search_course(student_id int, semester_id int,
            //    searchCid varchar, searchName varchar, searchInstructor varchar, searchDayOfWeek varchar,
            //    searchClassTime smallint, searchClassLocations varchar[],
            //    searchCourseType varchar,
            //    ignoreFull bool, ignoreConflict bool, ignorePassed bool, ignoreMissingPrerequisites bool,
            //    pageSize int, pageIndex int)
            statement.setInt(1, studentId);
            statement.setInt(2, semesterId);
            statement.setString(3, searchCid);
            statement.setString(4, searchName);
            statement.setString(5, searchInstructor);
            if (Objects.isNull(searchDayOfWeek)) {
                statement.setNull(6, Types.VARCHAR);
            } else {
                statement.setString(6, String.valueOf(searchDayOfWeek));
            }
            if (searchClassTime == null) {
                statement.setNull(7, Types.SMALLINT);
            }
            else {
                statement.setShort(7, searchClassTime);
            }
            statement.setArray(8, connection.createArrayOf("varchar", searchClassLocations == null ? null : searchClassLocations.toArray()));
            statement.setString(9, searchCourseType.name());
            statement.setBoolean(10, ignoreFull);
            statement.setBoolean(11, ignoreConflict);
            statement.setBoolean(12, ignorePassed);
            statement.setBoolean(13, ignoreMissingPrerequisites);
            statement.setInt(14, pageSize);
            statement.setInt(15, pageIndex);
            ResultSet set = statement.executeQuery();

            while (set.next()) {

                int sectionIdReg = set.getInt("sectionId");
                String courseIdReg = set.getString("courseId").replaceAll("X", "-");
                String courseNameReg = set.getString("courseName");
                int creditReg = set.getInt("credit");
                int classHourReg = set.getInt("classHour");
                Course.CourseGrading gradingReg = Course.CourseGrading.valueOf(set.getString("grading"));
                String sectionNameReg = set.getString("sectionName");
                int totalCapacityReg = set.getInt("totalCapacity");
                int leftCapacityReg = set.getInt("leftCapacity");
                String[] conflictsReg = (String[]) set.getArray("distinctName").getArray();
                int classId = set.getInt("id");
                int instructorIdReg = set.getInt("instructor");
                String instructorNameReg = set.getString("fullname");
                DayOfWeek dayOfWeekReg = DayOfWeek.valueOf(set.getString("dayOfWeek"));
                Short[] weekListReg = ((Short[]) set.getArray("weekList").getArray());
                short classStartReg = set.getShort("classStart");
                short classEndReg = set.getShort("classEnd");
                String locationReg = set.getString("location");



                CourseSearchEntry entry;
                if (fastEntry.containsKey(sectionIdReg)) {
                    entry = fastEntry.get(sectionIdReg);
                } else {
                    entry = new CourseSearchEntry();
                    fastEntry.put(sectionIdReg, entry);
                    Course course = new Course();
                    course.id = courseIdReg;
                    course.name = courseNameReg;
                    course.credit = creditReg;
                    course.classHour = classHourReg;
                    course.grading = gradingReg;
                    entry.course = course;

                    CourseSection section = new CourseSection();
                    section.id = sectionIdReg;
                    section.name = sectionNameReg;
                    section.totalCapacity = totalCapacityReg;
                    section.leftCapacity = leftCapacityReg;
                    entry.section = section;
                }
                if (entry.conflictCourseNames == null) {
                    entry.conflictCourseNames = new ArrayList<>();
                }
                List<String> strings = entry.conflictCourseNames;
                for (String s : conflictsReg) {
                    if (!strings.contains(s)) {
                        strings.add(s);
                    }
                }
                if (entry.sectionClasses == null) {
                    entry.sectionClasses = new HashSet<>();
                }
                CourseSectionClass courseSectionClass = new CourseSectionClass();
                courseSectionClass.id = classId;

                Instructor instructor = new Instructor();
                instructor.id = instructorIdReg;
                instructor.fullName = instructorNameReg;
                courseSectionClass.instructor = instructor;

                courseSectionClass.dayOfWeek = dayOfWeekReg;
                courseSectionClass.classBegin = classStartReg;
                courseSectionClass.classEnd = classEndReg;
                courseSectionClass.location = locationReg;
                courseSectionClass.weekList = new HashSet<>(weekListReg.length);
                for (Short aShort : weekListReg) {
                    courseSectionClass.weekList.add(aShort);
                }

                entry.sectionClasses.add(courseSectionClass);
                if (!searchEntries.contains(entry)) {
                    searchEntries.add(entry);
                }
            }

            searchEntries.sort(new Comparator<CourseSearchEntry>() {
                @Override
                public int compare(CourseSearchEntry o1, CourseSearchEntry o2) {
                    int i = o1.course.id.compareToIgnoreCase(o2.course.id);
                    if (i != 0) return i;
                    String o1full = o1.course.name + "[" + o1.section.name + "]";
                    String o2full = o1.course.name + "[" + o2.section.name + "]";
                    return o1full.compareTo(o2full);
                }
            });

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        if (pageIndex * pageSize > searchEntries.size()) {
            return List.of();
        }
        int lowIndex = pageIndex * pageSize;
        int highIndex = Math.min(((pageIndex + 1) * pageSize), searchEntries.size());
        for (int i = lowIndex; i < highIndex; i++) {
            searchEntries.get(i).conflictCourseNames.sort(String::compareTo);
        }
        return searchEntries.subList(lowIndex, highIndex);
    }


    /**
     * 为学生选择课程。<br>
     * 八种不同的结果返回情况：<br>
     *
     * {@link StudentService.EnrollResult#COURSE_NOT_FOUND} 找不到相应的课段。<br>
     * {@link StudentService.EnrollResult#ALREADY_ENROLLED} 课段已经选择过了。<br>
     * {@link StudentService.EnrollResult#ALREADY_PASSED} 课程已通过。<br>
     * {@link StudentService.EnrollResult#PREREQUISITES_NOT_FULFILLED} 前置先修课不满足要求。<br>
     * {@link StudentService.EnrollResult#COURSE_CONFLICT_FOUND} 课程发生冲突：课时安排冲突，或选择了重复的课程。<br>
     * {@link StudentService.EnrollResult#COURSE_IS_FULL} 课段容量已满。<br>
     * {@link StudentService.EnrollResult#UNKNOWN_ERROR} 不明的错误发生。
     *
     * @param studentId 学生 ID
     * @param sectionId the id of CourseSection
     * @return 返回选课结果。
     */
    @Override
    public EnrollResult enrollCourse(int studentId, int sectionId) {
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement executeStatement = connection.
                     prepareStatement("SELECT enroll_course(?, ?);")){
            executeStatement.setInt(1, studentId);
            executeStatement.setInt(2, sectionId);
            executeStatement.execute();
        } catch (SQLException throwable) {
            try {
                Matcher matcher = Pattern.compile("错误:\\s+(?<enumName>.+)").matcher(throwable.getMessage());
                if (matcher.find()) {
                    String enumName = matcher.group("enumName");
                    return EnrollResult.valueOf(enumName);
                }
            } catch (IllegalArgumentException exception) {
//                System.out.println(studentId + "\tchoose\t" + sectionId);
//                System.out.println(exception.getMessage());
//                System.out.println("\n");
                // 发生未知的错误，顺便打印相关的递归栈。
                return EnrollResult.UNKNOWN_ERROR;
            }
        }
        return EnrollResult.SUCCESS;
    }

    /**
     * 退课
     * @param studentId 学生 ID
     * @param sectionId 课段 ID
     * @throws IllegalStateException 该课程已经结束
     */
    @Override
    public void dropCourse(int studentId, int sectionId) throws IllegalStateException{
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT drop_selection(?, ?)")){
            statement.setInt(1, studentId);
            statement.setInt(2, sectionId);
            statement.execute();
        } catch (SQLException throwables) {
            if (throwables.getMessage().contains("Finished the section.")) {
                throw new IllegalStateException(throwables.getMessage());
            }
            // 找不到匹配的sectionId 就算了，算我倒霉。
        }
    }

    /**
     * 强制加入学生选择课程(可能带成绩).
     * @param studentId 学生 ID
     * @param sectionId 课段 ID<br>
     *                  原文：We will get the sectionId of one section first
     *                  and then invoke the method by using the sectionId.
     * @param grade     Can be null
     */
    @Override
    public void addEnrolledCourseWithGrade(int studentId, int sectionId, @Nullable Grade grade) {
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT enroll_course_with_grade(?, ?, ? :: varchar);"
            )){
            statement.setInt(1, studentId);
            statement.setInt(2, sectionId);
            if (grade == null) {
                statement.setString(3, null);
            }
            else {
                statement.setString(3, grade.when(properStringCase));
            }
            statement.execute();
        } catch (SQLException ignore) {
        }
    }



    /**
     * 搜索该学生当周目课程表
     * todo: 好多东西要写啊，不想干了。
     * @param studentId 学生 ID
     * @param date 参考日期
     * @return 该学生本周课程表
     */
    @Override
    public CourseTable getCourseTable(int studentId, Date date) {
        CourseTable courseTable =  new CourseTable();
        courseTable.table = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            courseTable.table.put(DayOfWeek.of(i + 1), new HashSet<>());
        }
        // 获取该学生当周的所有课程
        // 1. 查询所有学期
        // 2. 找到对应学期的周目数
        // 3. 找到相应学生在该学期选择的所有课程
        // 4. 找出相应课时
        // 5. 筛选出在该周目上课的所有课时

        // 从数据库中获取所有的学期信息。
        SemesterService semesterService = Config.getServiceFactory().createService(SemesterService.class);
        List<Semester> allSemesters = semesterService.getAllSemesters();
        // 释放空间
        semesterService = null;

        // 获得当前的学期。
        Semester currentSemester = null;
        Iterator<Semester> semesterIterator = allSemesters.iterator();
        while (semesterIterator.hasNext() && currentSemester == null) {
            Semester current = semesterIterator.next();
            if ((!date.before(current.begin)) &&
                    (!date.after(current.end))) {
                currentSemester = current;
            }
        }
        if (currentSemester == null) {
            // 找不到对应学期
            return courseTable;
        }
        // 算出该 date 所对应的周目数。
        int diffDay = diffDay(currentSemester.begin, date);
        int beginDayOfWeek = (diffDay(originDate, currentSemester.begin) + 4) % 7;
        if (beginDayOfWeek == 0) {
            beginDayOfWeek = 7;
        }
        // beginDayOfWeek 的返回值和情况的映射关系：
        // 1: 星期一, ..., 7: 星期日
        diffDay += beginDayOfWeek ;
        // 周目数计算结果
        // 新的优化补充：当开学日期是星期六、星期日时，当时记为第零周，而后才是第一周。
        int week = (diffDay - 1)/ 7;
        if (beginDayOfWeek == 1) {
            week ++;
        }
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM get_course_table(?, ?, ?);")){
            statement.setInt(1, studentId);
            statement.setShort(2, (short) week);
            statement.setInt(3, currentSemester.id);
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                /**
                 * 返回 TABLE 的各列对应类型：
                 *     course_full_name varchar,
                 *     instructor_id int,
                 *     instructor_full_name varchar,
                 *     class_begin smallint,
                 *     class_end smallint,
                 *     location varchar,
                 *     day_of_week int
                 */
                CourseTable.CourseTableEntry entry = new CourseTable.CourseTableEntry();
                entry.courseFullName = set.getString("course_full_name");
                entry.instructor = new Instructor();
                entry.instructor.fullName = set.getString("instructor_full_name");
                entry.instructor.id = set.getInt("instructor_id");
                entry.classBegin = set.getShort("class_begin");
                entry.classEnd = set.getShort("class_end");
                entry.location = set.getString("location");
                DayOfWeek day_of_week;
                try {
                    day_of_week = DayOfWeek.valueOf(set.getString("day_of_week"));
                    courseTable.table.get(day_of_week).add(entry);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (SQLException throwables) {
//            throwables.printStackTrace();
        }
        return courseTable;
    }


    /**
     * 计算两个 Date 之间的日期差
     * @param begin 初始 Date
     * @param end 结束 Date
     * @return 日期差
     */
    private static int diffDay(Date begin, Date end) {
        long diff = ((end.getTime() - begin.getTime()) >>> 10) / 84375L;
        return (int) diff;
    }

}

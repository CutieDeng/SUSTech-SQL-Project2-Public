package Implementation;

import cn.edu.sustech.cs307.config.Config;
import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.grade.HundredMarkGrade;
import cn.edu.sustech.cs307.dto.grade.PassOrFailGrade;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.SemesterService;
import cn.edu.sustech.cs307.service.StudentService;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@SuppressWarnings("all")
@ParametersAreNonnullByDefault
public class StudentServiceImplementation implements StudentService {

    private final String replace = "X";
    private final String original = "-";

    private final static Grade.Cases<String> properStringCase = new Grade.Cases<>() {
        @Override
        public String match(PassOrFailGrade self) {
            if (self == PassOrFailGrade.FAIL) {
                return "F";
            }
            return "P";
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
     * 将学生的相关信息添加入数据库中。
     * @param userId 学生 ID<br>
     *               老师和学生都共同用这一套 ID, 表示人物。
     * @param majorId 专业 ID
     * @param firstName 学生名称
     * @param lastName 学生姓氏
     * @param enrolledDate 加入时间
     */
    @Override
    public void addStudent(int userId, int majorId, String firstName, String lastName, Date enrolledDate) {
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM add_student(?, ?, ?, ?, ? :: DATE);");
             PreparedStatement addUserStatement = connection.prepareStatement("select add_user(?, ?);")){
            String fullName = firstName + lastName;
            boolean alphabet = !fullName.matches(".*[\u4e00-\u9fa5].*");
            if (alphabet) {
                fullName = firstName + " " + lastName;
            }
            addUserStatement.setInt(1, userId);
            addUserStatement.setString(2, fullName);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, majorId);
            preparedStatement.setString(3, firstName);
            preparedStatement.setString(4, lastName);
            preparedStatement.setDate(5, enrolledDate);
            addUserStatement.execute();
            preparedStatement.execute();
        } catch (SQLException throwable) {
            throw new IntegrityViolationException(throwable);
        }
    }


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

        // todo: 搜索

        return List.of();
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

        EnrollResult result = EnrollResult.UNKNOWN_ERROR;

        if (true) {
            return result;
        }

        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement executeStatement = connection.
                     prepareStatement("SELECT * FROM enroll_course(?, ?);")){
            executeStatement.setInt(1, studentId);
            executeStatement.setInt(2, sectionId);
            executeStatement.execute();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        // todo:
        return result;


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
            throw new IllegalStateException(throwables);
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
                    "PERFORM enroll_course_with_grade(?, ?, ? :: varchar);"
            )){
            statement.setInt(1, studentId);;
            statement.setInt(2, sectionId);
            statement.setString(3, grade.when(properStringCase));
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    /**
     * 搜索该学生当周目课程表
     * @param studentId 学生 ID
     * @param date 参考日期
     * @return 该学生本周课程表
     */
    @Override
    public CourseTable getCourseTable(int studentId, Date date) {

        CourseTable courseTable =  new CourseTable();
        courseTable.table = new HashMap<>();
        // todo: 获取该学生当周的所有课程
        // 1. 查询所有学期
        // 2. 找到对应学期的周目数
        // 3. 找到相应学生在该学期选择的所有课程
        // 4. 找出相应课时
        // 5. 筛选出在该周目上课的所有课时

        // 从数据库中获取所有的学期信息。
        SemesterService semesterService = Config.getServiceFactory().createService(SemesterService.class);
        List<Semester> allSemesters = semesterService.getAllSemesters();
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

        // 算出该 date 所对应的周目数。
        int diffDay = diffDay(currentSemester.begin, date);
        int beginDayOfWeek = (diffDay(currentSemester.begin, date) + 4) % 7;
        if (beginDayOfWeek == 0) {
            beginDayOfWeek = 7;
        }
        // beginDayOfWeek 的返回值和情况的映射关系：
        // 1: 星期一, ..., 7: 星期日
        diffDay += beginDayOfWeek - 1;
        // 周目数计算结果
        int week = 1 + (diffDay / 7);

        try (Connection connection = SQLDataSource.getInstance().getSQLConnection()){
            PreparedStatement searchSections = connection.prepareStatement(
                    "SELECT \"sectionId\" FROM student_section WHERE \"studentId\" = ?;"
            );
            searchSections.setInt(1, studentId);
            ResultSet sectionsSelectedSet = searchSections.executeQuery();


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return courseTable;
    }



    private static int diffDay(Date begin, Date end) {
        long diff = ((end.getTime() - begin.getTime()) >>> 10) / 84375L;
        return (int) diff;
    }

}

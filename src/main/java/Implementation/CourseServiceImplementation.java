package Implementation;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.CourseService;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.sql.rowset.serial.SerialArray;
import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("all")
@ParametersAreNonnullByDefault
public class CourseServiceImplementation implements CourseService {

    private final String replace = "X";

    /**
     * 添加一门课程，
     * @param courseId 课程 ID, 比如：CS307
     * @param courseName 课程名
     * @param credit 课程学分
     * @param classHour 课程学时
     * @param grading 课程成绩计算规则
     * @param coursePrerequisite 课程先修要求，可能为 NULL
     */
    @Override
    public void addCourse(String courseId, String courseName,
                          int credit, int classHour,
                          Course.CourseGrading grading,
                          @Nullable Prerequisite coursePrerequisite) {
        courseId = courseId.replace("-",replace);
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select add_Course(?,?,?,?,?)")
        ) {
            stmt.setString(1, courseId);
            stmt.setString(2, courseName);
            stmt.setInt(3, credit);
            stmt.setInt(4, classHour);
            stmt.setString(5, grading.name());
            stmt.execute();
            if(coursePrerequisite != null){
                try (
                        PreparedStatement prerequisite = 
                                connection.prepareStatement("insert into prerequisite (\"courseId\", path, level, \"No\") values (?,text2ltree(?),?,?)")
                ){
                    String path = "Top."+courseId;
                    prerequisite.setString(1,courseId);
                    prerequisite.setString(2,path);
                    prerequisite.setInt(3,2);
                    addPrerequisite(prerequisite,coursePrerequisite,"Top."+courseId,courseId,2,1);
                    prerequisite.executeBatch();
                }
            }

        } catch (SQLException e) {
            throw new IntegrityViolationException(e);
        }
    }

    /**
     * 虽然我不知道这段代码在写啥，但直觉告诉我写得很不好。 -- Cutie Deng
     * // todo: 删掉这段代码。
     * @param preparedStatement 预处理 sql 语句
     * @param prerequisite 课程先修课条件
     * @param path [有人能告诉我这是啥么？]
     * @param courseId 课程 ID
     * @param level [有人能告诉我这是啥么？]
     * @param no [这是啥？]
     */
    public void addPrerequisite(PreparedStatement preparedStatement,
                                Prerequisite prerequisite,
                                String path,
                                String courseId,
                                int level,
                                int no){
        courseId = courseId.replace("-",replace);
        if (prerequisite == null) {
            return;
        }
        level = level + 1;
        if (prerequisite instanceof AndPrerequisite){
            path += (".and"+no);
            int i = 1;
            try {
                preparedStatement.setString(1,courseId);
                preparedStatement.setString(2,path);
                preparedStatement.setInt(3,level);
                preparedStatement.setInt(4, no);
                preparedStatement.addBatch();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            for (Prerequisite tmp : ((AndPrerequisite) prerequisite).terms){
                addPrerequisite(preparedStatement,tmp,path,courseId,level,i);
                i++;
            }
        } else if (prerequisite instanceof OrPrerequisite){
            path += (".or"+no);
            int i = 1;
            try {
                preparedStatement.setString(1,courseId);
                preparedStatement.setString(2,path);
                preparedStatement.setInt(3,level);
                preparedStatement.setInt(4, no);
                preparedStatement.addBatch();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            for (Prerequisite tmp : ((OrPrerequisite) prerequisite).terms){
                addPrerequisite(preparedStatement,tmp,path,courseId,level,i);
                i++;
            }
        } else if (prerequisite instanceof CoursePrerequisite){
            path += ("."+((CoursePrerequisite) prerequisite).courseID);
            try {
                preparedStatement.setString(1,courseId);
                preparedStatement.setString(2,path);
                preparedStatement.setInt(3,level);
                preparedStatement.setInt(4, no);
                preparedStatement.addBatch();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    /**
     * 为课程添加课段。
     * @param courseId 课程 ID
     * @param semesterId 学期 ID
     * @param sectionName 课段名称 {@link cn.edu.sustech.cs307.dto.CourseSection}
     * @param totalCapacity 课段学生容量
     * @return 被添加的课段 ID
     */
    @Override
    public int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity) {
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM add_coursesection(?, ?, ?, ?);"
            )){
            statement.setString(1, courseId);
            statement.setInt(2, semesterId);
            statement.setString(3, sectionName);
            statement.setInt(4, totalCapacity);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getInt("add_coursesection");
            }
        } catch (SQLException throwables) {
            // 不知道怎么处理的错误情形
            throwables.printStackTrace();
        }
        return 0;
    }

    /**
     * 添加课段的具体课时。
     * @param sectionId 课段 id.
     * @param instructorId 教授 id.
     * @param dayOfWeek 星期几的课程。
     * @param weekList 课程活跃的周目情况。
     * @param classStart 当天课程开始的具体时间。
     * @param classEnd 当天课程结束的具体时间。
     * @param location 该课的授课地点。
     * @return 课时的 ID.
     */
    @Override
    public int addCourseSectionClass(int sectionId, int instructorId,
                                     DayOfWeek dayOfWeek, Set<Short> weekList,
                                     short classStart, short classEnd,
                                     String location) {
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM add_coursesectionclass(?, ?, ?, ?, ?, ?, ?)")){
            statement.setInt(1, sectionId);
            statement.setInt(2, instructorId);
            statement.setString(3, dayOfWeek.name());
            statement.setArray(4, connection.createArrayOf("smallint", weekList.toArray()));
            statement.setShort(5, classEnd);
            statement.setShort(6, classEnd);
            statement.setString(7, location);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getInt("add_coursesectionclass");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }


    /**
     * 获得所有课程
     * @return 所有课程形成的列表<br>
     * 如果没有任何课程，会返回一个特殊的 immutable 列表。
     */
    @Override
    public List<Course> getAllCourses() {
        List<Course> result = new ArrayList<>();
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT \"courseId\", \"courseName\", \"credit\", \"classHour\", \"grading\" " +
                             "FROM \"Course\";"
             )){
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                String courseId = set.getString("courseId");
                String courseName = set.getString("courseName");
                int credit = set.getInt("credit");
                int classHour = set.getInt("classHour");
                String grading = set.getString("grading");

                Course course = new Course();
                course.id = courseId;
                course.name = courseName;
                course.credit = credit;
                course.classHour = classHour;
                course.grading = Course.CourseGrading.valueOf(grading);
                result.add(course);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (result.isEmpty()) {
            return List.of();
        }
        return result;
    }
}

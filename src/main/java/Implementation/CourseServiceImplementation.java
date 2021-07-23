package Implementation;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.CourseService;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.*;

@SuppressWarnings("all")
@ParametersAreNonnullByDefault
public class CourseServiceImplementation implements CourseService {

    private final String replace = "X";

    /**
     * 添加一门课程，<br>
     * 这是一个已经被完善好的方法，我们只需要调用即可。<br>
     * 代码由老师赞助提供。
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
     * 添加先修课限制约束实体，private 方法，原则上禁止被外部访问。<br>
     * 这个方法非常有意思，在数据库对先修课进行约束、限制的时候，它采取的策略竟然实例化先修课程约束。<br>
     * 我不太赞同这种想法——它把一个虚无的事物在数据库中表述成一个客观存在的实体，其实不太符合我的准则。<br>
     * 已违反——实体关系描述。<br>
     * 代码由老师提供。
     * @param preparedStatement 预处理 sql 语句
     * @param prerequisite 课程先修课条件
     * @param path 先修课条件路径描述
     * @param courseId 课程 ID
     * @param level 先修课约束树高度
     * @param no [possible] 该层次的约束数目
     */
    private void addPrerequisite(PreparedStatement preparedStatement,
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
     * 为课程对应课段。<br>
     * 当传入参数非法，throw {@link IntegrityViolationException}.
     * @param courseId 课程 ID
     * @param semesterId 学期 ID
     * @param sectionName 课段名称 {@link cn.edu.sustech.cs307.dto.CourseSection}
     * @param totalCapacity 课段学生容量
     * @return 被添加的课段 ID
     */
    @Override
    public int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity) {
        if (Objects.isNull(courseId)) {
            throw new IntegrityViolationException("Course section fails to add, cause by: courseId is null. ");
        }
        if (Objects.isNull(sectionName)) {
            throw new IntegrityViolationException("Course section fails to add, cause by: sectionName is null. ");
        }
        courseId = courseId.replace("-" ,replace);
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT add_course_section(?, ?, ?, ?);"
            )){
            statement.setString(1, courseId);
            statement.setInt(2, semesterId);
            statement.setString(3, sectionName);
            statement.setInt(4, totalCapacity);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getInt("add_course_section");
            }
        } catch (SQLException throwables) {
            // 执行 add_course_section 的过程中，根据笔者的 document&report, 我们可能会发现加入重复课段，即相同(courseId, semesterId, sectionName)情况，
            // 这有可能导致加入课段失败。
            throw new IntegrityViolationException(throwables.getMessage());
        }
        // 其实我没什么理由相信——该程序会执行到这里，所以我决定在这里让它丢出一个运行时错误。
        throw new RuntimeException(String.format(
                "addCourseSection 非预期的执行情况，传入参数：\ncourseId=%s\nsemesterId=%s\nsectionName=%s\ntotalCapacity=%d",
                courseId,
                semesterId,
                sectionName,
                totalCapacity
        ));
    }

    /**
     * 添加课段的具体课时。
     * 如果有非法的参数传入——丢出 {@link IntegrityViolationException}.
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
        // 首先，任何一门具体课时，都要有明确的 dayOfWeek, weekList 和 location 描述，同时 weekList 应当非空。
        // 违背该条件均直接引起错误发生。
        if (Objects.isNull(dayOfWeek)) {
            throw new IntegrityViolationException("加入 course section class 时发生错误，cause by: dayOfWeek = NULL.");
        }
        if (Objects.isNull(weekList)) {
            throw new IntegrityViolationException("执行 addCourseSectionClass 时发生错误，cause by: weekList = NULL. ");
        }
        if (weekList.isEmpty()) {
            throw new IntegrityViolationException("执行 addCourseSectionClass 时违反数据约束，cause by: weekList.size = 0. ");
        }
        if (Objects.isNull(location)) {
            throw new IntegrityViolationException("执行 addCourseSectionClass 时违反约束，cause by: location = NULL. ");
        }

        // classStart 应当小于等于 classEnd, 同时满足它们均为正数。
        if (classStart <= 0) {
            throw new IntegrityViolationException("执行 addCourseSectionClass 时违反约束，cause by: classStart <= 0");
        }
        if (classEnd <= 0) {
            throw new IntegrityViolationException("执行 addCourseSectionClass 时违反约束，cause by: classEnd <= 0");
        }
        if (classStart > classEnd) {
            throw new IntegrityViolationException(String.format(
                    "执行 addCourseSectionClass 时违反约束，cause by: classStart(%d) > classEnd(%d). ", classStart, classEnd
            ));
        }
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT add_course_section_class(?, ?, ?, ?, ?, ?, ?)")){
            statement.setInt(1, sectionId);
            statement.setInt(2, instructorId);
            statement.setString(3, dayOfWeek.name());
            statement.setArray(4, connection.createArrayOf("smallint", weekList.toArray()));
            statement.setShort(5, classEnd);
            statement.setShort(6, classEnd);
            statement.setString(7, location);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getInt("add_course_section_class");
            }
        } catch (SQLException exception) {
            throw new IntegrityViolationException(exception.getMessage());
        }
        // 理论上应该跑不到这里来吧？
        // 丢个特殊错误用以检查吧。
        throw new RuntimeException(
                String.format(
                        "非预期的 addCourseSectionClass 方法执行，相关 parameters:\n" +
                                "sectionId = %d\tinstructorId = %d\n" +
                                "dayOfWeek = %s\tweekList = %s\n" +
                                "classStart = %d\tclassEnd = %d\n" +
                                "location = %s\n",
                        sectionId, instructorId, dayOfWeek, weekList, classStart, classEnd, location
                )
        );
    }


    /**
     * 获得所有课程。<br>
     * 这方法写得好裸啊！
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

package Implementation;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.dto.Semester;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.SemesterService;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@ParametersAreNonnullByDefault
public class SemesterServiceImplementation implements SemesterService {
    @Override
    public int addSemester(String name, Date begin, Date end) {
        int result = 0;
        if (begin.after(end)) {
            // 存疑：学期持续一天？不考虑给个学期最短时间限制么？ -- Cutie Deng
            throw new IntegrityViolationException();
        }
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from add_Semester(?,?,?)")
        ) {
            stmt.setString(1, name);
            stmt.setDate(2, begin);
            stmt.setDate(3, end);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()){
                result = resultSet.getInt("add_Semester");
            }
        } catch (SQLException ignored) {
        }
        return result;
    }

    @Override
    public void removeSemester(int semesterId) {
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from remove_Semester(?)")
        ) {
            stmt.setInt(1, semesterId);
            stmt.execute();
        } catch (SQLException e) {
            throw new EntityNotFoundException(e);
        }
    }

    @Override
    public List<Semester> getAllSemesters() {
        ResultSet resultSet;
        List<Semester> result = new ArrayList<>();

        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from get_all_Semester()")
        ) {
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Semester tmp = new Semester();
                tmp.id = resultSet.getInt("id_out");
                tmp.name = resultSet.getString("semester_name");
                tmp.begin = resultSet.getDate("begin_");
                tmp.end = resultSet.getDate("end_");
                result.add(tmp);
            }
        } catch (SQLException ignored) {
        }
        if (result.isEmpty()) {
            return List.of();
        }
        return result;

    }

}

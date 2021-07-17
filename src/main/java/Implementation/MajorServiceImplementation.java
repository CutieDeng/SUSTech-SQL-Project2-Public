package Implementation;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.dto.Major;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.MajorService;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class MajorServiceImplementation implements MajorService {
    private final String replace = "X";

    @Override
    public int addMajor(String name, int departmentId) {
        int result = 0;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from add_Major(?,?)")
        ) {
            stmt.setString(1, name);
            stmt.setInt(2, departmentId);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()){
                result = resultSet.getInt("add_Major");
            }
        } catch (SQLException e) {
            throw new IntegrityViolationException(e);
        }
        return result;
    }

    @Override
    public void addMajorCompulsoryCourse(int majorId, String courseId) {
        courseId = courseId.replace("-",replace);
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from add_Major_Compulsory_Course(?,?)")
        ) {
            stmt.setInt(1, majorId);
            stmt.setString(2, courseId);
            stmt.execute();
        } catch (SQLException e) {
            throw new IntegrityViolationException(e);
        }
    }

    @Override
    public void addMajorElectiveCourse(int majorId, String courseId) {
        courseId = courseId.replace("-",replace);
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from add_Major_Elective_Course(?,?)")
        ) {
            stmt.setInt(1, majorId);
            stmt.setString(2, courseId);
            stmt.execute();
        } catch (SQLException e) {
            throw new IntegrityViolationException(e);
        }
    }
}

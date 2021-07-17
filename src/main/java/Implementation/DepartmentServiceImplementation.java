package Implementation;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.DepartmentService;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 写的不错。 -- Cutie Deng.
 */
@ParametersAreNonnullByDefault
public class DepartmentServiceImplementation implements DepartmentService {

    @Override
    public int addDepartment(String name) {
        int result = 0;
        ResultSet resultSet;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from add_Department(?);")
        ) {
            stmt.setString(1, name);
            resultSet = stmt.executeQuery();
            while (resultSet.next()){
                result = resultSet.getInt("add_Department");
            }
        } catch (SQLException e) {
            throw new IntegrityViolationException(e);
        }
        return result;
    }

    @Override
    public void removeDepartment(int departmentId) {
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from remove_Department(?)")
        ) {
            stmt.setInt(1, departmentId);
            stmt.execute();
        } catch (SQLException e) {
            throw new EntityNotFoundException(e);
        }
    }

    @Override
    public List<Department> getAllDepartments() {

        List<Department> result = new ArrayList<>();
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from getalldepartments()")
        ) {
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Department tmp = new Department();
                tmp.id = resultSet.getInt("departmentId_out");
                tmp.name = resultSet.getString("name_out");
                result.add(tmp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(result.isEmpty()){return List.of();}
        return result;
    }

}

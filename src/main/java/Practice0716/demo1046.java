package Practice0716;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@SuppressWarnings("all")
public class demo1046 {
    public static void main(String[] args) throws PropertyVetoException {
//        System.out.println(1);
        System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        cpds.setDriverClass( "org.postgresql.Driver" ); //loads the jdbc driver
        cpds.setJdbcUrl( "jdbc:postgresql://localhost/demo01" );
        cpds.setUser("tem1149");
        cpds.setPassword("123456");
//        cpds.setPassword("dbpassword");
    }
}

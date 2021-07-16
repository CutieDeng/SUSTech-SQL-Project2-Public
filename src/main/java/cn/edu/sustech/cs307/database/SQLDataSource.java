package cn.edu.sustech.cs307.database;

import cn.edu.sustech.cs307.config.Config;
import com.zaxxer.hikari.HikariDataSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQLDataSource 类是一个描述对数据库连接的一个类，同时也是一个禁止被继承的类。<br>
 *
 */
@ParametersAreNonnullByDefault
public final class SQLDataSource implements Closeable {
    /**
     * SQLDataSource 单例，使用懒汉式单例模式，在类被加载的时候便完成该初始化过程。
     */
    private static final SQLDataSource INSTANCE = new SQLDataSource();

    /**
     * 最大连接池容量，缺省值。
     */
    private static final int MAXIMUM_POOL_SIZE_DEFAULT = 16;

    /**
     * 连接池描述。
     */
    private HikariDataSource dataSource;

    /**
     * public 的获取单例方法，通过该方法可以获取该类的唯一实例。
     * @return {@link SQLDataSource} 的唯一实例。
     */
    public static SQLDataSource getInstance() {
        return INSTANCE;
    }

    /**
     * private 构造器，同时也是类的唯一构造器，不允许被轻易调用。<br>
     * 构造方法会调用 {@link Config} 的相关访问器方法，这些方法会获取 properties 中的相关信息，
     * 以便于创造出对应的 JDBC 连接池。<br>
     * 具体见 {@link SQLDataSource#configureSQLServer(String jcbdUrl, String 用户名, String 用户密码)}.
     */
    private SQLDataSource() {
        configureSQLServer(Config.getJdbcUrl(), Config.getSQLUsername(), Config.getSQLPassword());
    }

    /**
     * public 更改器方法。<br>
     * 这是一个配置 SQLServer 相关信息的方法，在真正进行数据库连接之前，我们必须先关闭
     * 先前原有的连接，这部分内容并不是原有代码有的，笔者在此追加。<br>
     * 该方法实际上还在内部调用了另一个特殊的方法(其实我想把它移到参数上的，但是想想，关于
     * maximum pool size 的参数也并没有非常重要，提高它的可移植性不是一件特别有必要的事情。)<br>
     * 但除此之外，我还是把这部分的自定义功能预留给了我们的 Config, 使得 Config 在有必要的情况下，可以调控
     * 我们的连接池情况。<br>
     * 因此，如果在 Config 中描述了方法 getMaximumPoolSize(), 对应的参数将会被获取。
     * 这个方法由于是 public, 的其他三个参数可以被我们特别地调用，这意味着我们可以对某一个 SQLDataSource 实例的行为
     * 进行更加自由地调整更改。<br>
     * 尽管原则上我们并不提倡使用 String 去存储我们的密码，但由于前任已经做了这个不太聪明的决定，我们便只能
     * 稍稍路径依赖，跟从这个方向继续前行。<br>
     * 但如果给我更好的选择，我会放弃这个方法，把它描述成 deprecated.<br>
     * @param jdbcUrl jdbc url.
     * @param username 对于某个确定的 url 描述地数据库，用户姓名。
     * @param password 对于该数据库，用户密码。
     */
    public void configureSQLServer(String jdbcUrl, String username, String password) {
        close();
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        int maximumPoolSize = MAXIMUM_POOL_SIZE_DEFAULT;
        try {
            Method getSize = Config.class.getDeclaredMethod("getMaximumPoolSize");
            String maximumString = (String) getSize.invoke(Config.class);
            if (maximumString != null) {
                maximumPoolSize = Integer.parseInt(maximumString);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
                | RuntimeException ignore) {
        }
        dataSource.setMaximumPoolSize(maximumPoolSize);
    }

    /**
     * 该方法将会获取相关的数据库连接。
     * @return 配置描述的数据库的具体连接。
     * @throws SQLException 暂时还不清楚的错误发生。todo: 等待补充。
     * @throws NullPointerException 该实例的连接池属性还未初始化，或已经被关闭。
     */
    public Connection getSQLConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * public 更改器方法。<br>
     * 关闭 dataSource, 尽管我还不知道 dataSource 是什么类型。<br>
     * 笔者在此追加了一点判定：我们只有在 dataSource 建立连接了，我们才
     * 真正尝试对它进行关闭。<br>
     * 在关闭后，我们会试着释放相应的内存空间。<br>
     */
    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}

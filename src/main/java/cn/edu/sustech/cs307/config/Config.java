package cn.edu.sustech.cs307.config;

import cn.edu.sustech.cs307.factory.ServiceFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Config 类是一个管理 project 相关所有设置而使用的类，它暴露的方法如下：<br>
 * {@link Config#searchFiles(String 待匹配字符串)} 用于搜索相对路径下所有与待匹配字符串相吻合的文件，不包括文件夹。<br>
 * {@link Config#getJdbcUrl()} 获取项目的 jdbcUrl.<br>
 * {@link Config#getSQLUsername()} 获取我们配置信息中的账号名称。<br>
 * {@link Config#getSQLPassword()} 获取我们配置信息中的用户密码。<br>
 * {@link Config#getServiceFactory()} 获得提供服务的工厂类实例。值得注意的是，
 * 你可以通过描述一个 newInstance() 的静态方法，来获得它的合法实例。(如果你在重写该方法的时候，并没有获得一个合法的实例，那么
 * 这个特殊方法会被忽略。)
 */
public final class Config {
    /**
     * Project 运行的所有属性描述。
     */
    private static final Properties properties = new Properties();

    /**
     * Config 类的日志管理器，负责记录 configuration 载入的相关信息。
     */
    private static final Logger configLogger = Logger.getLogger("ConfigLogger");

    /**
     * 属性文件名。
     */
    private static final String propertiesFileName = "config.properties";

    static {
        AtomicBoolean fail = new AtomicBoolean(false);
        configLogger.fine("Config 类开始进行类加载。");
        File propertiesFile = new File(propertiesFileName);
        if (!propertiesFile.exists()) {
            List<File> files = searchFiles(propertiesFileName);
            if (files.isEmpty()) {
                fail.set(true);
            }
            files.stream().map(file -> {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException ignore) {
                    // 不可能会找不到该文件，此处异常不做处理。
                }
                return null;
            }).forEach(inStream -> {
                try {
                    if (inStream != null) {
                        properties.load(inStream);
                    }
                } catch (IOException e) {
                    configLogger.severe(
                            String.format("加载 %s 发生异常，异常信息：%s\n递归栈描述：%s", inStream,
                                    e.getMessage(),
                                    Arrays.toString(e.getStackTrace())
                            )
                    );
                    fail.set(true);
                }
            });
        }
        else {
            try {
                properties.load(new FileInputStream(propertiesFile));
            } catch (IOException e) {
                configLogger.severe(
                        String.format("加载 %s[文件]发生异常，异常信息：%s", propertiesFile.getName(),
                                e.getMessage()
                        )
                );
                fail.set(true);
            }
        }

    }

    /**
     * 文件搜索，可以搜索出满足要求文件名的所有相关文件。(不包含文件夹)
     * @param fileNameMatchingString 文件名要求。
     * @return 满足要求的所有文件列表。
     */
    public static List<File> searchFiles(String fileNameMatchingString) {
        File nowPlace = new File(".\\");
        if (nowPlace.exists() && nowPlace.isDirectory()) {
            return searchFilesRecursively(nowPlace, fileNameMatchingString);
        }
        return new ArrayList<>();
    }

    /**
     * 递归文件搜索。
     * @param nowFile 当前正在搜索的文件夹。
     * @param fileNameMatchingString 需要比较匹配的文件名，支持正则表达式。
     * @return 当前文件夹下符合要求的文件名匹配的所有文件形成的一个列表。
     */
    private static List<File> searchFilesRecursively(File nowFile, String fileNameMatchingString) {
        final List<File> result = new ArrayList<>();
        final File[] subFiles = nowFile.listFiles();
        assert subFiles != null;
        for (File subFile : subFiles) {
            if (subFile.isDirectory()) {
                result.addAll(searchFilesRecursively(subFile, fileNameMatchingString));
            }
            else if (subFile.isFile()) {
                if (subFile.getName().matches(fileNameMatchingString)) {
                    result.add(subFile);
                }
            }
        }
        return result;
    }


    /**
     * 这是一个静态的访问器方法，将会尝试调用属性中的 serviceFactory key 的 value, <br>
     * 并同时获取它的默认构造器，并调用它的实例。<br>
     * 这是一种非常糟糕的方法实现方式。<br>
     * 因此我尝试给出另一种实现方式，在这个类中如果有出现 getInstance() 的静态方法，
     * 我会优先调用这个方法；否则，调用默认构造器的构造函数。<br>
     * 我希望实现相关内容的编程者，都能够留意这个细节，并尽可能地保持单例的习惯，而不是
     * 贸然、非法地通过反射轻易构造新的实例。<br>
     * 感谢配合。<br>
     * 除此之外，该方法会轻易地抛出一个 RuntimeException 当它遇到任何异常的时候。
     * @return 返回 properties 中的 Service Factory 类。
     * @throws RuntimeException 遇到任何异常，都将捕获并转化成运行时异常。
     */
    public static ServiceFactory getServiceFactory() {
        try {
            Class<?> serviceFactory = Class.forName(properties.getProperty("serviceFactory"));
            Method getInstance = null;
            try {
                getInstance = serviceFactory.getMethod("getInstance");
            } catch (NoSuchMethodException ignore) {
            }
            if (getInstance != null) {
                Object result = getInstance.invoke(serviceFactory);
                if (result instanceof ServiceFactory) {
                    return (ServiceFactory) result;
                }
            }
            return (ServiceFactory) serviceFactory.
                    getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从属性列表中获取对应的属性，这是一个私有的方法，不应该被轻易地暴露和调用。
     * @param property 要获取的属性字段。
     * @return 询问的属性值。
     */
    private static String getProperty(String property) {
        configLogger.fine(
                String.format("执行 %s 方法中，正在获取属性 %s.",
                        Thread.currentThread().getStackTrace()[2].getMethodName(),
                        property
                )
        );
        String defaultKey = property;
        boolean findOtherUrl = false;
        if (!properties.containsKey(property)) {
            configLogger.info(String.format("找不到属性 %s, 正在尝试进行模糊匹配。",
                    property));
            Enumeration<Object> keys = properties.keys();
            Iterator<Object> keyIterator = keys.asIterator();
            defaultKey = defaultKey.toLowerCase();
            while (keyIterator.hasNext() && (!findOtherUrl)) {
                Object object = keyIterator.next();
                if (!(object instanceof String)) {
                    continue;
                }
                String actualObject = (String)object;
                if (Objects.equals(actualObject.toLowerCase(), defaultKey.toLowerCase())) {
                    defaultKey = actualObject;
                    findOtherUrl = true;
                    configLogger.info(String.format("属性 %s 模糊匹配成功，匹配项为：%s",
                            property, defaultKey));
                }
            }
            if (!findOtherUrl) {
                configLogger.warning(String.format("属性 %s 获取失败！", property));
            }
        }
        return properties.getProperty(defaultKey);
    }

    /**
     * 获取 jdbcUrl.
     * @return 配置文件信息中的 jdbcUrl.
     */
    public static String getJdbcUrl() {
        return getProperty("jdbcUrl");
    }

    /**
     * 获取 username, 用以连接数据库。
     * @return 使用者姓名 username.
     */
    public static String getSQLUsername() {
        return getProperty("username");
    }

    /**
     * 获取 用户密码。
     * @return 密码字符串。
     */
    public static String getSQLPassword() {
        return getProperty("password");
    }

    /**
     * 从文件中获取连接池的最大容量。
     * @return 连接池最大容量。
     */
    public static String getMaximumPoolSize() {
        return getProperty("maximumPoolSize");
    }
}

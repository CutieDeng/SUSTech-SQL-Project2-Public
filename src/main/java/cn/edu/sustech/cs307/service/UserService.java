package cn.edu.sustech.cs307.service;

import cn.edu.sustech.cs307.dto.User;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * User Service 是一个用户接口<br>
 * 我也不知道是用来干什么的。<br>
 * todo: 告诉我这是用来干啥的。
 */
@ParametersAreNonnullByDefault
public interface UserService {
    /**
     * 根据 userID 移除一个用户。
     * @param userId 用户 ID.
     */
    void removeUser(int userId);

    /**
     * 获得所有用户。
     * @return 一个列表，存储所有用户。
     */
    List<User> getAllUsers();

    /**
     * 根据 userID 获取一个用户。
     * @param userId 用户 ID.
     * @return 对应的用户实例对象。
     */
    User getUser(int userId);
}

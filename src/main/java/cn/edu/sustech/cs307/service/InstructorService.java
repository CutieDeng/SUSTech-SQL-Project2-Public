package cn.edu.sustech.cs307.service;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Instructor Service 是一个提供教师相关服务的接口。<br>
 * 里面提供了一个方法：<br>
 * {@link #addInstructor(int userId, String firstName, String lastName)} 加入一个新的教师，
 * 分别传入它的名称和姓氏作为参数。
 */
@ParametersAreNonnullByDefault
public interface InstructorService {
    /**
     * 加入一个新的教师。
     * @param userId 新教师 id.
     * @param firstName 新教师的名称。
     * @param lastName 新教师的姓氏。
     */
    void addInstructor(int userId, String firstName, String lastName);
}

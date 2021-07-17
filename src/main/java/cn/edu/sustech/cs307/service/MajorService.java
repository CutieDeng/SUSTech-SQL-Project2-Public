package cn.edu.sustech.cs307.service;

import cn.edu.sustech.cs307.dto.Major;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Major Service 提供专业的相关设置服务，包括为学科设置选修课和必修课两种类型。<br>
 * 提供的方法如下：<br>
 * {@link #addMajor(String 专业名称, int 相关学科部门 ID)} 添加一个新专业。<br>
 * {@link #addMajorCompulsoryCourse(int 专业 ID, String 课程名)} 为专业设置一门必修课。<br>
 * {@link #addMajorElectiveCourse(int 专业 ID, String 课程名)} 为专业设置一门选修课。<br>
 */
@ParametersAreNonnullByDefault
public interface MajorService {
    /**
     * 添加一个专业。
     * @param name 专业名称。
     * @param departmentId 相关的部门 ID.
     * @return 专业 ID.
     */
    int addMajor(String name, int departmentId);

    /**
     * 为一个专业设置一门必修课。
     * 原文：<br>
     * Binding a course id {@code courseId} to major id {@code majorId}, and the selection is compulsory.
     * @param majorId the id of major
     * @param courseId the course id
     */
    void addMajorCompulsoryCourse(int majorId, String courseId);

    /**
     * 为一个专业设置一门选修课。
     * 原文：<br>
     * Binding a course id{@code courseId} to major id {@code majorId}, and the selection is elective.
     * @param majorId the id of major
     * @param courseId the course id
     */
    void addMajorElectiveCourse(int majorId, String courseId);
}

package cn.edu.sustech.cs307.service;

import cn.edu.sustech.cs307.dto.Department;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * DepartmentService 接口是对部门设定提供服务的接口。<br>
 * 里面提供了三个 public 方法，均不是 default 的：<br>
 * {@link DepartmentService#addDepartment(String 部门名称)} 加入一个新的学科部门。<br>
 * {@link DepartmentService#removeDepartment(int 待移除的部门的ID)} 移除一个学科部门。<br>
 * {@link DepartmentService#getAllDepartments()} 获取所有学科部门。
 */
@ParametersAreNonnullByDefault
public interface DepartmentService {
    /**
     * 如果加入了一个已经存在的重名部门，丢出一个错误 {@link cn.edu.sustech.cs307.exception.IntegrityViolationException}. <br>
     * 原文：<br>
     *  if adding a new department which has the same name with an existing department,
     *  it should throw an {@code IntegrityViolationException}
     * @param name 学科部门名称
     * @return 返回新加入的部门的 departmentId.
     */
    int addDepartment(String name);

    /**
     * 根据 departmentId, 移除一个已经存在的部门。
     * @param departmentId 待移除部门的 id.
     */
    void removeDepartment(int departmentId);

    /**
     * 获取所有部门。
     * @return 获得所有部门，以列表形式呈现。
     */
    List<Department> getAllDepartments();

}

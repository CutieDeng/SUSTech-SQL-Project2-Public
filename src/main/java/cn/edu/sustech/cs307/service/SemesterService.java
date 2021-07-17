package cn.edu.sustech.cs307.service;

import cn.edu.sustech.cs307.dto.Semester;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Date;
import java.util.List;

/**
 * Semester Service 学期服务接口<br>
 *
 */
@ParametersAreNonnullByDefault
public interface SemesterService {
    /**
     * 添加一个学期<br>
     * 如果传入的参数非法，则丢出错误 {@link cn.edu.sustech.cs307.exception.IntegrityViolationException}. <br>
     * 原文：<br>
     * Add one semester according to following parameters:
     * If some of parameters are invalid, throw {@link cn.edu.sustech.cs307.exception.IntegrityViolationException}
     * @param name 学期名。
     * @param begin 学期开始日期。
     * @param end 学期结束日期。
     * @return the Semester id of new inserted line, if adding process is successful.
     */
    int addSemester(String name, Date begin, Date end);

    /**
     * 移除一个学期<br>
     * 相关的课段和学生的课程选择记录也相应移除。<br>
     * 原文：<br>
     *To remove an entity from the system, related entities dependent on this entity
     *  (usually rows referencing the row to remove through foreign keys in a relational database) shall be removed together.
     *
     * More specifically, when remove a semester, the related select course record should be removed accordingly.
     * @param semesterId 待移除的学期 ID.
     */
    void removeSemester(int semesterId);

    /**
     * 获得所有学期。
     * @return 列表，由学期的元素组成。
     */
    List<Semester> getAllSemesters();

}

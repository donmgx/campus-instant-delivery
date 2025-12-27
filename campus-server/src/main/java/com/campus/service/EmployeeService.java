package com.campus.service;

import com.campus.dto.EmployeeDTO;
import com.campus.dto.EmployeeLoginDTO;
import com.campus.dto.EmployeePageQueryDTO;
import com.campus.dto.PasswordEditDTO;
import com.campus.entity.Employee;
import com.campus.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /*
    * 新增员工
    * */
    void save(EmployeeDTO employeeDTO);

    /*
     * 员工分页查询
     * */
    PageResult selectPage(EmployeePageQueryDTO employeePageQueryDTO);

    /*
    * 启用禁用员工
    * */
    void startOrStop(Integer status, Long id);

    /*
     * 根据id查询员工
     * */
    Employee selectById(Long id);

    /*
     * 编辑员工
     * */
    void updateEmp(EmployeeDTO employeeDTO);


    /*
     * 修改密码
     * */
    void editPassword(PasswordEditDTO passwordEditDTO);

}

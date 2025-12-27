package com.campus.service.impl;

import com.campus.context.BaseContext;
import com.campus.dto.PasswordEditDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.campus.constant.MessageConstant;
import com.campus.constant.PasswordConstant;
import com.campus.constant.StatusConstant;
import com.campus.dto.EmployeeDTO;
import com.campus.dto.EmployeeLoginDTO;
import com.campus.dto.EmployeePageQueryDTO;
import com.campus.entity.Employee;
import com.campus.exception.AccountLockedException;
import com.campus.exception.AccountNotFoundException;
import com.campus.exception.PasswordErrorException;
import com.campus.mapper.EmployeeMapper;
import com.campus.result.PageResult;
import com.campus.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /*
     * 新增员工
     * */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //转换为实体类对象
        Employee employee = new Employee();

        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置dto没有的其他值
        //设置帐号状态
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码,默认密码，要加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置创建时间，更新时间
        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());*/

        /*//设置当前记录 创建人的id 和 修改人id
        Long currentId = BaseContext.getCurrentId();
        employee.setCreateUser(currentId);
        employee.setUpdateUser(currentId);*/

        //插入到数据库中
        employeeMapper.insert(employee);

    }

    /*
     * 员工分页查询
     * */
    @Override
    public PageResult selectPage(EmployeePageQueryDTO employeePageQueryDTO) {//page页码, pageSize每页显示记录数
        //传统方法
        /*PageResult pageResult = new PageResult();

        //查询总记录数
        pageResult.setTotal(employeeMapper.selectCount());

        // 起始索引 = （查询页码 - 1）* 每页显示记录数
        //页码
        int page = employeePageQueryDTO.getPage();

        //每页显示记录数
        int pageSize = employeePageQueryDTO.getPageSize();

        //起始索引
        int beginIndex = (page - 1) *pageSize;*/

        //开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<Employee> page = employeeMapper.selectPage(employeePageQueryDTO);

        /*//查询当前数据集
        pageResult.setRecords(employees);*/

        long total = page.getTotal();
        List<Employee> result = page.getResult();

        return new PageResult(total, result);
    }

    /*
     * 启用禁用员工
     * */
    @Override
    public void startOrStop(Integer status, Long id) {
        /*Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);*/

        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }

    /*
     * 根据id查询员工
     * */
    @Override
    public Employee selectById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        employee.setPassword("******");
        return employee;
    }

    /*
     * 编辑员工
     * */
    @Override
    public void updateEmp(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        /*employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());*/

        employeeMapper.update(employee);
    }


    /*
     * 修改密码
     * */
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        //判断新旧密码是否一致
        if (passwordEditDTO.getNewPassword().equals(passwordEditDTO.getOldPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_CAN_NOT_SAME);
        }
        Long currentId = BaseContext.getCurrentId();
        //如果不相同,查数据库看旧密码是否正确
        String oldPassword = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        Employee employee = employeeMapper.selectById(currentId);
        if (!oldPassword.equals(employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        //可以修改密码
        employeeMapper.update(Employee.builder()
                .id(currentId)
                .password(DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes()))
                .build());

    }

}

package com.campus.service.impl;

import com.campus.constant.JwtClaimsConstant;
import com.campus.context.BaseContext;
import com.campus.dto.*;
import com.campus.properties.JwtProperties;
import com.campus.security.LoginUser;
import com.campus.utils.JwtUtil;
import com.campus.vo.EmployeeLoginVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.campus.constant.MessageConstant;
import com.campus.constant.PasswordConstant;
import com.campus.constant.StatusConstant;
import com.campus.entity.Employee;
import com.campus.exception.AccountLockedException;
import com.campus.exception.PasswordErrorException;
import com.campus.mapper.EmployeeMapper;
import com.campus.result.PageResult;
import com.campus.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private PasswordEncoder passwordEncoder; //注入BCrypt加密器
    @Autowired
    private AuthenticationManager authenticationManager;  //注入认证管理器
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    @Qualifier("myRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;
    private Employee employee;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO) {

        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //使用 AuthenticationManager 进行认证
        //会自动调用 UserDetailsServiceImpl.loadUserByUsername ，并自动校验密码
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        //如果认证通过，获取UserDetails
        if (authenticate == null) {
            throw new RuntimeException(MessageConstant.LOGIN_FAILED);
        }
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        Employee employee = loginUser.getEmployee();

        //校验状态，双重保障
        if (employee.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //生成JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        loginUser.setToken(token);
        LoginMemberCacheDTO loginMemberCacheDTO = new LoginMemberCacheDTO(employee, loginUser.getPermissions(), token);
        //将完整的登录信息(包含权限)存入Redis，Key为 "login_" + userId
        String redisKey = "login_" + employee.getId();

        try {

            // 1. 临时创建一个 ObjectMapper
            redisTemplate.delete(redisKey);
            redisTemplate.opsForValue().set(redisKey, loginMemberCacheDTO, jwtProperties.getAdminTtl(), TimeUnit.MINUTES);

        } catch (Exception e) {
            e.printStackTrace(); // 打印错误堆栈
            throw new RuntimeException("登录信息存入Redis失败");
        }

        //3、返回实体对象
        return employeeLoginVO;
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

        //设置密码,默认密码，BCrypt加密
        employee.setPassword(passwordEncoder.encode(PasswordConstant.DEFAULT_PASSWORD));

        //设置角色
        employee.setRoleId(2);

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
        String oldPassword = passwordEditDTO.getOldPassword();
        //判断用户输入的新旧密码是否一致
        if (passwordEditDTO.getNewPassword().equals(oldPassword)) {
            //相同
            throw new PasswordErrorException(MessageConstant.PASSWORD_CAN_NOT_SAME);
        }

        Long currentId = BaseContext.getCurrentId();
        //如果不相同,查数据库看旧密码是否正确
        Employee employee = employeeMapper.selectById(currentId);
        if (!passwordEncoder.matches(oldPassword, employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        //可以修改密码
        employeeMapper.update(Employee.builder()
                .id(currentId)
                .status(StatusConstant.ENABLE)
                .password(passwordEncoder.encode(passwordEditDTO.getNewPassword()))
                .build());

        redisTemplate.delete("login_" + currentId);

    }

}

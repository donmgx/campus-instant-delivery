package com.campus.service.impl;

import com.campus.constant.MessageConstant;
import com.campus.entity.Employee;
import com.campus.mapper.EmployeeMapper;
import com.campus.mapper.PermissionMapper;
import com.campus.mapper.RoleMapper;
import com.campus.security.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义加载用户详情逻辑
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //1 .获取用户信息
        Employee employee = employeeMapper.getByUsername(username);
        if (employee == null) {
            //如果employee为空，代表用户不存在
            throw new UsernameNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //2 .查询权限信息
        //获取所有权限字符串
        List<String> permissions = permissionMapper.selectPermissionByRoleId(employee.getRoleId());
        //查询角色
        List<String> roles = roleMapper.selectCodeByRoleId(employee.getRoleId());

        // 4 .将角色和权限合并到一个集合中
        List<String> combinedAuthorities = new ArrayList<>();

        //处理角色：自动添加 ROLE_ 前缀
        if (roles != null && !roles.isEmpty()) {
            roles.forEach(role -> combinedAuthorities.add("ROLE_" + role));
        }

        //处理权限：直接添加
        if (permissions != null && !permissions.isEmpty()) {
            combinedAuthorities.addAll(permissions);
        }

        return new LoginUser(employee, combinedAuthorities);
    }
}

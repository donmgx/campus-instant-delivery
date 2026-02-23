package com.campus.dto;

import com.campus.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 专门用于 Redis 存储的纯净 DTO
 * 不包含任何 Spring Security 的逻辑，只存数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginMemberCacheDTO implements Serializable {
    // 存员工基本信息
    private Employee employee;

    // 存权限字符串列表 (纯 String，不要存 GrantedAuthority)
    private List<String> permissions;

    // 存 Token 用于比对
    private String token;
}
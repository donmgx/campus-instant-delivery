package com.campus.security;

import com.campus.entity.Employee;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/*
 * spring security 用来
 * 封装当前登录用户的信息
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
// 核心：忽略掉从 UserDetails 继承来的 getAuthorities 等方法，防止序列化冲突
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser implements UserDetails {

    private Employee employee;

    private List<String> permissions;  //从数据库查出来的-原始权限列表 和 角色

    private String token;

    // 1：必须给 authorities 加 @JsonIgnore，因为它无法被序列化
    @JsonIgnore
    private List<SimpleGrantedAuthority> authorities; //存储Spring Security所需要的权限信息，缓存转换后的 GrantedAuthority 对象

    public LoginUser(Employee employee, List<String> permissions) {
        this.employee = employee;
        this.permissions = permissions;
    }

    // 2：在获取权限时动态转换，这样 Redis 里只存 permissions 字符串列表，不存 authorities 对象
    @JsonIgnore // 忽略这个 Getter 的序列化
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities != null) {
            return authorities;
        }
        if (permissions != null) {
            authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return employee.getPassword();
    }

    @Override
    public String getUsername() {
        return employee.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return employee.getStatus() != null && employee.getStatus() == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return employee.getStatus() != null && employee.getStatus() == 1;
    }
}


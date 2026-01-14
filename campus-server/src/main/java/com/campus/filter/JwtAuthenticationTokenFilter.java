package com.campus.filter;

import com.campus.constant.JwtClaimsConstant;
import com.campus.context.BaseContext;
import com.campus.dto.LoginMemberCacheDTO;
import com.campus.properties.JwtProperties;
import com.campus.security.LoginUser;
import com.campus.utils.JwtUtil;
import io.jsonwebtoken.Claims;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;


/*
 * JWT 认证过滤器
 * 解析 Header 中的 Token
 * ，查 Redis 确认用户是否登录
 * ，然后将用户信息放入 SecurityContext
 * */
@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    @Qualifier("myRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 获取请求路径
        String requestURI = request.getRequestURI();

        //如果是登录接口，直接放行，不检查 Token，也不查 Redis
        //避免旧 Token 触发 Redis 脏数据报错
        if (requestURI.contains("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        //1 .获取token
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        //检查token是否为空或不存在
        if (!StringUtils.hasText(token)) {
            //若果没有，代表登录，放行 ( 后续 security 会拦截 )
            filterChain.doFilter(request, response);
            return;
        }

        //2 .如果 token不为空 ，解析token

        String userId;
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            userId = claims.get(JwtClaimsConstant.EMP_ID).toString();
        } catch (Exception e) {
            //token 过期 或 非法
            log.error("token解析失败: {}", e.getMessage());
            //放行，交给 Security 的 统一异常处理机制 AuthenticationEntryPoint 处理
            filterChain.doFilter(request, response);
            return;
        }

        // 3 .从 redis获取用户信息
        //构造redis所需的key
        LoginMemberCacheDTO loginMemberCacheDTO;
        String redisKey = "login_" + userId;
        try {

            loginMemberCacheDTO = (LoginMemberCacheDTO) redisTemplate.opsForValue().get(redisKey);

        } catch (Exception e) {
            log.error("从 Redis 获取用户信息失败: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (Objects.isNull(loginMemberCacheDTO) || !token.equals(loginMemberCacheDTO.getToken())) {
            //Redis中的token过期 或对不上
            redisTemplate.delete(redisKey);
            //放行
            filterChain.doFilter(request, response);
            return;
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setEmployee(loginMemberCacheDTO.getEmployee());
        loginUser.setPermissions(loginMemberCacheDTO.getPermissions());
        loginUser.setToken(loginMemberCacheDTO.getToken());
        //redis中有该token

        //4 .存入SecurityContextHolder
        //以此告诉Spring Security当前是谁在操作
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        BaseContext.setCurrentId(loginUser.getEmployee().getId());
        // 5. 放行
        filterChain.doFilter(request, response);
    }
}

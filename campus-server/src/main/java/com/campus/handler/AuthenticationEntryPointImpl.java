package com.campus.handler;

import com.alibaba.fastjson.JSON;
import com.campus.constant.MessageConstant;
import com.campus.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 *    认证失败处理类
 * */
@Component
@Slf4j
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.info("认证失败处理");
        if (authException != null) {
            log.error("认证失败原因：", authException); //打印具体异常
        }
        //设置响应状态码401
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(Result.error(MessageConstant.AUTHENTICATION_FAILED)));
    }
}

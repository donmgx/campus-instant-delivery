package com.campus.interceptor;

import com.campus.annotation.AutoIdempotent;
import com.campus.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/*
*
*
* */
@Component
@Slf4j
public class IdempotentInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断handler是否为Controller
        if (!(handler instanceof HandlerMethod)) {
            //不是Controller放行
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        AutoIdempotent annotation = method.getAnnotation(AutoIdempotent.class);
        if (annotation != null) {
            //如果有该注解
            String idempotentToken = request.getHeader("idempotentToken");
            try {
                return tokenService.checkToken(idempotentToken);
            } catch (Exception e) {
                throw e;
            }
        }

        return true;

    }
}

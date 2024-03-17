package com.sharecampus.utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断是否需要拦截
        if (UserHolder.getUser() == null) {
            log.debug("没有用户登录");
            response.setStatus(401);
            return false;
        }
        return true;
    }
}

package com.sharecampus.config;


import com.sharecampus.utils.LoginInterceptor;
import com.sharecampus.utils.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/user/register",
                        "/blog/hot",
                        "/venue/**",
                        "/voucher/**",
                        "/venue-type/**",
                        "/upload/**"
                        // "/voucher-order/**"
                ).order(1);
        // token刷新的拦截器
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/user/register",
                        "/blog/hot",
                        "/venue/**",
                        "/voucher/**",
                        "/venue-type/**",
                        "/upload/**"
                        // "/voucher-order/**"
                ).order(0);
    }
}

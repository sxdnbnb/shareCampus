package com.sharecampus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sharecampus.dto.LoginFormDTO;
import com.sharecampus.dto.RegisterFormDTO;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();

    Result register(RegisterFormDTO registerForm);

    Result logout(String token);
}

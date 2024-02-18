package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result sendCode(String phone, HttpSession session){
        //1. 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            //if 不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        //符合，生成验证码
         String code = RandomUtil.randomNumbers(6);
        //保存验证码到redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //session.setAttribute("code",code);
        //发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        //返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session){
        // 1.校验手机号
        String phone = loginForm.getPhone();
        log.debug("手机号码为：{}", phone);
        if (RegexUtils.isPhoneInvalid(phone)){
            //if 不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 2.校验验证码
        //Object cachecode = session.getAttribute("code");
        String cachecode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY+phone);
        log.debug("Redis中验证码为：{}", cachecode);
        String code = loginForm.getCode();
        if (cachecode == null || !cachecode.equals(code)){
            //3.不一致，报错
            return Result.fail("验证码错误");
        }
        //根据手机号查询用户
        User user = query().eq("phone",phone).one();

        //判断用户是否存在
        if (user == null){
            //不存在，创建用户
            user = createUserWithPhone(phone);
        }
        log.debug("用户为：{}", user.getNickName());
        //保存信息到redis
        //  随机生成token作为登录令牌、User转为Hash存储、存储、返回token到客户端
        String token = UUID.randomUUID().toString(true);
        String user_token = LOGIN_USER_KEY+token;
        log.debug("登录令牌为：{}", token);
        //session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        UserDTO userDTO = BeanUtil.copyProperties(user,UserDTO.class);

        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO);
        userMap.forEach((key, value) -> {
            if (null != value) userMap.put(key, String.valueOf(value));
        });

        log.debug("UserTDO：{}", userMap);
        stringRedisTemplate.opsForHash().putAll(user_token,userMap);
        log.debug("token 存储成功");
        //设置有效期
        stringRedisTemplate.expire(user_token,LOGIN_USER_TTL,TimeUnit.MINUTES);
        return Result.ok(token);
    }

    @Override
    public Result sign() {
        UserDTO userDTO = UserHolder.getUser();
        if (userDTO == null){
            return Result.fail("请先登录");
        }
        Long userId = userDTO.getId();
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyymm"));
        String key = USER_SIGN_KEY+userId+keySuffix;
        int dayOfMonth = now.getDayOfMonth();
        stringRedisTemplate.opsForValue().setBit(key,dayOfMonth-1,true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        int dayOfMonth = now.getDayOfMonth();
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result == null || result.isEmpty()){
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num == null || num == 0){
            return Result.ok(0);
        }
        int cnt = 0;
        while (num>0){
            if ((num&1)==0){
                break;
            }else{
                cnt++;
            }
            num >>>=1;
        }
        return Result.ok(cnt);
    }

    private  User createUserWithPhone(String phone){
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        save(user);
        return null;
    }

}

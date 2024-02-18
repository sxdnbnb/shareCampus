package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserServiceImpl userService;
    @Override
    public Result follow(Long folloeUserId, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        String key = "follows:"+ userId;
        if (isFollow){
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(folloeUserId);
            boolean ok = save(follow);
            if (ok){
                stringRedisTemplate.opsForSet().add(key,folloeUserId.toString());
            }
        }else{
            boolean ok = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", folloeUserId));
            if (ok){
                stringRedisTemplate.opsForSet().remove(key,folloeUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long folloeUserId) {
        Long userId = UserHolder.getUser().getId();
        Integer count = query().eq("user_id", userId).eq("follow_user_id", folloeUserId).count();

        return Result.ok(count>0);
    }

    @Override
    public Result followCommons(Long targetId) {
        Long userId = UserHolder.getUser().getId();
        String sKey = "follows:" + userId;
        String tkey = "follows:" + targetId;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(tkey, sKey);
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> userDTOS = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(userDTOS);
    }
}

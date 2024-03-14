package com.sharecampus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.Follow;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IFollowService extends IService<Follow> {

    Result follow(Long folloeUserId, Boolean isFollow);

    Result isFollow(Long folloeUserId);

    Result followCommons(Long id);
}

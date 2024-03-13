package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;
import com.hmdp.entity.Venue;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IVenueService extends IService<Venue> {

    Result queryById(Long id);
    Result updateByIdWithCache(Venue venue);
    Result deleteByIdWithCache(Venue venue);
    Result queryVenueByType(Integer typeId, Integer current);
}

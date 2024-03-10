package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;
import com.hmdp.entity.Venue;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVenueService extends IService<Venue> {

    Result queryById(Long id);
    Result updateByIdWithCache(Venue venue);

    Result queryVenueByType(Integer typeId, Integer current, Double x, Double y);
}

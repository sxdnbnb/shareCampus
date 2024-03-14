package com.sharecampus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.Venue;

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

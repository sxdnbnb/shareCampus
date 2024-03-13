package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;
import com.hmdp.entity.VenueType;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IVenueTypeService extends IService<VenueType> {
    Result queryList();
}

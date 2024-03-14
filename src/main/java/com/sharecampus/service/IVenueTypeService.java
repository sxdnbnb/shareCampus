package com.sharecampus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.VenueType;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IVenueTypeService extends IService<VenueType> {
    Result queryList();
}

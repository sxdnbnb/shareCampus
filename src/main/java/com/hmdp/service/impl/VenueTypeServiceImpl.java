package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.VenueType;
import com.hmdp.mapper.VenueTypeMapper;
import com.hmdp.service.IVenueTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class VenueTypeServiceImpl extends ServiceImpl<VenueTypeMapper, VenueType> implements IVenueTypeService {

    @Override
    public Result queryList() {
        List<VenueType> typeList = this.query().orderByAsc("sort").list();
        List<String> venueTypeList = new ArrayList<>();
        for (VenueType type: typeList){
            venueTypeList.add(JSONUtil.toJsonStr(type));
        }
        return Result.ok(typeList);
    }
}

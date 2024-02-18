package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
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
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryList() {
        String key = "ShopTypeList";
        List<String> shopTypeListString = stringRedisTemplate.opsForList().range(key,0,-1);
        if (shopTypeListString!=null && shopTypeListString.size()>0){
            List<ShopType> typeList = new ArrayList<>();
            for (String shoptype: shopTypeListString){
                typeList.add(JSONUtil.toBean(shoptype,ShopType.class));
            }
            return Result.ok(typeList);
        }
        List<ShopType> typeList = this.query().orderByAsc("sort").list();
        List<String> shopTypeList = new ArrayList<>();
        for (ShopType type: typeList){
            shopTypeList.add(JSONUtil.toJsonStr(type));
        }
        stringRedisTemplate.opsForList().rightPushAll(key,shopTypeList);
        stringRedisTemplate.expire(key,30L, TimeUnit.MINUTES);
        return Result.ok(typeList);
    }
}

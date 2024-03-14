package com.sharecampus.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.Venue;
import com.sharecampus.mapper.VenueMapper;
import com.sharecampus.service.IVenueService;
import com.sharecampus.utils.SystemConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
public class VenueServiceImpl extends ServiceImpl<VenueMapper, Venue> implements IVenueService {
    @Override
    public Result queryById(Long id) {
        Venue venue = getById(id);
        log.debug("venue:" + venue);
        if (venue == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(venue);
    }


    @Override
    @Transactional
    public Result updateByIdWithCache(Venue venue) {
        Long id = venue.getId();
        if (id == null) {
            return Result.fail("没有该场所");
        }
        // 更新数据库
        updateById(venue);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result deleteByIdWithCache(Venue venue) {
        Long id = venue.getId();
        if (id == null) {
            return Result.fail("没有该场所");
        }
        // 删除数据库
        removeById(venue);
        return Result.ok();
    }

    @Override
    public Result queryVenueByType(Integer typeId, Integer current) {
        Page<Venue> page = query()
                .eq("type_id", typeId)
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }
}

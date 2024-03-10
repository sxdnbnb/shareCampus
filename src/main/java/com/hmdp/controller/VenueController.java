package com.hmdp.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.entity.Venue;
import com.hmdp.service.IVenueService;
import com.hmdp.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/venue")
public class VenueController {

    @Resource
    public IVenueService venueService;

    /**
     * 根据id查询场所信息
     * @param id 场所id
     * @return 场所详情数据
     */
    @GetMapping("/{id}")
    public Result queryvenueById(@PathVariable("id") Long id) {
        return venueService.queryById(id);
    }

    /**
     * 新增场所信息
     * @param venue 场所数据
     * @return 场所id
     */
    @PostMapping
    public Result savevenue(@RequestBody Venue venue) {
        // 写入数据库
        venueService.save(venue);
        // 返回店铺id
        return Result.ok(venue.getId());
    }

    /**
     * 更新场所信息
     * @param venue 场所数据
     * @return 无
     */
    @PutMapping
    public Result updatevenue(@RequestBody Venue venue) {
        // 写入数据库
        //venueService.updateById(venue);
        return venueService.updateByIdWithCache(venue);
    }

    /**
     * 根据场所类型分页查询场所信息
     * @param typeId 场所类型
     * @param current 页码
     * @return 场所列表
     */
    @GetMapping("/of/type")
    public Result queryVenueByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y
    ) {
        return venueService.queryVenueByType(typeId, current, x,y);
    }

    /**
     * 根据场所名称关键字分页查询场所信息
     * @param name 场所名称关键字
     * @param current 页码
     * @return 场所列表
     */
    @GetMapping("/of/name")
    public Result queryvenueByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Venue> page = venueService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }

}

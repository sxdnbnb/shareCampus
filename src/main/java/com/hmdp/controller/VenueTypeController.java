package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.entity.VenueType;
import com.hmdp.service.IVenueTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 */
@RestController
@RequestMapping("/venue-type")
public class VenueTypeController {
    @Resource
    private IVenueTypeService typeService;
    /*
    @GetMapping("list")
    public Result queryTypeList() {
        List<ShopType> typeList = typeService
                .query().orderByAsc("sort").list();
        return Result.ok(typeList);
    }*/
    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryList();
    }
}

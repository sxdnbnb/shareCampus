package com.sharecampus.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.Venue;
import com.sharecampus.entity.Voucher;
import com.sharecampus.service.IVoucherService;
import com.sharecampus.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private IVoucherService voucherService;

    /**
     * 新增场馆券
     *
     * @param voucher 场馆券信息
     * @return 场馆券id
     */
    @PostMapping("seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        return voucherService.addSeckillVoucher(voucher);
    }

    /**
     * 查询场馆券列表
     *
     * @param title 场馆券名称
     * @return 场馆券id
     */
    @GetMapping("/of/title")
    public Result queryVoucher(@RequestParam(value = "title", required = false) String title,
                               @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return voucherService.queryVoucher(title, current);
    }
}

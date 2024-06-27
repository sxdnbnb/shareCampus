package com.sharecampus.controller;


import com.sharecampus.dto.Result;
import com.sharecampus.service.IVoucherOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    /**
     * 抢场馆券
     *
     * @param voucherId 场馆券
     * @return 订单id
     */
    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId){
        return voucherOrderService.seckillVoucher(voucherId);
    }
}

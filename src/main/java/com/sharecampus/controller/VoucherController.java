package com.sharecampus.controller;


import com.sharecampus.dto.Result;
import com.sharecampus.entity.Venue;
import com.sharecampus.entity.Voucher;
import com.sharecampus.service.IVoucherService;
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
     * @param voucher 场馆券信息
     * @return 场馆券id
     */
    @PostMapping
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        return voucherService.addSeckillVoucher(voucher);
    }

    /**
     * 查询场馆券信息
     *
     * @param id 场馆券id
     * @return 场馆券信息
     */
    @GetMapping("/{id}")
    public Result queryVoucher(@PathVariable("id") Long id) {
        return voucherService.queryVoucher(id);
    }

    /**
     * 删除场馆券信息
     * @param voucher 场馆券数据
     * @return 无
     */
    @DeleteMapping
    public Result deleteVouvher(@RequestBody Voucher voucher){
        return voucherService.deleteVoucher(voucher);
    }

    /**
     * 更新场馆券信息
     * @param voucher 场馆券数据
     * @return 无
     */
    @PutMapping
    public Result updateVoucher(@RequestBody Voucher voucher) {
        return voucherService.updateVoucher(voucher);
    }
}

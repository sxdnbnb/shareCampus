package com.sharecampus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.VoucherOrder;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);
    void createVoucherOrder(VoucherOrder voucherId);
}

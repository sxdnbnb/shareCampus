package com.sharecampus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.Voucher;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IVoucherService extends IService<Voucher> {

    Result queryVoucher(Long id);
    Result deleteVoucher(Voucher voucher);
    Result updateVoucher(Voucher voucher);
    Result addSeckillVoucher(Voucher voucher);
}

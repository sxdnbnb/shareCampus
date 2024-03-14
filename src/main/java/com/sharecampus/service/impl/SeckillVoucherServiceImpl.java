package com.sharecampus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharecampus.entity.SeckillVoucher;
import com.sharecampus.mapper.SeckillVoucherMapper;
import com.sharecampus.service.ISeckillVoucherService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

}

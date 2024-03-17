package com.sharecampus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.SeckillVoucher;
import com.sharecampus.entity.Voucher;
import com.sharecampus.mapper.VoucherMapper;
import com.sharecampus.service.ISeckillVoucherService;
import com.sharecampus.service.IVoucherService;
import com.sharecampus.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存场馆券
        save(voucher);
        //保存场馆券库存到redis中
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY +voucher.getId(), voucher.getStock().toString());
        //保存秒杀开始时间到redis中
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_BRGIN_Time_KEY +voucher.getId(), voucher.getBeginTime().toString());
        //保存秒杀结束时间到redis中
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_END_Time_KEY +voucher.getId(), voucher.getEndTime().toString());
    }
}

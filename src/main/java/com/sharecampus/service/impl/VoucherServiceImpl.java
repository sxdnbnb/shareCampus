package com.sharecampus.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.Voucher;
import com.sharecampus.mapper.VoucherMapper;
import com.sharecampus.service.IVoucherService;
import com.sharecampus.utils.RedisConstants;
import com.sharecampus.utils.SystemConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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
    public Result queryVoucher(String title, Integer current) {
        // 根据名称分页查询
        Page<Voucher> page = query()
                .like(StrUtil.isNotBlank(title), "title", title)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }

    @Override
    @Transactional
    public Result addSeckillVoucher(Voucher voucher) {
        // 保存场馆券
        save(voucher);
        //保存场馆券库存到redis中
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY +voucher.getId(), voucher.getStock().toString());
        //保存秒杀开始时间到redis中
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_BRGIN_Time_KEY +voucher.getId(), voucher.getBeginTime().toString());
        //保存秒杀结束时间到redis中
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_END_Time_KEY +voucher.getId(), voucher.getEndTime().toString());
        return Result.ok(voucher.getId());
    }
}

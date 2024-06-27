package com.sharecampus.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.Voucher;
import com.sharecampus.mapper.VoucherMapper;
import com.sharecampus.service.IVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.sharecampus.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Slf4j
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucher(Long id) {
        String key = CACHE_Voucher_KEY + id;
        // 1.从redis查询
        String voucherJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(voucherJson)) {
            // 3.存在，直接返回
            Voucher voucher = JSONUtil.toBean(voucherJson, Voucher.class);
            return Result.ok(voucher);
        }
        // 判断命中的是否是空值
        if (voucherJson != null) {
            // 返回一个错误信息
            return Result.fail("场馆券信息不存在！");
        }

        // 4.不存在，根据id查询数据库
        Voucher voucher = getById(id);
        // 5.不存在，返回错误
        if (voucher == null) {
            // 将空值写入redis
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 返回错误信息
            return Result.fail("场馆券信息不存在！");
        }
        // 6.存在，写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(voucher), CACHE_Voucher_TTL, TimeUnit.MINUTES);
        // 7.返回
        return Result.ok(voucher);
    }

    @Override
    @Transactional
    public Result deleteVoucher(Voucher voucher) {
        Long id = voucher.getId();
        if (id == null) {
            return Result.fail("没有该场所");
        }
        // 1.删除数据库
        removeById(voucher);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_Voucher_KEY + id);
        stringRedisTemplate.delete(SECKILL_STOCK_KEY + id);
        stringRedisTemplate.delete(SECKILL_BRGIN_Time_KEY + id);
        stringRedisTemplate.delete(SECKILL_END_Time_KEY + id);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result updateVoucher(Voucher voucher) {
        Long id = voucher.getId();
        if (id == null) {
            return Result.fail("场馆券id不能为空");
        }
        // 1.更新数据库
        updateById(voucher);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_Voucher_KEY + id);
        // 3.更新秒杀信息
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + id, voucher.getStock().toString());
        stringRedisTemplate.opsForValue().set(SECKILL_BRGIN_Time_KEY + id, voucher.getBeginTime().format(formatter));
        stringRedisTemplate.opsForValue().set(SECKILL_END_Time_KEY + id, voucher.getEndTime().format(formatter));

        return Result.ok();
    }

    @Override
    @Transactional
    public Result addSeckillVoucher(Voucher voucher) {
        // 保存场馆券
        save(voucher);
        Long id = voucher.getId();
        // 保存场馆券库存到redis中
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + id, voucher.getStock().toString());
        // 保存秒杀开始时间到redis中
        stringRedisTemplate.opsForValue().set(SECKILL_BRGIN_Time_KEY + id, voucher.getBeginTime().format(formatter));
        // 保存秒杀结束时间到redis中
        stringRedisTemplate.opsForValue().set(SECKILL_END_Time_KEY + id, voucher.getEndTime().format(formatter));
        return Result.ok(voucher.getId());
    }
}

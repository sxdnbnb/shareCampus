package com.sharecampus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharecampus.dto.Result;
import com.sharecampus.entity.VoucherOrder;
import com.sharecampus.mapper.VoucherOrderMapper;
import com.sharecampus.rabbitmq.MqPublisher;
import com.sharecampus.service.IVoucherOrderService;
import com.sharecampus.utils.RedisIdWorker;
import com.sharecampus.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.sharecampus.utils.RedisConstants.formatter;


/**
 * <p>
 * 服务实现类
 * </p>
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {


    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MqPublisher mqPublisher;

    @Resource
    private RedissonClient redissonClient;

    // lua脚本
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        // 1.判断下单时间是否符合规则
        // 1.1 获取场馆券发放时间
        String beginTimeString = stringRedisTemplate.opsForValue().get("seckill:begin_time:" + voucherId);
        String endTimeString = stringRedisTemplate.opsForValue().get("seckill:end_time:" + voucherId);

        // 1.2 判断场馆券发放时间是否正确
        if (beginTimeString == null || endTimeString == null) {
            return Result.fail("场馆券发放时间有误");
        }
        LocalDateTime beginTime = LocalDateTime.parse(beginTimeString, formatter);
        LocalDateTime endTime = LocalDateTime.parse(endTimeString, formatter);
        log.info("场馆券发放时间：" + beginTime.format(formatter) + "-----" + endTime.format(formatter));

        // 1.3 获取当前时间
        LocalDateTime nowTime = LocalDateTime.now();
        log.info("当前时间：" + nowTime.format(formatter));

        // 1.4 返回错误信息
        if (nowTime.isBefore(beginTime) || nowTime.isAfter(endTime)) {
            return Result.fail("没到场馆券的发放时间");
        }

        // 2.时间符合，开始准备抢单
        log.info("准备抢单");
        // 2.1 获取用户id
        Long userId = UserHolder.getUser().getId();
        log.info("当前下单用户为：" + userId);

        // 2.2 执行lua脚本实现创建订单业务
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString()
        );

        // 2.3 判断结果是否为0
        int r = result.intValue();
        if (r != 0) {
            // 不为0代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }

        // 2.4 为0代表有购买资格, 直接生成订单信息并保存到阻塞队列
        VoucherOrder voucherOrder = new VoucherOrder();
        // 生成订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);

        // 2.5将信息放入MQ中
        mqPublisher.sendSeckillMessage(voucherOrder);

        // 2.6 返回订单id
        return Result.ok(orderId);

    }

}
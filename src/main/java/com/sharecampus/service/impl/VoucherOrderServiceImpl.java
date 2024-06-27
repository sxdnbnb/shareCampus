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
import java.time.format.DateTimeFormatter;
import java.util.Collections;


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

    // private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();
    //
    // @PostConstruct
    // private void init(){
    //     // TODO 需要秒杀下单功能的同学自己解开下面的注释
    //     SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    // }
    //
    // private class VoucherOrderHandler implements Runnable{
    //     private final String queueName = "stream.orders";
    //     @Override
    //     public void run() {
    //         while (true){
    //             // 获取队列中订单信息
    //             try {
    //                 // 0.初始化stream
    //                 initStream();
    //                 // 1、获取消息队列中的订单信息 XREADGROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS streams.order >
    //                 List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
    //                         Consumer.from("g1", "c1"),
    //                         StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
    //                         StreamOffset.create(queueName, ReadOffset.lastConsumed())
    //                 );
    //                 // 2. 判断消息获取是否成功
    //                 // 2.1 获取失败。没有消息，继续下一次循环
    //                 if (list == null || list.isEmpty()){
    //                     continue;
    //                 }
    //                 // 2.2 有消息继续下单
    //                 MapRecord<String, Object, Object> record = list.get(0);
    //                 Map<Object, Object> values = record.getValue();
    //                 VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
    //                 // 3. 创建订单
    //                 handleVoucherOrder(voucherOrder);
    //                 // 4. ACk确认
    //                 stringRedisTemplate.opsForStream().acknowledge(queueName,"g1",record.getId());
    //             } catch (Exception e) {
    //                 log.error("处理订单异常", e);
    //                 handlePendingList();
    //             }
    //         }
    //     }
    //     public void initStream(){
    //         Boolean exists = stringRedisTemplate.hasKey(queueName);
    //         if (BooleanUtil.isFalse(exists)) {
    //             log.info("stream不存在，开始创建stream");
    //             // 不存在，需要创建
    //             stringRedisTemplate.opsForStream().createGroup(queueName, ReadOffset.latest(), "g1");
    //             log.info("stream和group创建完毕");
    //             return;
    //         }
    //         // stream存在，判断group是否存在
    //         StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups(queueName);
    //         if(groups.isEmpty()){
    //             log.info("group不存在，开始创建group");
    //             // group不存在，创建group
    //             stringRedisTemplate.opsForStream().createGroup(queueName, ReadOffset.latest(), "g1");
    //             log.info("group创建完毕");
    //         }
    //     }
    //
    //     private void handlePendingList(){
    //         while (true){
    //             // 获取队列中订单信息
    //             try {
    //                 // 1、获取pending-list中的订单信息 XREADGROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS streams.order >
    //                 List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
    //                         Consumer.from("g1", "c1"),
    //                         StreamReadOptions.empty().count(1),
    //                         StreamOffset.create(queueName, ReadOffset.from("0"))
    //                 );
    //                 // 2. 判断消息获取是否成功
    //                 // 2.1 获取失败。pendding-list没有消息，继续下一次循环
    //                 if (list == null || list.isEmpty()){
    //                     break;
    //                 }
    //                 // 2.2 有消息继续下单
    //                 MapRecord<String, Object, Object> record = list.get(0);
    //                 Map<Object, Object> values = record.getValue();
    //                 VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
    //                 // 3. 创建订单
    //                 handleVoucherOrder(voucherOrder);
    //                 // 4. ACk确认
    //                 stringRedisTemplate.opsForStream().acknowledge(queueName,"g1",record.getId());
    //             }catch (Exception e) {
    //                 log.error("处理订单异常", e);
    //             }
    //         }
    //     }
    //
    // }
    // private void handleVoucherOrder(VoucherOrder voucherOrder) {
    //     Long userId = voucherOrder.getUserId();
    //     RLock lock = redissonClient.getLock("lock:order:" + userId);
    //     boolean islock = lock.tryLock();
    //     if (!islock){
    //         log.error("一人只能下一单");
    //         return;
    //     }
    //     try{
    //         //获取代理对象（事务）
    //         Long voucherId = voucherOrder.getVoucherId();
    //         proxy.createVoucherOrder(voucherOrder);
    //     } finally {
    //         lock.unlock();
    //     }
    // }

    // private IVoucherOrderService proxy;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime beginTime = LocalDateTime.parse(beginTimeString, formatter);
        LocalDateTime endTime = LocalDateTime.parse(endTimeString, formatter);
        log.info("场馆券发放时间：" + beginTime + "-----" + endTime);

        // 1.3 获取当前时间
        LocalDateTime nowTime = LocalDateTime.now();
        log.info("当前时间：" + nowTime);

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

    // @Transactional
    // public void createVoucherOrder(VoucherOrder voucherOrder) {
    //     Long userId = voucherOrder.getId();
    //
    //     // 一人一单
    //     int count = query().eq("user_id", userId).eq("voucher_id", voucherOrder).count();
    //     if (count > 0) {
    //         log.error("用户已经购买过一次");
    //         return;
    //     }
    //     Long voucherId = voucherOrder.getVoucherId();
    //     // 扣减库存
    //     boolean success = seckillVoucherService.update()
    //             .setSql("stock = stock-1")
    //             .eq("voucher_id", voucherId)
    //             .gt("stock", 0).update();
    //     if (!success) {
    //         log.error("库存不足");
    //         return;
    //     }
    //     save(voucherOrder);
    //     log.info("订单创建成功");
    //
    // }

}
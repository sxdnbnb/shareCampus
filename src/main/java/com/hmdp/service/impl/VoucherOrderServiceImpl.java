package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

//    @Override
//    public Result seckillVoucher(Long voucherId) throws InterruptedException {
//
//        //查询优惠券
//        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
//        //判断秒杀是否开始
//        if (voucher.getBeginTime().isAfter(LocalDateTime.now())){
//            return Result.fail("尚未开始");
//        }
//        //判断秒杀是否结束
//        if (voucher.getEndTime().isBefore(LocalDateTime.now())){
//            return Result.fail("已经结束");
//        }
//        //判断库存是否充足
//        if (voucher.getStock()<1){
//            return Result.fail("库存不足");
//        }
//
//        Long userId = UserHolder.getUser().getId();
//        //SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
//        RLock lock = redissonClient.getLock("lock:order:" + userId);
//        boolean islock = lock.tryLock(1L, TimeUnit.SECONDS);
//        if (!islock){
//            return Result.fail("一人只能下一单");
//        }
//        try{
//            //获取代理对象（事务）
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            return proxy.createVoucherOrder(voucherId);
//        } finally {
//            lock.unlock();
//        }
//
//    }

//    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024*1024);
    private static final ExecutorService SECKILL_SERVICE_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init(){
        SECKILL_SERVICE_EXECUTOR.submit(new VoucherOrderHandler());
    }

//    private class VoucherOrderHandler implements Runnable{
//
//        @Override
//        public void run() {
//            while (true){
//                // 获取队列中订单信息
//                try {
//                    VoucherOrder voucherOrder = orderTasks.take();
//                    handleVoucherOrder(voucherOrder);
//                } catch (Exception e) {
//                    log.error("处理订单异常", e);
//                }
//                // 创建订单
//            }
//        }
//
//
//    }
    private class VoucherOrderHandler implements Runnable{
        String queueName = "stream.orders";
        @Override
        public void run() {
            while (true){
                // 获取队列中订单信息
                try {
                    // 1、获取消息队列中的订单信息 XREADGROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS streams.order >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );
                    // 2. 判断消息获取是否成功
                    // 2.1 获取失败。没有消息，继续下一次循环
                    if (list == null || list.isEmpty()){
                        continue;
                    }
                    // 2.2 有消息继续下单
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    // 3. 创建订单
                    handleVoucherOrder(voucherOrder);
                    // 4. ACk确认
                    stringRedisTemplate.opsForStream().acknowledge(queueName,"g1",record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    handlePendingList();
                }
            }
        }
        @SneakyThrows
        private void handlePendingList(){
            while (true){
                // 获取队列中订单信息
                try {
                    // 1、获取pending-list中的订单信息 XREADGROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS streams.order >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );
                    // 2. 判断消息获取是否成功
                    // 2.1 获取失败。pendding-list没有消息，继续下一次循环
                    if (list == null || list.isEmpty()){
                        break;
                    }
                    // 2.2 有消息继续下单
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    // 3. 创建订单
                    handleVoucherOrder(voucherOrder);
                    // 4. ACk确认
                    stringRedisTemplate.opsForStream().acknowledge(queueName,"g1",record.getId());
                }catch (Exception e) {
                    log.error("处理订单异常", e);
                    Thread.sleep(20L);
                }
            }
        }

    }
    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        boolean islock = lock.tryLock();
        if (!islock){
            log.error("一人只能下一单");
            return;
        }
        try{
            //获取代理对象（事务）
            Long voucherId = voucherOrder.getVoucherId();
            proxy.createVoucherOrder(voucherOrder);
        } finally {
            lock.unlock();
        }
    }

    private IVoucherOrderService proxy;
    @Override
    public Result seckillVoucher(Long voucherId) {
        //获取用户id
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");
        // 执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(),String.valueOf(orderId)
        );

        // 判断结果是否为0
        int r = result.intValue();
        // not 0 无购买资格
        if (r != 0){
            return Result.fail(r==1?"库存不足":"不能重复下单");
        }
        //获取代理对象
        proxy = (IVoucherOrderService) AopContext.currentProxy();
        // 返回订单id
        return Result.ok(orderId);

    }

    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getId();

        //一人一单
        int count = query().eq("user_id", userId).eq("voucher_id", voucherOrder).count();
        if (count > 0) {
            log.error("用户已经购买过一次");
            return;
        }
        Long voucherId = voucherOrder.getVoucherId();
        //扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock-1")
                .eq("voucher_id", voucherOrder)
                .gt("stock", 0).update();
        if (!success) {
            log.error("库存不足");
            return;
        }
        save(voucherOrder);

    }

}
//
//@Override
//public Result seckillVoucher(Long voucherId) {
//    //获取用户id
//    Long userId = UserHolder.getUser().getId();
//    // 执行lua脚本
//    Long result = stringRedisTemplate.execute(
//            SECKILL_SCRIPT,
//            Collections.emptyList(),
//            voucherId.toString(), userId.toString()
//    );
//
//    // 判断结果是否为0
//    int r = result.intValue();
//    // not 0 无购买资格
//    if (r != 0){
//        return Result.fail(r==1?"库存不足":"不能重复下单");
//    }
//    // 0 有购买资格，将下单信息保存到阻塞队列
//    long orderId = redisIdWorker.nextId("order");
//
//    VoucherOrder voucherOrder = new VoucherOrder();
//    voucherOrder.setId(orderId);
//    voucherOrder.setUserId(userId);
//    voucherOrder.setVoucherId(voucherId);
//    //创建阻塞队列
//    orderTasks.add(voucherOrder);
//
//    //获取代理对象
//    proxy = (IVoucherOrderService) AopContext.currentProxy();
//    // 返回订单id
//    return Result.ok(orderId);
//
//}
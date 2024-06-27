package com.sharecampus.rabbitmq;

import com.sharecampus.entity.VoucherOrder;
import com.sharecampus.service.IVoucherOrderService;
import com.sharecampus.service.IVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.sharecampus.utils.RabbitMQConstants.*;


/**
 * 消息消费者
 */
@Slf4j
@Service
public class MqListener {

    @Resource
    IVoucherOrderService voucherOrderService;

    @Resource
    IVoucherService VoucherService;

    /**
     * 接收订单信息并写入数据库
     */
    @Transactional
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SECK_QUEUE),
            exchange = @Exchange(name = SECK_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = SECK_ROUTINGKEY
    ))
    public void receiveSeckillMessage(VoucherOrder msg) {
        log.info("消费者接收到消息：【" + msg + "】");

        Long voucherId = msg.getVoucherId();
        // 1.校验一人一单
        Long userId = msg.getUserId();
        // 1.1 查询订单
        int count = voucherOrderService.query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        // 1.2 判断是否存在
        if (count > 0) {
            // 用户已经购买过了
            log.error("该用户已购买过");
            return;
        }
        log.info("扣减库存");
        // 2.扣减库存
        boolean success = VoucherService
                .update()
                .setSql("stock = stock-1")
                .eq("id", voucherId)
                .gt("stock", 0)// cas乐观锁
                .update();
        if (!success) {
            log.error("库存不足");
            return;
        }
        // 3.保存订单
        voucherOrderService.save(msg);
    }
}

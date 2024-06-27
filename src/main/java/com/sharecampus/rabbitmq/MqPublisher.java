package com.sharecampus.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.sharecampus.utils.RabbitMQConstants.SECK_EXCHANGE;

/**
 * 消息发送者
 */
@Slf4j
@Service
public class MqPublisher {
    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final String ROUTINGKEY = "seckill.message";

    /**
     * 发送秒杀信息
     *
     * @param msg
     */
    public void sendSeckillMessage(Object msg) {
        log.info("生产者发送消息：【" + msg + "】");
        rabbitTemplate.convertAndSend(SECK_EXCHANGE, ROUTINGKEY, msg);
    }
}

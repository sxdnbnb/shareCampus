package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    private static final long BEGIN_TIMESTAMP = 1640995200;
    private static final int Count_Bits = 32;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    public long nextId(String keyPrefix){
        // 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSecond - BEGIN_TIMESTAMP;
        // 生成序列号
        String date = now.format(DateTimeFormatter.ofPattern("YYYY:MM:DD"));
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        // 拼接并返回
        long id = (timeStamp << Count_Bits)|count;
        return id;
    }

    public static void main(String[] args){
        LocalDateTime  time = LocalDateTime.of(2022,1,1,0,0,0);
        long second = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println("second = "+second);
    }
}


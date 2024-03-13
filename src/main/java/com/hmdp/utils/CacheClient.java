package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }
    public  void set(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R,ID> R queryWithPassThrough(String keyPre, ID id, Class<R> type,
                                         Function<ID,R> dbFallback,  Long time, TimeUnit unit){
        String key = keyPre+id;
        // 从redis查缓存
        String json = stringRedisTemplate.opsForValue().get(key);

        // 判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 存在，直接返回，
            return JSONUtil.toBean(json,type);
        }
        if (json != null){
            return null;
        }
        // 不存在，根据ID查数据库
        R r = dbFallback.apply(id);
        // 不存在，返回错误
        if (r == null){
            this.set(key,"",time,unit);
            return null;
        }
        //存在，写入redis
        this.set(key,r,time,unit);
        return r;
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    public <R,ID> R queryWithLogicalExpire(
            String keyPre, ID id, Class<R> type, Function<ID,R> dbFallback,Long time, TimeUnit unit
    ){
        String key = keyPre + id;
        // 从redis查缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        //log.debug("shopJson"+shopJson);
        // 判断是否存在
        if (StrUtil.isBlank(json)) {
            // 不存在，直接返回，
            return null;
        }
        // 命中，需要判断过期时间
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        R r = JSONUtil.toBean(data, type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //log.debug("expireTime: "+expireTime);
        if (expireTime.isAfter(LocalDateTime.now())){
            //log.debug("AfterNow");
            return r;
        }
        String lockKey = RedisConstants.LOCK_Venue_KEY+id;
        boolean isLock = tryLock(lockKey);
        if (isLock){
            //开启独立线程实现重建
            CACHE_REBUILD_EXECUTOR.submit(
                    ()->{
                        try {
                            R r1 = dbFallback.apply(id);
                            this.setWithLogicalExpire(key,r1,time,unit);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            unlock(lockKey);
                        }
                    }
            );
        }
        return r;
    }

    public <R,ID> R queryWithMutex(
            String keyPre,ID id, Class<R> type, Function<ID,R> dbFallback,Long time, TimeUnit unit
    ) {
        String key = keyPre+id;
        // 从redis查缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 存在，直接返回，
            R r = JSONUtil.toBean(json, type);
            stringRedisTemplate.expire(key, time, unit);
            return r;
        }
        if (json != null) {
            return null;
        }
        //实现缓存重建
        //获取互斥锁
        String lockkey = RedisConstants.LOCK_Venue_KEY + id;
        R r = null;
        try {
            boolean isLock = tryLock(lockkey);
            //判断是否获取成功
            while (!isLock) {
                //失败，则休眠并重试
                Thread.sleep(50L);
                isLock = tryLock(lockkey);
            }


            //成功，根据id查询数据库
            r = dbFallback.apply(id);
            //模拟重建延时
            //Thread.sleep(200);
            // 不存在，根据ID查数据库

            // 不存在，返回错误
            if (r == null) {
                stringRedisTemplate.opsForValue().set(
                        RedisConstants.CACHE_Venue_KEY + id.toString(), "",
                        RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            //存在，写入redis
           this.set(key,r,time,unit);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            unlock(lockkey);
            return r;
        }


    }

    private boolean tryLock(String key){
        boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key,"1",RedisConstants.LOCK_Venue_TTL,TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private  void  unlock(String key){
        stringRedisTemplate.delete(key);
    }
}

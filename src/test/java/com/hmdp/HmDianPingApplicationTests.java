package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.SHOP_GEO_KEY;

@SpringBootTest
class HmDianPingApplicationTests {
    @Resource
    private ShopServiceImpl shopService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private ExecutorService es = Executors.newFixedThreadPool(500);
    @Test
    void testIdWorker(){
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = ()->{
            for (int i = 0; i< 100; i++){
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++){
            es.submit(task);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        System.out.println("time = "+(end-begin));
    }
    @Test
    void testSaveShop(){
        Long id = UserHolder.getUser().getId();
        System.out.println(id);
    }
    @Test
    void loadshopdata(){
        // 1. 查询店铺信息
        List<Shop> list = shopService.list();
        // 2. 将店铺按照typeid分组
        Map<Long,List<Shop>> map= list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        // 3. 分批完成写入
        for (Map.Entry<Long, List<Shop>> entry: map.entrySet()){
            Long typeId = entry.getKey();
            String key = SHOP_GEO_KEY+typeId;
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            for (Shop shop:value){
                //stringRedisTemplate.opsForGeo().add(key,new Point(shop.getX(),shop.getY()),shop.getId().toString());
                locations.add(
                        new RedisGeoCommands.GeoLocation<>(
                                shop.getId().toString(),new Point(shop.getX(),shop.getY()
                        )
                        ));
            }
            stringRedisTemplate.opsForGeo().add(key,locations);
        }
    }

    @Test
    void testHyperLogLog(){
        String[] values = new String[1000];
        for (int i = 0; i < 1000000;i++){
            int j = i%1000;
            values[j] = "user_"+i;
            if (j == 999){
                stringRedisTemplate.opsForHyperLogLog().add("hl2",values);
            }
        }
        Long count = stringRedisTemplate.opsForHyperLogLog().size("hl2");
        System.out.println("count = "+count);

    }
}

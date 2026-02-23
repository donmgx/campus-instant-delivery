package com.campus.task;

import com.campus.entity.Coupon;
import com.campus.mapper.CouponMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/*
 * 定期向数据库更新优惠券库存
 * */
@Component
@Slf4j
public class CouponTask {
    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void syncCouponStock() {

        log.info("开始同步优惠券库存");

        //获取所有优惠券的key
        Set<String> keys = stringRedisTemplate.keys("seckill_coupon_stock_*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {

            //获取couponId
            String[] splits = key.split("_");
            Long couponId = Long.valueOf(splits[splits.length - 1]);

            //获取redis中优惠券库存
            String stockStr = stringRedisTemplate.opsForValue().get(key);
            if (stockStr != null) {

                //获取数据库中的优惠券库存
                Coupon coupon = couponMapper.getById(couponId);
                if (coupon == null) {
                    //数据库没券
                    continue;
                }

                //判断redis中的库存数量是否与数据库中的库存数量相等
                int redisStock = Integer.parseInt(stockStr);
                if (coupon.getRemainingCount() == redisStock) {
                    //如果一致，不需要更新
                    continue;
                }

                //更新数据库 库存
                couponMapper.updateStock(couponId, redisStock);

                log.info("同步完成->id：{}，修正库存：{} -> {}", couponId, coupon.getRemainingCount(), redisStock);

            }
        }
    }
}

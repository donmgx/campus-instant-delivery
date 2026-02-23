package com.campus.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    private static final String DISH_BLOOM_FILTER = "dish:bloom:filter";
    private static final String SETMEAL_BLOOM_FILTER = "setmeal:bloom:filter";
    private static final String COUPON_BLOOM_FILTER = "coupon:bloom:filter";

    /*
     * 菜品的布隆过滤器
     * */
    @Bean
    public RBloomFilter<Long> dishBloomFilter(RedissonClient redissonClient) {
        //获取布隆过滤器实例
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(DISH_BLOOM_FILTER);

        //初始化布隆过滤器 tryInit(预计元素数量, 误判率)
        bloomFilter.tryInit(100000L, 0.01);
        return bloomFilter;
    }

    /*
     * 套餐的布隆过滤器
     * */
    @Bean
    public RBloomFilter<Long> setmealBloomFilter(RedissonClient redissonClient){
        //获取布隆过滤器实例
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(SETMEAL_BLOOM_FILTER);

        //初始化布隆过滤器 tryInit(预计元素数量, 误判率)
        bloomFilter.tryInit(10000L, 0.01);
        return bloomFilter;
    }

    /*
     * 优惠券布隆过滤器
     * */
    @Bean
    public RBloomFilter<Long> couponBloomFilter(RedissonClient redissonClient){
        //获取布隆过滤器实例
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(COUPON_BLOOM_FILTER);

        //初始化布隆过滤器 tryInit(预计元素数量, 误判率)
        bloomFilter.tryInit(10000L, 0.01);
        return bloomFilter;
    }

}

package com.campus.task;

import com.campus.entity.Coupon;
import com.campus.entity.Dish;
import com.campus.entity.Setmeal;
import com.campus.mapper.CouponMapper;
import com.campus.mapper.DishMapper;
import com.campus.mapper.SetmealMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * 每次启动进行布隆过滤器的数据预热
 * */
@Slf4j
@Component
public class BloomFilterInitRunner implements CommandLineRunner {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private RBloomFilter<Long> dishBloomFilter;

    @Autowired
    private RBloomFilter<Long> setmealBloomFilter;

    @Autowired
    private RBloomFilter<Long> couponBloomFilter;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始进行布隆过滤器的预热...");

        // 1. 预热菜品
        List<Dish> dishList = dishMapper.list(new Dish());
        if (!dishList.isEmpty() && dishList != null) {
            for (Dish dish : dishList) {
                //将id加入到布隆过滤器
                dishBloomFilter.add(dish.getId());
            }
        }

        // 2. 预热套餐
        List<Setmeal> setmealList = setmealMapper.list(new Setmeal());
        if (setmealList != null) {
            for (Setmeal s : setmealList) {
                setmealBloomFilter.add(s.getId());
            }
        }

        // 3. 预热优惠券
        // 假设 couponMapper 有 list 方法
        List<Coupon> couponList = couponMapper.list(new Coupon());
        if (couponList != null) {
            for (Coupon c : couponList) {
                couponBloomFilter.add(c.getId());
            }
        }

        log.info("布隆过滤器预热完成,共加载{}条菜品,{}条套餐,{}条优惠券"
                , dishList != null ? dishList.size() : 0
                , setmealList != null ? setmealList.size() : 0
                , couponList != null ? couponList.size() : 0);
    }
}

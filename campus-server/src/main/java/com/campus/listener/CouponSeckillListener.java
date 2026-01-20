package com.campus.listener;

import com.campus.config.RabbitConfig;
import com.campus.constant.StatusConstant;
import com.campus.entity.UserCoupon;
import com.campus.mapper.UserCouponMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/*
* 监听器
* */

@Component
@Slf4j
public class CouponSeckillListener {
    @Autowired
    private UserCouponMapper userCouponMapper;

    /*
     * 用于监听 RabbitMQ 队列 ,插入数据库
     * */
    @RabbitListener(queues = RabbitConfig.SECKILL_QUEUE)
    private void processSeckillMsg(Map<String, Object> msg) {
        log.info("监听到领券消息:{}", msg);

        try {
            //提取数据
            Long userId = Long.valueOf(msg.get("userId").toString());
            Long couponId = Long.valueOf(msg.get("couponId").toString());
            LocalDateTime endTime = LocalDateTime.parse(msg.get("endTime").toString());
            //构造实体对象
            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(userId)
                    .endTime(endTime)
                    .couponId(couponId)
                    .getTime(LocalDateTime.now())
                    .status(StatusConstant.UNUSED)
                    .build();
            //插入数据库
            userCouponMapper.insert(userCoupon);

            log.info("用户{}的优惠券{}落库成功",userId,couponId);

        } catch (Exception e) {
            log.info("异步落库失败：{}",e.getMessage());
        }

    }
}

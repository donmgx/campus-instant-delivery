package com.campus.listener;

import com.campus.event.UserSignedEvent;
import com.campus.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/*
 * 签到事件监听器
 * */
@Component
@Slf4j
public class SignEventListener {

    @Autowired
    private CouponService couponService;

    //奖励优惠券id
    private static final Long REWARD_COUPON_ID = 2L;

    @Async
    @EventListener
    public void handleSignEvent(UserSignedEvent userSignedEvent) {
        Long userId = userSignedEvent.getUserId();
        log.info("监听用户 {} 签到已连续签到 {} 天，准备发放奖励", userId
                , userSignedEvent.getContinuousDays()
        );

        try {
            //系统发放优惠券
            couponService.sendSystemCoupon(userId, REWARD_COUPON_ID);
            log.info("奖励发放成功！");
        } catch (Exception e) {
            log.info("奖励发放失败:{}", e.getMessage());
        }

    }
}

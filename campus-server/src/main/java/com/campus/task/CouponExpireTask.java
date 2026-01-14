package com.campus.task;

import com.campus.constant.StatusConstant;
import com.campus.mapper.UserCouponMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/*
 * 定期清理过期优惠券
 * */
@Component
@Slf4j
public class CouponExpireTask {

    @Autowired
    private UserCouponMapper userCouponMapper;

    /*
     * 每天凌晨两点，清理过期优惠券
     * */
    @Scheduled(cron = "0 0 2 * * ?")
    public void processCouponExpire() {
        log.info("开始清理过期优惠券");
        int rows = userCouponMapper.updateExpireStatus(LocalDateTime.now(), StatusConstant.UNUSED);
        log.info("清理完成，共清理{}张优惠券", rows);

    }
}

package com.campus.service.impl;

import com.campus.constant.MessageConstant;
import com.campus.constant.RedisConstant;
import com.campus.constant.StatusConstant;
import com.campus.context.BaseContext;
import com.campus.event.UserSignedEvent;
import com.campus.result.Result;
import com.campus.service.SignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class SignServiceImpl implements SignService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /*
     * 用户签到
     * */
    @Transactional(rollbackFor = Exception.class)
    public Result sign() {
        // 1.获取当前用户 id 和 当前时间
        Long userId = BaseContext.getCurrentId();
        LocalDate now = LocalDate.now();

        // 2.检查用户是否已经签到
        // 构造 key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String bitmapKey = RedisConstant.SIGN_RECORD_KEY + userId + keySuffix;
        // 构造 offset
        int dayOfMonth = now.getDayOfMonth();
        int offset = dayOfMonth - 1;
        // 原子性：setBit 返回原来的值。如果原来是 false，说明第一次签；如果原来是 true，说明重复签
        Boolean oldVal = stringRedisTemplate.opsForValue().setBit(bitmapKey, offset, true);
        if (Boolean.TRUE.equals(oldVal)) {
            // 证明已经签到
            return Result.error(MessageConstant.SIGNED_IN_TODAY);
        }

        // 3.维护连续签到天数
        String streakKey = RedisConstant.SIGN_STREAK_KEY + userId;
        long continuousDays = 1; //连签天数默认为 1，因为本次已经签上

        // 检查昨天是否已签
        LocalDate yesterday = now.minusDays(1);
        String yesterdaySuffix = yesterday.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        // 构造昨天的签到 key
        String yesterdayBitmapKey = RedisConstant.SIGN_RECORD_KEY + userId + yesterdaySuffix;
        // 构造 offset
        int yesterdayOffset = yesterday.getDayOfMonth() - 1;
        Boolean yesterdaySigned = stringRedisTemplate.opsForValue().getBit(yesterdayBitmapKey, yesterdayOffset);
        // 判断昨天是否已签
        if (Boolean.TRUE.equals(yesterdaySigned)) {
            // 昨天已签 ，加一天
            continuousDays = stringRedisTemplate.opsForValue().increment(streakKey);
        } else {
            //昨天未签，断签   置一
            stringRedisTemplate.opsForValue().set(streakKey, String.valueOf(continuousDays));
        }

        if (continuousDays > 0 && continuousDays % StatusConstant.SEVEN_DAYS == 0) {
            log.info("用户 {} 连续签到 {} 天，触发奖励事件", userId, continuousDays);
            //发布事件
            applicationEventPublisher.publishEvent(new UserSignedEvent(userId, (int) continuousDays));
        } else {
            log.info("用户 {} 签到成功，连续签到 {} 天", userId, continuousDays);
        }


        return Result.success(MessageConstant.SIGN_SUCCESS + "，已连续签到" + continuousDays + "天");

    }
}

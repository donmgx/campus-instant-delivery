package com.campus.service.impl;

import com.campus.constant.MessageConstant;
import com.campus.constant.RiderConstant;
import com.campus.exception.BaseException;
import com.campus.mapper.OrderMapper;
import com.campus.service.RiderStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
public class RiderStatsServiceImpl implements RiderStatsService {

    @Autowired
    private OrderMapper orderMapper;

    /*
     * 统计骑手数据
     * */
    public BigDecimal getStats(Long riderId, LocalDate begin, LocalDate end) {
        if (end == null) {
            end = LocalDate.now();
        }
        // 判断时间数据是否合理
        if (begin.isAfter(end)||begin == null) {
            throw new BaseException(MessageConstant.TIME_ERROR);
        }
        // 将 LocalDate 转换为 LocalDateTime
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        if (beginTime.isAfter(LocalDateTime.now())) {
            throw new BaseException(MessageConstant.TIME_ERROR);
        }
        // 统计时间段内该骑手的配送订单数
        Integer orderNumber = orderMapper.countRiderIncome(riderId, beginTime, endTime);
        // 计算收入
        BigDecimal income = BigDecimal.valueOf(orderNumber).multiply(RiderConstant.deliveryFee);
        return income;
    }
}

package com.campus.controller.rider;

import com.campus.context.BaseContext;
import com.campus.result.Result;
import com.campus.service.RiderStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/rider/stats")
public class RiderStatsController {
    @Autowired
    private RiderStatsService riderStatsService;

    /*
     * 统计骑手数据
     * */
    @GetMapping("/statistics")
    public Result<BigDecimal> getTodayStats(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin
            , @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
            Long riderId = BaseContext.getCurrentId();
        log.info("骑手{}查看个人今日数据", riderId);
        BigDecimal stats = riderStatsService.getStats(riderId, begin, end);
        return Result.success(stats);
    }
}

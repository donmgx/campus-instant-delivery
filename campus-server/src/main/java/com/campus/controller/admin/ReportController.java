package com.campus.controller.admin;

import com.campus.result.Result;
import com.campus.service.ReportService;
import com.campus.vo.OrderReportVO;
import com.campus.vo.SalesTop10ReportVO;
import com.campus.vo.TurnoverReportVO;
import com.campus.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/*
 * 数据统计相关接口
 * */
@RestController
@Slf4j
@Api(tags = "数据统计相关接口")
@RequestMapping("/admin/report")
public class ReportController {
    @Autowired
    private ReportService reportService;


    /*
     * 营业额统计
     * */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> TurnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin
            , @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额统计：{} ~ {}", begin, end);
        TurnoverReportVO turnoverReportVO = reportService.TurnoverStatistics(begin, end);
        return Result.success(turnoverReportVO);
    }


    /*
     * 用户统计
     * */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("用户统计：{} ~ {}", begin, end);
        UserReportVO userReportVO = reportService.userStatistics(begin, end);
        return Result.success(userReportVO);
    }


    /*
     * 订单统计
     * */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> orderStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("订单统计：{} ~ {}", begin, end);
        OrderReportVO orderReportVO = reportService.orderStatistics(begin, end);
        return Result.success(orderReportVO);
    }


    /*
     * 销量排名
     * */
    @GetMapping("top10")
    @ApiOperation("销量排行")
    public Result<SalesTop10ReportVO> salesTop(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("销量排名：{} ~ {}", begin, end);
        SalesTop10ReportVO salesTop10ReportVO = reportService.salesTop(begin, end);
        return Result.success(salesTop10ReportVO);
    }


    /*
    * 营业数据导出
    * */
    @GetMapping("/export")
    @ApiOperation("营业数据导出")
    public void export(HttpServletResponse response){
        log.info("营业数据导出");
        reportService.exportBusinessDate(response);
    }

}

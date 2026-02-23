package com.campus.service;

import com.campus.vo.OrderReportVO;
import com.campus.vo.SalesTop10ReportVO;
import com.campus.vo.TurnoverReportVO;
import com.campus.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {


    /*
     * 营业额统计
     * */
    TurnoverReportVO TurnoverStatistics(LocalDate begin, LocalDate end);


    /*
     * 用户统计
     * */
    UserReportVO userStatistics(LocalDate begin, LocalDate end);


    /*
     * 订单统计
     * */
    OrderReportVO orderStatistics(LocalDate begin, LocalDate end);


    /*
     * 销量排名
     * */
    SalesTop10ReportVO salesTop(LocalDate begin, LocalDate end);


    /*
     * 营业数据导出
     * */
    void exportBusinessDate(HttpServletResponse response);
}

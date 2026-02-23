package com.campus.service.impl;

import com.campus.dto.GoodsSalesDTO;
import com.campus.entity.Orders;
import com.campus.mapper.OrderMapper;
import com.campus.mapper.UserMapper;
import com.campus.service.ReportService;
import com.campus.service.WorkspaceService;
import com.campus.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /*
     * 营业额统计
     * */
    public TurnoverReportVO TurnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        //返回每天营业额数据
        for (LocalDate localDate : dateList) {
            //把LocalDate 变为 LocalDateTime
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);

            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        return new TurnoverReportVO()
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }


    /*
     * 用户统计
     * */
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //参数dateList
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //参数newUserList 和 totalUserList
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            //获取总用户数
            Integer totalUserCount = userMapper.countByMap(map);

            map.put("end", endTime);
            //获取新增用户数
            Integer newUserCount = userMapper.countByMap(map);

            newUserList.add(newUserCount);
            totalUserList.add(totalUserCount);
        }

        return new UserReportVO().builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }


    /*
     * 订单统计
     * */
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        //参数 dateList
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }


        //订单总数 totalOrderCount
        Integer totalOrderCount = 0;
        //总有效订单数
        Integer totalValidOrderCount = 0;

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            //订单数
            Integer orderCount = orderMapper.countByMap(map);

            totalOrderCount += orderCount;

            //有效订单数
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);

            totalValidOrderCount += validOrderCount;

            //参数 orderCountList
            orderCountList.add(orderCount);
            //参数 validOrderCountList
            validOrderCountList.add(validOrderCount);

        }

        //订单完成率 orderCompletionRate
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != null) {
            orderCompletionRate = totalValidOrderCount.doubleValue() / totalOrderCount;
        }


        return new OrderReportVO().builder()
                .dateList(StringUtils.join(dateList, ","))
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(totalOrderCount)
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .build();
    }


    /*
     * 销量排名
     * */
    public SalesTop10ReportVO salesTop(LocalDate begin, LocalDate end) {
        /*
        select sum(od.number) as number, od.name as name from order_detail od, orders ods
        where ods.id = od.order_id and ods.status = 6 and ods.order_time > '2025-11-24 11:34:23' and ods.order_time < '2025-11-26 11:57:00'
        group by od.name
        order by sum(od.number) desc, od.name desc
        limit 0,10
        */

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.sumSales(beginTime, endTime);

        List<String> names = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        String nameList = StringUtils.join(names, ",");
        String numberList = StringUtils.join(numbers, ",");


        return new SalesTop10ReportVO().builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }


    /*
     * 营业数据导出
     * */
    public void exportBusinessDate(HttpServletResponse response) {

        //查询数据库，获取营业数据
        LocalDateTime endTime = LocalDateTime.now().plusDays(-1);
        LocalDateTime beginTime = endTime.plusDays(-30);
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);

        //写入到excel文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {

            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheetAt(0);

            //填充时间数据
            sheet.getRow(1).getCell(1).setCellValue("时间：" + beginTime + "至" + endTime);

            //获取概览数据
            //获取第四行
            XSSFRow row = sheet.getRow(3);
            //营业额
            row.getCell(2).setCellValue(businessData.getTurnover());
            //订单完成率
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            //新增用户数
            row.getCell(6).setCellValue(businessData.getNewUsers());

            //获取第五行
            row = sheet.getRow(4);
            //有效订单
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            //平均客单价
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //设置明细数据
            for (int i = 0; i < 30; i++) {
                LocalDateTime begin = beginTime.plusDays(1);
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(begin.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //将excel文件下载到浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            //关闭流
            outputStream.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

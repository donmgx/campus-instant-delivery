package com.campus.controller.admin;

import com.campus.dto.*;
import com.campus.result.PageResult;
import com.campus.result.Result;
import com.campus.service.OrderService;
import com.campus.vo.OrderStatisticsVO;
import com.campus.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "订单相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;


    /*
     * 查询订单详情
     * */
    @GetMapping("/details/{id}")
    @ApiOperation("管理端查询订单详情")
    public Result<OrderVO> getById(@PathVariable Long id) {
        log.info("管理端查询订单详情:id:{}", id);
        OrderVO orderVO = orderService.getById(id);
        return Result.success(orderVO);
    }

    /*
     * 订单搜索
     * */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> getByIds(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索:id:{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.getOrders(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
     * 取消订单
     * */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        log.info("取消订单:{}", ordersCancelDTO);
        orderService.adminConcel(ordersCancelDTO);
        return Result.success();
    }


    /*
     * 各个状态的订单数量统计
     * */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> getStatus() {
        log.info("各个状态的订单数量统计");

        OrderStatisticsVO orderStatisticsVO = orderService.getStatus();

        return Result.success(orderStatisticsVO);
    }


    /*
    * 拒单
    * */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result reject(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        log.info("商家拒单：{}",ordersRejectionDTO);

        orderService.reject(ordersRejectionDTO);
        return Result.success();
    }


    /*
    * 接单
    * */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接单：id:{}",ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();

    }

    /*
    * 派送订单
    * */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id) {
        orderService.delivery(id);
        return Result.success();
    }


    /*
    * 完成订单
    * */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }
}

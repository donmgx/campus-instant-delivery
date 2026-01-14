package com.campus.controller.user;

import com.campus.annotation.AutoIdempotent;
import com.campus.dto.OrdersPageQueryDTO;
import com.campus.dto.OrdersPaymentDTO;
import com.campus.dto.OrdersSubmitDTO;
import com.campus.result.PageResult;
import com.campus.result.Result;
import com.campus.service.OrderService;
import com.campus.vo.OrderCalculateVO;
import com.campus.vo.OrderPaymentVO;
import com.campus.vo.OrderSubmitVO;
import com.campus.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;


    /*
     * 用户下单
     * */
    @ApiOperation("用户下单")
    @PostMapping("/submit")
    @AutoIdempotent //幂等性
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {

        log.info("用户下单:{}", ordersSubmitDTO);

        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);

        return Result.success(orderSubmitVO);
    }

    /*
    * 预计算支付价格
    * */
    @GetMapping("/calculate")
    @ApiOperation("订单费用试算")
    public Result<OrderCalculateVO> calculate(@RequestParam Long addressBookId){
        log.info("预计算支付价格,addressBookId:{}",addressBookId);
        OrderCalculateVO orderCalculateVO = orderService.calculate();
        return Result.success(orderCalculateVO);
    }



    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /*
     * 查询订单详情
     * */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getById(@PathVariable Long id) {
        log.info("查询订单详情:id:{}", id);
        OrderVO orderVO = orderService.getById(id);
        return Result.success(orderVO);
    }

    /*
     * 历史订单查询(分页查询)
     * */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> getByIds(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("历史订单查询:id:{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.getByUserIds(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
     * 取消订单
     * */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id) throws Exception {
        log.info("取消订单:id:{}", id);
        orderService.concel(id);
        return Result.success();
    }


    /*
    * 再来一单
    * */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result submitAgain(@PathVariable Long id){
        log.info("再来一单：原订单{}",id);

        orderService.submitAgain(id);

        return Result.success();
    }


    /*
    * 催单
    * */
    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result remider(@PathVariable Long id){
        log.info("客户催单:{}",id);
        orderService.remider(id);
        return Result.success();
    }
}

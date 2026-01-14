package com.campus.service;

import com.campus.dto.*;
import com.campus.result.PageResult;
import com.campus.vo.*;

public interface OrderService {


    /*
     * 用户下单
     * */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);


    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);


    /*
     * 查询订单详情
     * */
    OrderVO getById(Long id);


    /*
     * 历史订单查询(分页查询)
     * */
    PageResult getByUserIds(OrdersPageQueryDTO ordersPageQueryDTO);


    /*
     * 取消订单
     * */
    void concel(Long id) throws Exception;

    /*
     * 再来一单
     * */
    void submitAgain(Long id);


    /*
     * 订单搜索
     * */
    PageResult getOrders(OrdersPageQueryDTO ordersPageQueryDTO);


    /*
     * 各个状态的订单数量统计
     * */
    OrderStatisticsVO getStatus();


    /*
    * 管理端取消订单
    * */
    void adminConcel(OrdersCancelDTO ordersCancelDTO) throws Exception;


    /*
     * 拒单
     * */
    void reject(OrdersRejectionDTO ordersRejectionDTO) throws Exception;


    /*
     * 接单
     * */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);


    /*
     * 派送订单
     * */
    void delivery(Long id);


    /*
     * 完成订单
     * */
    void complete(Long id);


    /*
     * 催单
     * */
    void remider(Long id);

    /*
     * 预计算支付价格
     * */
    OrderCalculateVO calculate();

}

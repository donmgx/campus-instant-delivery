package com.campus.task;

import com.campus.constant.MessageConstant;
import com.campus.entity.Orders;
import com.campus.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/*
 * 定时任务类
 *
 * */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;


    /*
     * 订单超时处理
     * */
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeOutOrder() {
        log.info("定时处理超时订单，超时时间为：{}", LocalDateTime.now());

        //查询超时订单  select * from orders where status = 1 and order_time < (当前时间-15分钟)
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));

        //设置超时订单的状态
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason(MessageConstant.ORDER_TIMEOUT);
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }


    /*
    * 订单派送长时间未完成处理
    * */
    @Scheduled(cron = " 0 0 1 * * ? ")
    public void processDeliverOrder(){
        log.info("订单派送长时间未完成处理：{}",LocalDateTime.now());

        //查询昨日的订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusHours(-1));

        //设置超时订单的状态
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}

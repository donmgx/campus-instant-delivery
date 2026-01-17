package com.campus.listener;

import com.campus.config.RabbitConfig;
import com.campus.entity.Orders;
import com.campus.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderTimeoutListener {

    @Autowired
    private OrderMapper orderMapper;

    @RabbitListener(queues = RabbitConfig.QUEUE_DLX_ORDER)
    public void processTimeoutOrder(Long orderId, Message message, Channel channel) {
        log.info("接收到超时订单消息，订单ID: {}", orderId);

        try {
            // 1. 查询订单
            Orders orders = orderMapper.getById(orderId);

            // 2. 判断状态：如果是"待付款"，则取消
            if (orders != null && orders.getStatus().equals(Orders.PENDING_PAYMENT)) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
                log.info("订单超时已取消，订单ID: {}", orderId);
            } else {
                log.info("订单状态正常或已支付，无需取消。ID: {}", orderId);
            }


        } catch (Exception e) {
            log.error("处理超时订单失败", e);

        }
    }
}
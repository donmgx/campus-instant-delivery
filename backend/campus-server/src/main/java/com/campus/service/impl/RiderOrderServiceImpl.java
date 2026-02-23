package com.campus.service.impl;

import com.campus.constant.MessageConstant;
import com.campus.constant.RedisConstant;
import com.campus.constant.StatusConstant;
import com.campus.entity.Orders;
import com.campus.exception.OrderBusinessException;
import com.campus.mapper.OrderMapper;
import com.campus.service.RiderOrderService;
import com.campus.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RiderOrderServiceImpl implements RiderOrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedissonClient redissonClient;


    /*
     * 查询可配送订单
     * */
    public List<OrderVO> getWaitList() {
        // 1. 查询可派送的订单
        List<Orders> ordersList = orderMapper.listByStatus(Orders.CONFIRMED);

        // 2. 转换成 VO 给前端
        List<OrderVO> orderVOList = new ArrayList<>();
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }


    /*
     * 骑手抢单
     * */
    @Transactional
    public void takeOrder(Long riderId, Long orderId) {
        // 构建锁key,锁住该订单
        String key = RedisConstant.LOCK_ORDER_TAKE + orderId;
        // 获取锁的实例
        RLock lock = redissonClient.getLock(key);

        try {
            // 无需等待，说明已经被别人抢了
            boolean isLock = lock.tryLock(0, 5, TimeUnit.MINUTES);
            if (isLock) {
                // 成功获取锁
                try {
                    // 保险起见查数据库订单
                    Orders order = orderMapper.getById(orderId);
                    if (order.getStatus() != Orders.CONFIRMED) {
                        throw new OrderBusinessException(MessageConstant.ORDER_SNAPPED_UP);
                    }

                    // 执行抢单逻辑
                    Orders updateOrder = Orders.builder()
                            .id(orderId)
                            .status(Orders.DELIVERY_IN_PROGRESS)
                            .riderId(riderId)
                            .build();
                    // 修改数据：乐观锁
                    int row = orderMapper.updateStatusAndRider(updateOrder);
                    if (row == StatusConstant.ZERO) {
                        throw new OrderBusinessException(MessageConstant.ORDER_SNAPPED_UP);
                    }

                    log.info("骑手{}抢单成功，订单id:{}", riderId, orderId);
                } finally {
                    // 释放锁
                    lock.unlock();
                }
            } else {
                // 未能获取锁
                throw new OrderBusinessException(MessageConstant.ORDER_SNAPPED_UP);
            }
        } catch (InterruptedException e) {
            throw new OrderBusinessException(MessageConstant.SYSTEM_BUSY);
        }

    }

    /*
     * 骑手端——根据状态查询我的订单
     * */
    public List<OrderVO> getMyRunningList(Long riderId, Integer status) {
        List<Orders> ordersList = orderMapper.getMyRunningList(riderId, status);
        // 返回 OrderVO
        List<OrderVO> orderVOList = new ArrayList<>();
        ordersList.forEach(orders -> {

            OrderVO orderVO = new OrderVO();
            // 把Orders的属性拷贝到单个OrderVO对象中
            BeanUtils.copyProperties(orders, orderVO);
            // 将拷贝好的OrderVO添加到列表中
            orderVOList.add(orderVO);
        });
        return orderVOList;
    }


    /*
     * 订单派送完成
     * */
    public void completeOrder(Long riderId, Long orderId, String pickupCode) {
        // 查订单
        Orders order = orderMapper.getById(orderId);

        // 校验是否存在
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_EXIST);
        }
        // 判断是否为订单配送骑手，防止越权
        if (order.getRiderId() != riderId) {
            throw new OrderBusinessException(MessageConstant.ORDER_OPERATION_FORBIDDEN);
        }
        // 判断订单状态
        if (order.getStatus() != Orders.DELIVERY_IN_PROGRESS) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 判断取餐码是否正确
        if (!pickupCode.equals(order.getPickupCode())) {
            throw new OrderBusinessException(MessageConstant.PICKUP_CODE_ERROR);
        }

        // 修改数据库
        Orders updateOrder = Orders.builder()
                .id(orderId)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(updateOrder);
        log.info("骑手{}完成订单{}", riderId, orderId);
    }


    /*
     * 骑手取消订单配送
     * */
    public void cancelOrder(Long riderId, Long orderId) {
        // 查订单
        Orders order = orderMapper.getById(orderId);

        // 校验是否存在
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_EXIST);
        }
        // 判断是否为订单配送骑手，防止越权
        if (order.getRiderId() != riderId) {
            throw new OrderBusinessException(MessageConstant.ORDER_OPERATION_FORBIDDEN);
        }
        // 判断订单状态
        if (order.getStatus() != Orders.DELIVERY_IN_PROGRESS) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 修改数据
        orderMapper.cancelOrder(orderId, Orders.CONFIRMED);
        log.info("骑手{}取消订单{}", riderId, orderId);
    }
}

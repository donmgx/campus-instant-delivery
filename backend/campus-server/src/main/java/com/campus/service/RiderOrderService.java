package com.campus.service;

import com.campus.vo.OrderVO;

import java.util.List;

public interface RiderOrderService {
    void takeOrder(Long riderId, Long orderId);

    List<OrderVO> getWaitList();

    List<OrderVO> getMyRunningList(Long riderId, Integer status);

    void completeOrder(Long riderId, Long orderId, String pickupCode);

    void cancelOrder(Long riderId, Long orderId);
}

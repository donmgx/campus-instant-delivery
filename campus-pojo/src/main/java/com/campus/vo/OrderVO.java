package com.campus.vo;

import com.campus.entity.OrderDetail;
import com.campus.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO extends Orders implements Serializable {

    //订单菜品信息
    private String orderDishes;

    //订单详情
    private List<OrderDetail> orderDetailList;
    //取餐方式   1外卖配送  0到店自取
    private Integer deliveryMode;
    private String deliveryModeName; // “外卖配送” or “到店自取”
    //桌号
    private String tableNumber;
    private String pickupCode;

}

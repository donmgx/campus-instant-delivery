package com.campus.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCalculateVO implements Serializable {

    //推荐使用的优惠券ID(如果没有可用券，则为 null)
    private Long recommendCouponId;

    //订单原价(打包费 + 菜品总价)
    private BigDecimal originalAmount;

    //优惠金额(减了多少钱)
    private BigDecimal discountAmount;

    //预计实付金额
    private BigDecimal payAmount;

    //描述信息(已为您自动选择最佳优惠等)
    private String desc;
}
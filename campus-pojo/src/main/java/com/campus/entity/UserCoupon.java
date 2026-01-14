package com.campus.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon implements Serializable {

    private Long id;

    private Long userId;

    private Long couponId;

    private Long orderId;

    //用户优惠券的使用状态
    private Integer status; //0未使用  1已使用  2已过期

    private LocalDateTime getTime;

    private LocalDateTime endTime;

    private LocalDateTime useTime;

}

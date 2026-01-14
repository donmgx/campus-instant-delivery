package com.campus.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CouponPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String name;

    //优惠券id
    private Integer couponId;

    //状态 0表示禁用 1表示启用
    private Integer status;

    private Integer type;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}

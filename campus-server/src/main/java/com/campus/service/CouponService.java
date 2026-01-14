package com.campus.service;

import com.campus.dto.CouponDTO;
import com.campus.dto.CouponPageQueryDTO;
import com.campus.entity.Coupon;
import com.campus.result.PageResult;

/*
 * 优惠券相关接口
 * */
public interface CouponService {

    void addCoupon(CouponDTO couponDTO);

    Coupon getById(Long id);

    PageResult listPage(CouponPageQueryDTO couponPageQueryDTO);

    void edit(CouponDTO couponDTO);

    void startSeckill(Long id);

    void claimCoupon(Long couponId);
}

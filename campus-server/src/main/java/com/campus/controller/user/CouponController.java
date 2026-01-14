package com.campus.controller.user;


import com.campus.constant.MessageConstant;
import com.campus.constant.StatusConstant;
import com.campus.entity.es.CouponDoc;
import com.campus.repository.CouponDocRepository;
import com.campus.result.Result;
import com.campus.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "C端-优惠券接口")
@RequestMapping("/user/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponDocRepository couponDocRepository;


    /*
     * 领取优惠券
     * */
    @PostMapping("/claim/{id}")
    @ApiOperation("领取优惠券")
    public Result claim(@PathVariable Long id) {
        log.info("用户抢购优惠券couponId：{}", id);

        couponService.claimCoupon(id);

        return Result.success(MessageConstant.COUPON_CLAIM_SUCCESS);
    }


    /*
     * 用户查询正在秒杀活动的优惠券
     * */
    @GetMapping("/list")
    @ApiOperation("ES查询正在进行的秒杀活动")
    public Result<List<CouponDoc>> list() {
        log.info("用户查询正在秒杀活动的优惠券，使用ES");

        //直接走ES查询所有进行中的优惠券
        List<CouponDoc> couponDoc = couponDocRepository.findByStatusOrderByStartTimeDesc(StatusConstant.ONGOING);

        return Result.success(couponDoc);
    }


    /*
     * 用户根据名字搜索优惠券
     * */
    @GetMapping("/search")
    @ApiOperation("ES搜索优惠券")
    public Result<List<CouponDoc>> search(@RequestParam String keyWord) {

        log.info("用户根据名字搜索优惠券{}，使用ES", keyWord);

        List<CouponDoc> couponDocs = couponDocRepository.findByName(keyWord);

        return Result.success(couponDocs);

    }

}

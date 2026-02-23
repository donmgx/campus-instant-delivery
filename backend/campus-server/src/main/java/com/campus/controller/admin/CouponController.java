package com.campus.controller.admin;


import com.campus.dto.CouponDTO;
import com.campus.dto.CouponPageQueryDTO;
import com.campus.entity.Coupon;
import com.campus.result.PageResult;
import com.campus.result.Result;
import com.campus.service.CouponService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/*
 * 优惠券相关接口
 * */
@RestController("adminCouponController")
@Slf4j
@Api(tags = "管理端优惠券管理接口")
@RequestMapping("/admin/coupon")
public class CouponController {
    @Autowired
    private CouponService couponService;

    /*
     * 新增优惠券
     * */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('coupon:add')")
    public Result addCoupon(@RequestBody CouponDTO couponDTO) {
        log.info("新增优惠券:{}", couponDTO);

        couponService.addCoupon(couponDTO);

        return Result.success();
    }

    /*
     * 根据id查询优惠券详情
     * */
    @GetMapping("/list/{id}")
    @PreAuthorize("hasAuthority('coupon:list')")
    public Result<Coupon> list(@PathVariable Long id) {
        log.info("根据id查询优惠券详情：id:{}", id);

        Coupon coupon = couponService.getById(id);

        return Result.success(coupon);
    }

    /*
     * 优惠券的分页查询
     * */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('coupon:list')")
    public Result<PageResult> listPage(CouponPageQueryDTO couponPageQueryDTO) {
        log.info("优惠券的分页查询,分页参数：{}", couponPageQueryDTO);

        PageResult pageResult = couponService.listPage(couponPageQueryDTO);

        return Result.success(pageResult);
    }


    /*
     * 修改优惠券
     * */
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('coupon:edit')")
    public Result edit(@RequestBody CouponDTO couponDTO) {
        log.info("修改优惠券:{}", couponDTO);

        couponService.edit(couponDTO);

        return Result.success();
    }

    /*
     * 秒杀活动开始
     * */
    @PostMapping("/start/{id}")
    @PreAuthorize("hasAuthority('coupon:start')")
    public Result startSeckill(@PathVariable Long id) {
        log.info("秒杀活动开始：id：{}", id);
        couponService.startSeckill(id);
        return Result.success();
    }

}

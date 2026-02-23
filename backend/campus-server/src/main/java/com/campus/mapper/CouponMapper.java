package com.campus.mapper;


import com.campus.annotation.AutoFill;
import com.campus.dto.CouponPageQueryDTO;
import com.campus.entity.Coupon;
import com.campus.entity.UserCoupon;
import com.campus.entity.es.CouponDoc;
import com.campus.enumeration.OperationType;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CouponMapper {


    /*
     * 新增优惠券
     * */
    @AutoFill(OperationType.INSERT)
    void insert(Coupon coupon);

    /*
    * 根据id查询优惠券
    * */
    @Select("select * from campus_delivery.coupon where id = #{id}")
    Coupon getById(Long id);


    /*
    * 修改优惠券
    * */
    @AutoFill(OperationType.UPDATE)
    void update(Coupon coupon);


    /*
     * 优惠券的分页查询
     * */
    Page<Coupon> selectPage(CouponPageQueryDTO couponPageQueryDTO);


    /*
    * 更新数据库 库存
    * */
    @Update("update campus_delivery.coupon set remaining_count = #{redisStock} where id = #{couponId}")
    void updateStock(Long couponId, int redisStock);


    /*
    * 查询所有优惠券
    * */
    @Select("select * from campus_delivery.coupon")
    List<Coupon> list(Coupon coupon);


    @Select("select * from campus_delivery.user_coupon where user_id = #{userId} and status = #{status}")
    List<UserCoupon> getMyCoupons(Long userId,Integer status);
}

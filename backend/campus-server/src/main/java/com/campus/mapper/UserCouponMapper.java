package com.campus.mapper;


import com.campus.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserCouponMapper {


    /*
     * 插入领券纪录
     * */
    void insert(UserCoupon userCoupon);


    /*
     * 根据id查询优惠券
     * */
    @Select("select * from campus_delivery.user_coupon where id = #{id};")
    UserCoupon getById(Long id);

    /*
     * 使用优惠券
     * */
    @Update("update campus_delivery.user_coupon set order_id = #{orderId}, status = #{status}, use_time = #{useTime} " +
            "where id = #{userCouponId}")
    void userCoupon(Long userCouponId, Long orderId, Integer status, LocalDateTime useTime);


    /*
     * 回退优惠券
     * */
    @Update("update campus_delivery.user_coupon set order_id = null, status = #{status}, use_time = null where order_id = #{orderId}")
    void returnCoupon(Long orderId, Integer status);

    /*
     * 清理过期优惠券
     * */
    @Update("update campus_delivery.user_coupon set status = 2 where end_time < #{expireTime} and status = #{status}")
    int updateExpireStatus(LocalDateTime expireTime , Integer status);



    /*
    * 查询用户所有可用优惠券
    * */
    List<UserCoupon> listAvailable(Long userId);
}

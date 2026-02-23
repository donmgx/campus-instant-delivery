package com.campus.mapper;


import com.campus.annotation.AutoFill;
import com.campus.dto.CouponPageQueryDTO;
import com.campus.entity.Coupon;
import com.campus.entity.Rider;
import com.campus.enumeration.OperationType;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RiderMapper {

    void insert(Rider rider);

    @AutoFill(OperationType.UPDATE)
    void update(Rider rider);

    @Select("select * from campus_delivery.rider where phone = #{phone}")
    Rider getByPhone(String phone);
}

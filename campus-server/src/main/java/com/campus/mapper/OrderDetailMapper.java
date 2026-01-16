package com.campus.mapper;

import com.campus.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /*
    * 批量插入订单明细数据
    * */
    void insert(List<OrderDetail> orderDetails);


    /*查询订单详情表*/
    @Select("select * from campus_delivery.order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);


}

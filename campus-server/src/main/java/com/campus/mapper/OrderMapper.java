package com.campus.mapper;


import com.github.pagehelper.Page;
import com.campus.dto.GoodsSalesDTO;
import com.campus.dto.OrdersPageQueryDTO;
import com.campus.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /*
     * 插入订单数据
     * */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from campus_delivery.orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);


    /*
     * 查询订单表
     * */
    @Select("select * from campus_delivery.orders where id = #{id}")
    Orders getById(Long id);


    /*
     * 查询用户历史订单
     * */
    Page<Orders> selectPage(OrdersPageQueryDTO ordersPageQueryDTO);


    /*
     * 根据订单状态查询订单数量
     * */
    @Select("Select count(*) from campus_delivery.orders where status = #{status}")
    Integer countByStatus(Integer status);


    /*
     *
     * 根据订单状态和定单时间查询
     * */
    @Select("select * from campus_delivery.orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);


    /*
     * 计算每天营业额
     * */
    Double sumByMap(Map map);


    /*
     *
     *  查询订单数
     * */
    Integer countByMap(Map map);


    /*
     * 查询销量top10
     * */
    List<GoodsSalesDTO> sumSales(LocalDateTime beginTime, LocalDateTime endTime);


    /*
     * 查询可配送订单
     * */
    List<Orders> listByStatus(Integer status);

    /*
     * 骑手端-我的已接订单
     * */
    List<Orders> getMyRunningList(Long riderId, Integer status);

    /*
     * 统计骑手在指定时间段内的配送完成订单数
     * */
    Integer countRiderIncome(Long riderId, LocalDateTime beginTime, LocalDateTime endTime);

    /*
     * 骑手取消配送
     * */
    @Update("update campus_delivery.orders set status = #{status}, rider_id = null where id = #{orderId}")
    void cancelOrder(Long orderId, Integer status);


    /*
    * 骑手抢单
    * */
    int updateStatusAndRider(Orders updateOrder);
}

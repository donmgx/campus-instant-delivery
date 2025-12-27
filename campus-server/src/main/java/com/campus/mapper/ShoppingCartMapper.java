package com.campus.mapper;

import com.campus.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /*
     *根据用户id，菜品id或套餐id查询购物车数据
     * */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /*
     * 根据id修改购物车数据
     * */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart cart);


    /*
     * 插入数据到购物车
     * */
    void insert(ShoppingCart shoppingCart);


    /*
     * 清空购物车
     * */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void delete(Long userId);


    /*
    * 批量插入购物车
    * */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}

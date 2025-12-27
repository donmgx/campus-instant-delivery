package com.campus.service;

import com.campus.dto.ShoppingCartDTO;
import com.campus.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /*
    * 添加购物车
    * */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);


    /*
    * 根据userId查询所有购物车内容
    * */
    List<ShoppingCart> getByUserId();


    /*
     * 清空购物车
     * */
    void delete();


    /*
     * 删除购物车中一个商品
     * */
    void deleteOne(ShoppingCartDTO shoppingCartDTO);
}

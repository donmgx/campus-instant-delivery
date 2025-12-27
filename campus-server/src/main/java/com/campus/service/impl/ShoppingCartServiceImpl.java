package com.campus.service.impl;

import com.campus.context.BaseContext;
import com.campus.dto.ShoppingCartDTO;
import com.campus.entity.Dish;
import com.campus.entity.Setmeal;
import com.campus.entity.ShoppingCart;
import com.campus.mapper.DishMapper;
import com.campus.mapper.SetmealMapper;
import com.campus.mapper.ShoppingCartMapper;
import com.campus.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    /*
     * 添加购物车
     * */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //添加时先看购物车中是否有该菜品或套餐(不同的用户有自己的购物车数据),根据用户id，菜品id或套餐id
        ShoppingCart shoppingCart = new ShoppingCart();

        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //获取微信用户id
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //如果有，直接修改份数
        if (list != null && list.size() > 0) {

            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);

        } else {

            //如果没有，添加到购物车
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            if (dishId != null) {

                //如果 添加的是 菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            } else {

                //如果 添加的是套餐
                Setmeal setmeal = setmealMapper.selectById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }

            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            //插入数据到购物车
            shoppingCartMapper.insert(shoppingCart);
        }


    }


    /*
     * 根据userId查询所有购物车内容
     * */
    public List<ShoppingCart> getByUserId() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        return shoppingCartMapper.list(shoppingCart);
    }


    /*
     * 清空购物车
     * */
    public void delete() {
        Long userId = BaseContext.getCurrentId();

        shoppingCartMapper.delete(userId);

    }


    /*
     * 删除购物车中一个商品
     * */
    public void deleteOne(ShoppingCartDTO shoppingCartDTO) {
        //查询购物车中的数据
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        ShoppingCart cart = list.get(0);
        cart.setNumber(cart.getNumber()-1);

        shoppingCartMapper.updateNumberById(cart);

    }
}

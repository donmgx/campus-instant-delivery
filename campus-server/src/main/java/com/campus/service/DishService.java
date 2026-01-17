package com.campus.service;


import com.campus.dto.DishDTO;
import com.campus.dto.DishPageQueryDTO;
import com.campus.entity.Dish;
import com.campus.result.PageResult;
import com.campus.vo.DishVO;

import java.util.List;

/*
 * 菜品相关操作
 *
 * */
public interface DishService {

    /*
     * 新增菜品和对应口味
     * */
    void saveWithFlavor(DishDTO dishDTO);

    /*
     * 菜品的分类查询
     */
    PageResult getDishPage(DishPageQueryDTO dishPageQueryDTO);

    /*
     * 批量删除菜品
     * */
    void deleteDishWithFlavor(List<Long> ids);

    /*
     * 根据菜品 id 查询菜品和口味
     * */
    DishVO getDishWithFlavor(Long id) throws InterruptedException;

    /*
     * 修改菜品
     * */
    void update(DishDTO dishDTO);

    /*
     * 菜品起售停售
     * */
    void startOrStop(Integer status, Long id);


    /*
     *根据分类id查询菜品
     * */
    List<Dish> getDishByCategoryId(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}

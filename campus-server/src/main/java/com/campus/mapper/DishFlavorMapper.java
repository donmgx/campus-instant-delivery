package com.campus.mapper;

import com.campus.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    /*
    * 新增菜品口味
    * */
    void insert(List<DishFlavor> flavors);

    /*
    *
    * 根据dishId 删除口味
    * */
    void deleteByDishId(List<Long> dishIds);

    /*
    * 根据dishId查询口味
    * */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);

}

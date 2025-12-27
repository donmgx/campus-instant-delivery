package com.campus.mapper;

import com.campus.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /*
    * 根据菜品id查询套餐id
    * */
    List<Long> selectByDishId(List<Long> dishIds);

    /*
    * 新增套餐与菜品关联数据
    * */
    void insertBySetmealId(List<SetmealDish> setmealDishes);


    /*
    * 批量删除 setmeal_dish 中关联的菜品(根据setmeal_id)
    * */
    void delete(List<Long> setmealIds);


    /*
    * 根据setmealId查询关联的setmealDishs
    * */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> selectBySetmealId(Long setmealId);


}

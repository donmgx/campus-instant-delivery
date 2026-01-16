package com.campus.mapper;

import com.github.pagehelper.Page;
import com.campus.annotation.AutoFill;
import com.campus.dto.DishPageQueryDTO;
import com.campus.entity.Dish;
import com.campus.enumeration.OperationType;
import com.campus.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {


    /*
     * 根据id查询菜品的数量
     *
     * */
    @Select("select count(*) from campus_delivery.dish where category_id = #{id}")
    Integer countByCategoryId(Long id);


    /*
     * 插入菜品数据
     * */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);


    /*
     * 菜品的分类查询
     */
    //select d.* ,c.name as categoryName from dish d left join category c on d.category_id = c.id
    Page<DishVO> selectPage(DishPageQueryDTO dishPageQueryDTO);

    /*
    * 批量删除菜品
    * */
    void deleteById(List<Long> ids);

    /*
    * 根据菜品id 查询数据
    * */
    @Select("select * from campus_delivery.dish where id = #{id}")
    Dish getById(Long id);

    /*
     * 修改菜品
     * */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /*
     * 根据分类id查询菜品
     * */
    @Select("select * from campus_delivery.dish where category_id = #{categoryId}")
    List<Dish> getDishByCategoryId(Long categoryId);


    /*
    * 根据套餐id查询旗下的菜品
    *
    * */
    @Select("select d.* from campus_delivery.dish d left join campus_delivery.setmeal_dish s on d.id = s.dish_id where s.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);


    /**
     * 动态条件查询菜品
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);


    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

}

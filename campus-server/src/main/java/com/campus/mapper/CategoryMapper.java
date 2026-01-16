package com.campus.mapper;


import com.github.pagehelper.Page;
import com.campus.annotation.AutoFill;
import com.campus.dto.CategoryPageQueryDTO;
import com.campus.entity.Category;
import com.campus.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /*
    * 修改分类
    * */


    /*
     * 获取分类的数量
     * *//*
    @Select("select * from category")
    void getNumber();*/

    /*
    * 分类分页查询
    * */
    Page<Category> selectPage(CategoryPageQueryDTO categoryPageQueryDTO);

    /*
    * 修改分类
    * */
    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);

    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into campus_delivery.category (type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "values (#{type},#{name},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Category category);

    /*
    * 根据id查询关联的菜品或套餐
    * */
    @Select("select * from campus_delivery.dish where category_id = #{id} union all select * from campus_delivery.setmeal where category_id = #{id} ")
    List<Object> select(Long id);

    /*
    * 根据id删除分类
    * */
    @Delete("delete from campus_delivery.category where id  = #{id}")
    void delete(Long id);



    List<Category> list(Integer type);
}

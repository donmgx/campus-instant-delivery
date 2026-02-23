package com.campus.service;

import com.campus.dto.CategoryDTO;
import com.campus.dto.CategoryPageQueryDTO;
import com.campus.entity.Category;
import com.campus.result.PageResult;

import java.util.List;

public interface CategoryService {
    /*
     * 修改分类
     * */
    void changeCategory(CategoryDTO categoryDTO);


    /*
     * 分类分页查询
     * */
    PageResult selectPage(CategoryPageQueryDTO categoryPageQueryDTO);


    /*
     * 启用禁用分类
     * */
    void startOrStop(Integer status, Long id);

    /*
     * 新增分类
     * */
    void save(CategoryDTO categoryDTO);

    /*
     * 根据id删除分类
     * */
    void delete(Long id);

    /*
    * 根据类型分类
    * */
    List<Category> list(Integer type);

    List<Category> listById(Long categoryId);
}

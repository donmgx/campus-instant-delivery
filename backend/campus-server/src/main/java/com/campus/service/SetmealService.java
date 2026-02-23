package com.campus.service;

import com.campus.constant.StatusConstant;
import com.campus.dto.SetmealDTO;
import com.campus.dto.SetmealPageQueryDTO;
import com.campus.entity.Setmeal;
import com.campus.result.PageResult;
import com.campus.vo.DishItemVO;
import com.campus.vo.SetmealVO;

import java.util.List;

/*
* 套餐相关操作
* */
public interface SetmealService {

    /*
     * 新增套餐
     * */
    void insertSetmealWithDish(SetmealDTO setmealDTO);



    /*
     * 套餐分页查询
     * */
    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);


    /*
     * 起售停售套餐
     * */
    void startOrStop(Integer status, Long id);


    /*
    * 批量删除套餐
    * */
    void delete(List<Long> ids);


    /*
     * 根据id查询套餐
     * */
    SetmealVO getById(Long id);


    /*
     * 修改套餐
     * */
    void update(SetmealDTO setmealDTO);


    /**
     * 条件查询
     */
    List<Setmeal> list(Long categoryId);


    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);



}

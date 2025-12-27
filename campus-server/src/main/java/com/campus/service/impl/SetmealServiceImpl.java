package com.campus.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.campus.constant.MessageConstant;
import com.campus.constant.StatusConstant;
import com.campus.dto.SetmealDTO;
import com.campus.dto.SetmealPageQueryDTO;
import com.campus.entity.Dish;
import com.campus.entity.Setmeal;
import com.campus.entity.SetmealDish;
import com.campus.exception.DeletionNotAllowedException;
import com.campus.exception.SetmealEnableFailedException;
import com.campus.mapper.DishMapper;
import com.campus.mapper.SetmealDishMapper;
import com.campus.mapper.SetmealMapper;
import com.campus.result.PageResult;
import com.campus.service.DishService;
import com.campus.service.SetmealService;
import com.campus.vo.DishItemVO;
import com.campus.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/*
 * 套餐相关操作
 * */
@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishService dishService;
    @Autowired
    private DishMapper dishMapper;

    /*
     * 新增套餐
     * */
    @Transactional
    public void insertSetmealWithDish(SetmealDTO setmealDTO) {

        //新增套餐数据
        Setmeal setmeal = new Setmeal();
        //对象属性拷贝
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmealMapper.insert(setmeal);

        //增加菜品信息时，要先查询出菜品

        dishService.getDishByCategoryId(setmealDTO.getCategoryId());

        //新增套餐关联的菜品信息
        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));

        setmealDishMapper.insertBySetmealId(setmealDishes);


    }


    /*
     * 套餐分页查询
     * */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        //开始分页
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.selectSetmealWithCategory(setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }


    /*
     * 起售停售套餐
     * */
    @Override
    public void startOrStop(Integer status, Long id) {
        //当前套餐中有菜品未起售则不能起售
        List<Dish> dishes = dishMapper.getBySetmealId(id);
        if (status == StatusConstant.ENABLE) {
            for (Dish dish : dishes) {
                if (dish.getStatus() == StatusConstant.DISABLE) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }


        //根据id查询套餐
        Setmeal setmeal = setmealMapper.selectById(id);

        setmeal.setStatus(status);

        //修改数据
        setmealMapper.update(setmeal);

    }


    /*
     * 批量删除套餐
     * */
    @Transactional
    public void delete(List<Long> ids) {

        //已经起售的套餐不能删
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.selectById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //套餐下的菜品在起售中不能删
        for (Long id : ids) {
            List<Dish> dishes = dishMapper.getBySetmealId(id);
            for (Dish dish : dishes) {
                if (dish.getStatus() == StatusConstant.ENABLE) {
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_NOT_ENABLE_DELETE);
                }
            }
        }

        //批量删除套餐
        setmealMapper.delete(ids);

        //批量删除 setmeal_dish 中关联的菜品(根据setmeal_id)
        setmealDishMapper.delete(ids);


    }


    /*
     * 根据id查询套餐
     * */
    @Override
    public SetmealVO getById(Long id) {
        //查询套餐数据
        Setmeal setmeal = setmealMapper.selectById(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);

        //根据setmealId查询关联的setmealDishs
        List<SetmealDish> setmealDish = setmealDishMapper.selectBySetmealId(id);

        setmealVO.setSetmealDishes(setmealDish);
        return setmealVO;
    }


    /*
     * 修改套餐
     * */
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //修改套餐数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        //修改关联的setmeal_dish中的数据
        //  1.删除关联的setmeal_dish中的数据
        List<Long> ids = new ArrayList<>();
        ids.add(setmealDTO.getId());
        setmealDishMapper.delete(ids);

        // 2.插入新的关联数据
        //获取当前操作的套餐id
        Long setmealId = setmealDTO.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));

        setmealDishMapper.insertBySetmealId(setmealDishes);

    }


    /**
     * 条件查询
     *
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     *
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}

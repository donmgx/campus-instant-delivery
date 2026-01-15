package com.campus.service.impl;

import com.campus.entity.es.DishDoc;
import com.campus.event.DishChangedEvent;
import com.campus.repository.DishDocRepository;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.campus.constant.MessageConstant;
import com.campus.constant.StatusConstant;
import com.campus.dto.DishDTO;
import com.campus.dto.DishPageQueryDTO;
import com.campus.entity.Dish;
import com.campus.entity.DishFlavor;
import com.campus.entity.Setmeal;
import com.campus.exception.DeletionNotAllowedException;
import com.campus.exception.SetmealEnableFailedException;
import com.campus.mapper.DishFlavorMapper;
import com.campus.mapper.DishMapper;
import com.campus.mapper.SetmealDishMapper;
import com.campus.mapper.SetmealMapper;
import com.campus.result.PageResult;
import com.campus.service.DishService;
import com.campus.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/*
 * 菜品相关操作
 *
 * */

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private ApplicationContext applicationContext; //注入Spring上下文用于发事件


    /*
     * 新增菜品和对应口味
     * */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 1. 新增菜品
        Dish dish = new Dish();
        dish.setStatus(StatusConstant.DISABLE);
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        //2. 菜品对应的口味
        //前端出入的数据中不包含dish_id,,但是前面创建菜品时 id 已经自动分配好了，我们这里需要获取到 这个id
        //获取 DishMapper 中 insert 语句生成的 主键值（id）
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));

            //插入n个口味
            dishFlavorMapper.insert(flavors);
        }

        //发布事件
        List<Long> ids = Collections.singletonList(dishId);
        applicationContext.publishEvent(new DishChangedEvent(ids,DishChangedEvent.OPERATE_SYNC));

    }

    /*
     * 菜品的分类查询
     */
    public PageResult getDishPage(DishPageQueryDTO dishPageQueryDTO) {
        //开始分页
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.selectPage(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /*
     * 批量删除菜品
     * */
    @Transactional
    public void deleteDishWithFlavor(List<Long> ids) {
        //已经起售的不能删
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 有套餐setmeal关联的不能删
        //根据菜品id查询套餐id
        List<Long> setmealDishId = setmealDishMapper.selectByDishId(ids);
        if (setmealDishId != null && setmealDishId.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //批量删除菜品数据
        dishMapper.deleteById(ids);

        //同时批量删掉对应的口味
        dishFlavorMapper.deleteByDishId(ids);

        //发布事件
        applicationContext.publishEvent(new DishChangedEvent(ids,DishChangedEvent.OPERATE_DELETE));
    }


    /*
     * 根据菜品 id 查询菜品和口味
     * */
    public DishVO getDishWithFlavor(Long id) {
        //查询菜品
        Dish dish = dishMapper.getById(id);
        //查询口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        DishVO dishVO = new DishVO();
        //对象属性拷贝
        BeanUtils.copyProperties(dish, dishVO);

        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /*
     * 修改菜品
     * */
    @Transactional
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //修改菜品
        dishMapper.update(dish);
        //修改口味 --先删除原有口味--然后重新插入口味、
        List<Long> idList = new ArrayList<>();
        idList.add(dishDTO.getId());
        //删除
        dishFlavorMapper.deleteByDishId(idList);
        //插入
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            //插入n个口味
            dishFlavorMapper.insert(flavors);
        }

        //发布事件
        List<Long> ids = Collections.singletonList(dish.getId());
        applicationContext.publishEvent(new DishChangedEvent(ids,DishChangedEvent.OPERATE_SYNC));
    }


    /*
     * 菜品起售停售
     * */
    @Transactional
    public void startOrStop(Integer status, Long id) {
        //如果关联的套餐正起售，菜品则不能停售
        if (status == StatusConstant.DISABLE) {
            //查询该菜品id关联的套餐
            List<Setmeal> setmeals = setmealMapper.getByDishId(id);
            for (Setmeal setmeal : setmeals) {
                if (setmeal.getStatus() == StatusConstant.ENABLE) {
                    throw new SetmealEnableFailedException(MessageConstant.DISH_NOT_FAILED_BECAUSE_SETMEAL);
                }
            }

        }
        Dish dish = dishMapper.getById(id);
        dish.setStatus(status);
        dishMapper.update(dish);

        //发布事件
        List<Long> ids = Collections.singletonList(id);
        applicationContext.publishEvent(new DishChangedEvent(ids,DishChangedEvent.OPERATE_SYNC));
    }


    /*
     *根据分类id查询菜品
     * */
    @Override
    public List<Dish> getDishByCategoryId(Long categoryId) {
        List<Dish> dishes = dishMapper.getDishByCategoryId(categoryId);
        return dishes;
    }

    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}

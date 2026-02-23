package com.campus.service.impl;

import com.campus.event.SetmealChangedEvent;
import com.campus.exception.BaseException;
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
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/*
 * 套餐相关操作
 * */
@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private RBloomFilter<Long> setmealBloomFilter;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private ApplicationContext applicationContext;//注入Spring上下文

    @Autowired
    @Qualifier("myRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SETMEAL_KEY = "setmealCache";
    private static final String SETMEAL_FIELD = "setmealCache::";

    /*
     * 新增套餐
     * */
    @Transactional
    public void insertSetmealWithDish(SetmealDTO setmealDTO) {
        // 清除缓存
        String realHashKey = SETMEAL_FIELD + setmealDTO.getCategoryId();
        Boolean hasKey = redisTemplate.hasKey(realHashKey);
        if (hasKey){
            Long deleteCount = redisTemplate.opsForHash().delete(SETMEAL_KEY, realHashKey);
            log.info("删除缓存的Hash缓存字段{}，成功删除{}个字段", realHashKey, deleteCount);
        }

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

        //发布事件
        List<Long> ids = Collections.singletonList(setmeal.getId());
        applicationContext.publishEvent(new SetmealChangedEvent(ids, SetmealChangedEvent.OPERATE_SYNC));
    }


    /*
     * 套餐分页查询
     * */
    @Transactional
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        //开始分页
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.selectSetmealWithCategory(setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }


    /*
     * 起售停售套餐
     * */
    @Transactional
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
        // 清除缓存
        String realHashKey = SETMEAL_FIELD + setmeal.getCategoryId();
        Boolean hasKey = redisTemplate.hasKey(realHashKey);
        if (hasKey){
            Long deleteCount = redisTemplate.opsForHash().delete(SETMEAL_KEY, realHashKey);
            log.info("删除缓存的Hash缓存字段{}，成功删除{}个字段", realHashKey, deleteCount);
        }

        setmeal.setStatus(status);

        //修改数据
        setmealMapper.update(setmeal);

        //发布事件
        List<Long> ids = Collections.singletonList(id);
        applicationContext.publishEvent(new SetmealChangedEvent(ids, SetmealChangedEvent.OPERATE_SYNC));

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

        // 清除缓存
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.selectById(id);
            String realHashKey = SETMEAL_FIELD + setmeal.getCategoryId();
            Boolean hasKey = redisTemplate.hasKey(realHashKey);
            if (hasKey){
                Long deleteCount = redisTemplate.opsForHash().delete(SETMEAL_KEY, realHashKey);
                log.info("删除缓存的Hash缓存字段{}，成功删除{}个字段", realHashKey, deleteCount);
            }
        }

        //批量删除套餐
        setmealMapper.delete(ids);

        //批量删除 setmeal_dish 中关联的菜品(根据setmeal_id)
        setmealDishMapper.delete(ids);

        //发布事件
        applicationContext.publishEvent(new SetmealChangedEvent(ids, SetmealChangedEvent.OPERATE_DELETE));

    }


    /*
     * 根据id查询套餐
     * */
    @Override
    public SetmealVO getById(Long id) {
        //布隆过滤器
        if (!setmealBloomFilter.contains(id)){
           log.info("布隆过滤器拦截恶意请求，Setmeal ID: {}", id);
           throw new BaseException(MessageConstant.SETMEAL_NOT_FOUND);
        }
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
        String realHashKey = SETMEAL_FIELD + setmealDTO.getId();
        Boolean hasKey = redisTemplate.hasKey(realHashKey);
        if (hasKey){
            Long deleteCount = redisTemplate.opsForHash().delete(SETMEAL_KEY, realHashKey);
            log.info("删除缓存的Hash缓存字段{}，成功删除{}个字段", realHashKey, deleteCount);
        }

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

        //发布事件
        List<Long> syncIds = Collections.singletonList(setmeal.getId());
        applicationContext.publishEvent(new SetmealChangedEvent(syncIds, SetmealChangedEvent.OPERATE_SYNC));

    }


    /**
     * 条件查询
     */
    public List<Setmeal> list(Long categoryId) {
        //先查redis
        List<Setmeal> setmealList = (List<Setmeal>) redisTemplate.opsForHash().get(SETMEAL_KEY, SETMEAL_FIELD + categoryId);
        if (setmealList != null) {
            return setmealList;
        }

        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);
        List<Setmeal> list = setmealMapper.list(setmeal);

        redisTemplate.opsForHash().put(SETMEAL_KEY, SETMEAL_FIELD + categoryId, list);

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

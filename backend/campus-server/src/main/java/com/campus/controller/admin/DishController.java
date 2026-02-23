package com.campus.controller.admin;


import com.campus.dto.DishDTO;
import com.campus.dto.DishPageQueryDTO;
import com.campus.entity.Dish;
import com.campus.result.PageResult;
import com.campus.result.Result;
import com.campus.service.DishService;
import com.campus.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/*
 * 菜品相关操作
 *
 * */
@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关操作")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    @Qualifier("myRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;


    /*
     * 清理redis缓存
     * */
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);  //delete不能识别通配符
        log.info("删除redis缓存数据: {}", pattern);
    }


    /*
     * 新增菜品和对应口味
     * */
    @PostMapping
    @ApiOperation("新增菜品")
    @PreAuthorize("hasAuthority('dish:add')")
    public Result saveWithFlavor(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //删除redis缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /*
     * 菜品的分类查询
     */
    @GetMapping("/page")
    @ApiOperation("菜品的分类查询")
    @PreAuthorize("hasAuthority('dish:list')")
    public Result<PageResult> getDishPage(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品的分类查询:{}", dishPageQueryDTO);

        PageResult pageResult = dishService.getDishPage(dishPageQueryDTO);

        return Result.success(pageResult);
    }

    /*
     * 批量删除菜品
     * */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    @PreAuthorize("hasAuthority('dish:delete')")
    public Result deleteDishWithFlavor(@RequestParam List<Long> ids) {
        log.info("批量删除菜品:{}", ids);

        dishService.deleteDishWithFlavor(ids);

        //删除redis缓存数据,清理所有dish_开头的key
        cleanCache("dish_*");

        return Result.success();
    }

    /*
     * 根据菜品 id 查询菜品和口味 (回显)
     * */
    @GetMapping("/{id}")
    @ApiOperation("根据菜品 id 查询菜品和口味")
    @PreAuthorize("hasAuthority('dish:list')")
    public Result<DishVO> getDishWithFlavor(@PathVariable Long id) throws InterruptedException {
        log.info("根据菜品 id 查询菜品和口味: 菜品id:{}", id);
        //返回DishVO类型
        DishVO dishVO = dishService.getDishWithFlavor(id);
        return Result.success(dishVO);
    }


    /*
     * 修改菜品
     * */
    @PutMapping
    @ApiOperation("修改菜品")
    @PreAuthorize("hasAuthority('dish:edit')")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品:id:{}", dishDTO);
        dishService.update(dishDTO);

        ////删除redis缓存数据,清理所有dish_开头的key
        cleanCache("dish_*");

        return Result.success();
    }

    /*
     * 菜品起售停售
     * */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    @PreAuthorize("hasAuthority('dish:status')")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("菜品起售停售");

        dishService.startOrStop(status, id);

        cleanCache("dish_*");

        return Result.success();
    }

    /*
     *根据分类id查询菜品
     * */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    @PreAuthorize("hasAuthority('dish:list')")
    public Result<List<Dish>> getDishByCategoryId(Long categoryId) {
        log.info("根据分类id查询菜品:分类id：{}", categoryId);

        List<Dish> dishes = dishService.getDishByCategoryId(categoryId);

        return Result.success(dishes);
    }
}

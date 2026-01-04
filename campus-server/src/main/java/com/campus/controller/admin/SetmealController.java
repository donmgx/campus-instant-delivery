package com.campus.controller.admin;

import com.campus.dto.SetmealDTO;
import com.campus.dto.SetmealPageQueryDTO;
import com.campus.result.PageResult;
import com.campus.result.Result;
import com.campus.service.SetmealService;
import com.campus.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "套餐相关操作")
@RestController
@Slf4j
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /*
     * 新增套餐
     * */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    @PreAuthorize("hasAuthority('setmeal:add')")
    public Result insertSetmealWithDish(@RequestBody SetmealDTO setmealDTO) {

        log.info("新增套餐：{}", setmealDTO);

        setmealService.insertSetmealWithDish(setmealDTO);

        return Result.success();
    }


    /*
     * 套餐分页查询
     * */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    @PreAuthorize("hasAuthority('setmeal:list')")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询:{}", setmealPageQueryDTO);

        PageResult pageResult = setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }


    /*
     * 起售停售套餐
     * */
    @PostMapping("/status/{status}")
    @ApiOperation("起售停售套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    @PreAuthorize("hasAuthority('setmeal:status')")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("起售停售套餐:状态：{} 套餐id：{}", status, id);
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    /*
     * 批量删除套餐
     * */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    @PreAuthorize("hasAuthority('setmeal:delete')")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐:{}", ids);

        setmealService.delete(ids);

        return Result.success();
    }


    /*
     * 根据id查询套餐
     * */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    @PreAuthorize("hasAuthority('setmeal:list')")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐");

        SetmealVO setmealVO = setmealService.getById(id);

        return Result.success(setmealVO);

    }

    /*
     * 修改套餐
     * */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    @PreAuthorize("hasAuthority('setmeal:edit')")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐:{}", setmealDTO);

        setmealService.update(setmealDTO);

        return Result.success();
    }
}

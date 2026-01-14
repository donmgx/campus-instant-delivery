package com.campus.controller.user;

import com.campus.constant.StatusConstant;
import com.campus.entity.Setmeal;
import com.campus.entity.es.SetmealDoc;
import com.campus.repository.SetmealDocRepository;
import com.campus.result.Result;
import com.campus.service.SetmealService;
import com.campus.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐浏览接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDocRepository setmealDocRepository;

    /**
     * 条件查询
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId")  //key = setmealCache::categoryId
    public Result<List<Setmeal>> list(Long categoryId) {
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);

        List<Setmeal> list = setmealService.list(setmeal);
        return Result.success(list);
    }

    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品列表")
    public Result<List<DishItemVO>> dishList(@PathVariable("id") Long id) {
        List<DishItemVO> list = setmealService.getDishItemById(id);
        return Result.success(list);
    }


    /**
     * ES搜索套餐接口
     */
    @GetMapping("/search")
    @ApiOperation("套餐搜索(ES)")
    public Result<List<SetmealDoc>> search(String keyWord) {
        log.info("套餐搜索(ES): {}", keyWord);
        List<SetmealDoc> list = setmealDocRepository.searchByKeyword(keyWord, StatusConstant.ENABLE);
        return Result.success(list);
    }
}

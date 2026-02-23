package com.campus.controller.admin;
/*
 * 分类管理
 * */

import com.campus.dto.CategoryDTO;
import com.campus.dto.CategoryPageQueryDTO;
import com.campus.entity.Category;
import com.campus.result.PageResult;
import com.campus.result.Result;
import com.campus.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /*
     * 修改分类
     * */
    @PutMapping
    @ApiOperation("修改分类")
    @PreAuthorize("hasAuthority('category:edit')")
    public Result changeCategory(@RequestBody CategoryDTO categoryDTO) {

        log.info("修改分类:{}", categoryDTO);

        categoryService.changeCategory(categoryDTO);

        return Result.success();
    }

    /*
     * 分类分页查询
     * */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    @PreAuthorize("hasAuthority('category:list')")
    public Result<PageResult> selectPage(CategoryPageQueryDTO categoryPageQueryDTO) {

        log.info("分类分页查询:{}", categoryPageQueryDTO);

        PageResult pageResult = categoryService.selectPage(categoryPageQueryDTO);

        return Result.success(pageResult);
    }

    /*
     * 启用禁用分类
     * */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    @PreAuthorize("hasAuthority('category:status')")
    public Result startOrStop(@PathVariable Integer status, Long id) {

        log.info("启用禁用分类：状态：{},id：{}", status, id);

        categoryService.startOrStop(status, id);

        return Result.success();
    }

    /*
     * 新增分类
     * */
    @PostMapping
    @ApiOperation("新增分类")
    @PreAuthorize("hasAuthority('category:add')")
    public Result save(@RequestBody CategoryDTO categoryDTO) {

        log.info("新增分类：{}", categoryDTO);

        categoryService.save(categoryDTO);

        return Result.success();
    }

    /*
     * 根据id删除分类
     * */
    @DeleteMapping
    @ApiOperation("删除分类")
    @PreAuthorize("hasAuthority('category:delete')")
    public Result delete(Long id) {

        log.info("删除的分类id：{}", id);

        categoryService.delete(id);

        return Result.success();
    }

    /*
     * 根据类型查询分类 ,/admin/category/list
     * */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    @PreAuthorize("hasAuthority('category:list')")
    public Result list(Integer type) {
        log.info("根据类型查询分类:{}", type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}

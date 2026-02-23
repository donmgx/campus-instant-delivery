package com.campus.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.campus.constant.MessageConstant;
import com.campus.constant.StatusConstant;
import com.campus.dto.CategoryDTO;
import com.campus.dto.CategoryPageQueryDTO;
import com.campus.entity.Category;
import com.campus.exception.DeletionNotAllowedException;
import com.campus.mapper.CategoryMapper;
import com.campus.mapper.DishMapper;
import com.campus.mapper.SetmealMapper;
import com.campus.result.PageResult;
import com.campus.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /*
     * 修改分类
     * */
    @Override
    public void changeCategory(CategoryDTO categoryDTO) {

        Category category = new Category();
        //对象属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);

        /*category.setUpdateTime(LocalDateTime.now());

        category.setUpdateUser(BaseContext.getCurrentId());*/

        categoryMapper.update(category);

    }


    /*
     * 分类分页查询
     * */
    @Override
    public PageResult selectPage(CategoryPageQueryDTO categoryPageQueryDTO) {
        //分页
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        Page<Category> page = categoryMapper.selectPage(categoryPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }


    /*
     * 启用禁用分类
     * */
    @Override
    public void startOrStop(Integer status, Long id) {

        Category category = new Category();

        category.setStatus(status);

        category.setId(id);

        categoryMapper.update(category);
    }

    /*
     * 新增分类
     * */
    @Override
    public void save(CategoryDTO categoryDTO) {
        //分类类型：1为菜品分类，2为套餐分类
        Category category = new Category();
        //对象属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);

        category.setStatus(StatusConstant.DISABLE);

        /*category.setCreateTime(LocalDateTime.now());

        category.setCreateUser(BaseContext.getCurrentId());

        category.setUpdateTime(LocalDateTime.now());

        category.setUpdateUser(BaseContext.getCurrentId());*/

        categoryMapper.insert(category);
    }

    /*
     * 根据id删除分类
     * */
    @Override
    public void delete(Long id) {
        //根据id查询关联的菜品
        Integer count = dishMapper.countByCategoryId(id);

        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        //根据id查询关联的套餐
        count = setmealMapper.countByCategoryId(id);

        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //删除
        categoryMapper.delete(id);
    }

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    public List<Category> list(Integer type) {

        return categoryMapper.list(type);
    }

    @Override
    public List<Category> listById(Long categoryId) {
        return categoryMapper.listById(categoryId);
    }
}

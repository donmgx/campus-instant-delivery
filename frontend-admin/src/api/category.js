import request from '../utils/request'

// 分页查询分类
export const getCategoryPageAPI = (params) => request.get('/admin/category/page', { params })

// 新增分类
export const addCategoryAPI = (data) => request.post('/admin/category', data)

// 修改分类
export const updateCategoryAPI = (data) => request.put('/admin/category', data)

// 启用/禁用分类状态
export const changeCategoryStatusAPI = (status, id) => request.post(`/admin/category/status/${status}?id=${id}`)

// 删除分类
export const deleteCategoryAPI = (id) => request.delete(`/admin/category?id=${id}`)

// 根据类型查询所有分类 (用于下拉框：1是菜品分类，2是套餐分类)
export const getCategoryListAPI = (type) => request.get('/admin/category/list', { params: { type } })
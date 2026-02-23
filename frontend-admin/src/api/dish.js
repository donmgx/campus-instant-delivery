import request from '../utils/request'

export const getDishPageAPI = (params) => request.get('/admin/dish/page', { params })
export const addDishAPI = (data) => request.post('/admin/dish', data)
export const updateDishAPI = (data) => request.put('/admin/dish', data)
export const getDishByIdAPI = (id) => request.get(`/admin/dish/${id}`)
export const deleteDishAPI = (ids) => request.delete(`/admin/dish?ids=${ids}`)
export const changeDishStatusAPI = (status, id) => request.post(`/admin/dish/status/${status}?id=${id}`)
// 根据分类ID查询菜品列表 (用于新增套餐时选择菜品)
export const getDishListByCategoryIdAPI = (categoryId) => request.get('/admin/dish/list', { params: { categoryId } })
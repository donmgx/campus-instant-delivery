import request from '../utils/request.js'

export const getCategoryListAPI = () => request.get('/category/list')
export const getDishListAPI = (categoryId) => request.get('/dish/list', { categoryId })
export const getSetmealListAPI = (categoryId) => request.get('/setmeal/list', { categoryId })

// ES 模糊搜索接口
export const searchGoodsAPI = (keyword) => request.get('/dish/search',{ keyword })
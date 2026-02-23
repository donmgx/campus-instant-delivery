import request from '../utils/request.js'

export const getCategoryListAPI = () => request.get('/category/list')
export const getDishListAPI = (categoryId) => request.get('/dish/list', { categoryId })
export const getSetmealListAPI = (categoryId) => request.get('/setmeal/list', { categoryId })

// ES 模糊搜索接口 (请核对你的后端路径和参数名！)
// 如果你的后端是用 keyword 接收，就写 { keyword }；如果是 name，就改成 { name: keyword }
export const searchGoodsAPI = (keyword) => request.get('/dish/search',{ keyword })
import request from '../utils/request.js'

// 1. 获取购物车列表
export const getCartListAPI = () => request.get('/shoppingCart/list')

// 2. 添加购物车 (传 dishId 或 setmealId)
export const addCartAPI = (data) => request.post('/shoppingCart/add', data)

// 3. 减少购物车商品
export const subCartAPI = (data) => request.post('/shoppingCart/sub', data)

// 4. 清空购物车
export const clearCartAPI = () => request.delete('/shoppingCart/clean')
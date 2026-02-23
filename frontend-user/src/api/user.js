import request from '../utils/request.js'

// 1. 微信小程序登录
export const wxLoginAPI = (data) => request.post('/user/login', data)

// 2. 获取店铺营业状态 (1营业 0打烊)
export const getShopStatusAPI = () => request.get('/shop/status')
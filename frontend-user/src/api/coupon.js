import request from '../utils/request.js'

// 1. 查询“我的”优惠券列表 (后端路径: /user/coupon/my)
// 假设 status: 1未使用, 2已使用, 3已过期
export const getMyCouponListAPI = (status) => {
  return request.get('/coupon/my', { status })
}

// 2. 领取优惠券 (后端路径: /user/coupon/claim/{id})
export const claimCouponAPI = (id) => {
  return request.post(`/coupon/claim/${id}`) // 假设领取是 POST 请求
}

// 3. 搜索优惠券 (后端路径: /user/coupon/search)
export const searchCouponAPI = (keyword) => {
  return request.get('/coupon/search', { keyword }) 
}
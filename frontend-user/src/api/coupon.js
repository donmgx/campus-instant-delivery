import request from '../utils/request.js'

// 1. 查询“我的”优惠券列表 (后端路径: /user/coupon/my)
// status: 0未使用, 1已使用, 2已过期
export const getMyCouponListAPI = (status) => {
  return request.get('/coupon/my', { status })
}

// 2. 领取优惠券 (后端路径: /user/coupon/claim/{id})
export const claimCouponAPI = (id) => {
  return request.post(`/coupon/claim/${id}`)
}

// 3. 搜索优惠券 (后端路径: /user/coupon/search)
export const searchCouponAPI = (keyword) => {
  return request.get('/coupon/search', { keyword }) 
}
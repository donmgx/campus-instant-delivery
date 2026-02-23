import request from '../utils/request'

// 分页查询优惠券
export const getCouponPageAPI = (params) => request.get('/admin/coupon/page', { params })

// 新增优惠券
export const addCouponAPI = (data) => request.post('/admin/coupon/add', data)

// 根据ID查询详情，回显
export const getCouponByIdAPI = (id) => request.get(`/admin/coupon/list/${id}`)

// 修改优惠券
export const updateCouponAPI = (data) => request.put('/admin/coupon/update', data)

// 手动开启秒杀活动
export const startSeckillAPI = (id) => request.post(`/admin/coupon/start/${id}`)
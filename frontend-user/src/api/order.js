import request from '../utils/request.js'

// 1. 获取防重提交 Token 
export const getOrderTokenAPI = () => request.get('/token')

// 2. 提交订单
export const submitOrderAPI = (data, orderToken) => {
  return request.post('/order/submit', data, {
    'idempotentToken': orderToken 
  })
}

// 3. 模拟微信支付
export const payOrderAPI = (data) => request.put('/order/payment', data)

// 1. 历史订单分页查询
// params 包含: page, pageSize, status (1待付款 2待接单 3已接单 4派送中 5已完成 6已取消)
export const getOrderListAPI = (params) => request.get('/order/historyOrders', params)

// 2. 查询订单详情
export const getOrderDetailAPI = (id) => request.get(`/order/orderDetail/${id}`)

// 3. 取消订单
export const cancelOrderAPI = (id) => request.put(`/order/cancel/${id}`)

// 4. 再来一单
export const repetitionOrderAPI = (id) => request.post(`/order/repetition/${id}`)
import request from '../utils/request.js' // 这里的 request.js 逻辑和 admin 端一致，需带 token

// 1. 查询待抢订单 (抢单大厅)
export const getWaitListAPI = () => request.get('/rider/order/waitList')

// 2. 骑手抢单 (注意后端是 GET 请求)
export const takeOrderAPI = (orderId) => request.get(`/rider/order/${orderId}`)

// 3. 查看我的待配送订单 (传入 status=4 表示派送中)
export const getRunningListAPI = (status = 4) => request.get('/rider/order/runningList', { params: { status } })

// 4. 完成订单 (核销取餐码)
export const completeOrderAPI = (orderId, pickupCode) => request.post(`/rider/order/complete/${orderId}?pickupCode=${pickupCode}`)
// 骑手取消配送
export const cancelOrderAPI = (orderId) => request.post(`/rider/order/cancel/${orderId}`)

// 修改骑手信息 (包括密码)
export const updateRiderAPI = (data) => request.post('/rider/rider/update', data)
import request from '../utils/request'

export const getOrderPageAPI = (params) => request.get('/admin/order/conditionSearch', { params })
export const getOrderDetailAPI = (id) => request.get(`/admin/order/details/${id}`)
export const confirmOrderAPI = (data) => request.put('/admin/order/confirm', data)
export const rejectOrderAPI = (data) => request.put('/admin/order/rejection', data)
export const cancelOrderAPI = (data) => request.put('/admin/order/cancel', data)
export const deliveryOrderAPI = (id) => request.put(`/admin/order/delivery/${id}`)
export const completeOrderAPI = (id) => request.put(`/admin/order/complete/${id}`)
import request from '../utils/request'

// 获取营业额统计
export const getTurnoverStatsAPI = (begin, end) => request.get('/admin/report/turnoverStatistics', { params: { begin, end } })

// 获取用户统计
export const getUserStatsAPI = (begin, end) => request.get('/admin/report/userStatistics', { params: { begin, end } })

// 获取订单统计
export const getOrderStatsAPI = (begin, end) => request.get('/admin/report/ordersStatistics', { params: { begin, end } })

// 获取销量Top10
export const getTop10StatsAPI = (begin, end) => request.get('/admin/report/top10', { params: { begin, end } })
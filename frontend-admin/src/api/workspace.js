import request from '../utils/request'

// 1. 查询今日运营数据
export const getBusinessDataAPI = () => request.get('/admin/workspace/businessData')

// 2. 查询订单管理概览
export const getOverviewOrdersAPI = () => request.get('/admin/workspace/overviewOrders')

// 3. 查询菜品总览
export const getOverviewDishesAPI = () => request.get('/admin/workspace/overviewDishes')

// 4. 查询套餐总览
export const getOverviewSetmealsAPI = () => request.get('/admin/workspace/overviewSetmeals')
import request from '../utils/request.js'

// 骑手登录接口
export const loginAPI = (data) => request.post('/rider/rider/login', data)

// 骑手注册接口 (留作备用)
export const registerAPI = (data) => request.post('/rider/rider/register', data)

// 获取骑手今日战绩
export const getStatsAPI = (begin, end) => request.get('/rider/stats/statistics', { params: { begin, end } })

// 4. 修改骑手信息/密码
export const updateRiderAPI = (data) => request.post('/rider/rider/update', data)
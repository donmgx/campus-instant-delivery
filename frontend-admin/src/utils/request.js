import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 配合 vite 的代理
  timeout: 10000 // 请求超时时间
})

// 请求拦截器：自动携带 Token
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理错误和解包数据
service.interceptors.response.use(
  response => {
    const res = response.data
    // code 为 1 表示成功
    if (res.code === 1) {
      return res.data // 直接返回 data 部分，页面直接用
    } else {
      ElMessage.error(res.msg || '系统错误')
      return Promise.reject(new Error(res.msg || 'Error'))
    }
  },
  error => {
    if (error.response && error.response.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service
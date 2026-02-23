import axios from 'axios'
import { showFailToast } from 'vant'
import router from '../router'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 配合 vite 的代理
  timeout: 10000 // 请求超时时间
})

// 请求拦截器：自动携带 Token
service.interceptors.request.use(
  config => {
    // 从 localStorage 里拿出来的变量名还叫 token 没关系，这只是前端存的名字
    const token = localStorage.getItem('token')
    if (token) {
      // 发给后端时，把包在外面的名字改成 rider-jwt！
      config.headers['rider-jwt'] = token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理错误和解包数据
service.interceptors.response.use(
  response => {
    const res = response.data
    // 对应你后端的 Result 类，code 为 1 表示成功
    if (res.code === 1) {
      return res.data // 直接返回 data 部分
    } else {
      // 业务逻辑错误，使用 Vant 的轻提示
      showFailToast(res.msg || '系统错误')
      return Promise.reject(new Error(res.msg || 'Error'))
    }
  },
  error => {
    if (error.response && error.response.status === 401) {
      showFailToast('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
    } else {
      showFailToast(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service
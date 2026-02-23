// 项目的后端基础地址
const BASE_URL = 'http://localhost:8080/user'

export const request = (options) => {
  return new Promise((resolve, reject) => {
    // 1. 获取本地存储的 token
    const token = uni.getStorageSync('token')
    
    // 2. 组装请求头
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }
    if (token) {
      header['authentication'] = token
    }

    // 3. 发起请求
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success: (res) => {
        // 🌟 核心拦截 1：处理 HTTP 状态码 401 (Token 过期/未登录)
        if (res.statusCode === 401) {
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/login' })
          }, 1000)
          return reject(new Error('登录过期 (401)'))
        }

        const data = res.data
        
        // 🌟 核心拦截 2：兼容后端返回 200，但业务层面提示未登录的情况
        if (data.code === 0 && (data.msg === 'NOTLOGIN' || (data.msg && data.msg.includes('登录')))) {
          uni.showToast({ title: '身份校验失败，请重新登录', icon: 'none' })
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/login' })
          }, 1000)
          return reject(new Error('身份校验失败'))
        }

        // 正常业务逻辑
        if (data.code === 1) {
          resolve(data.data) 
        } else {
          uni.showToast({
            title: data.msg || '请求失败',
            icon: 'none',
            duration: 2000
          })
          reject(new Error(data.msg || '系统错误'))
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络连接异常，请检查后端',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default {
  get: (url, data, header) => request({ url, method: 'GET', data, header }),
  post: (url, data, header) => request({ url, method: 'POST', data, header }),
  put: (url, data, header) => request({ url, method: 'PUT', data, header }),
  delete: (url, data, header) => request({ url, method: 'DELETE', data, header })
}
import axios from 'axios'
import apiConfig from '../config/api'

// 创建axios实例
const http = axios.create({
  baseURL: apiConfig.REQUEST_BASE_URL,
  timeout: 10000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
http.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    return config
  },
  error => {
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)

// 响应拦截器
http.interceptors.response.use(
  response => {
    // 对响应数据做点什么
    const body = response && response.data
    if (body && typeof body === 'object' && Object.prototype.hasOwnProperty.call(body, 'code')
      && Object.prototype.hasOwnProperty.call(body, 'message')
      && Object.prototype.hasOwnProperty.call(body, 'data')) {
      if (String(body.code) !== '0') {
        const err = new Error(body.message || '请求失败')
        err.response = response
        return Promise.reject(err)
      }
      response.data = body.data
      return response
    }
    return response
  },
  error => {
    // 对响应错误做点什么
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，清除本地登录缓存
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          localStorage.removeItem('adminInfo')
          break
        case 403:
          // 权限不足
          console.error('权限不足')
          break
        case 404:
          // 资源不存在
          console.error('请求的资源不存在')
          break
        case 500:
          // 服务器错误
          console.error('服务器错误')
          break
      }
    }
    return Promise.reject(error)
  }
)

export default http
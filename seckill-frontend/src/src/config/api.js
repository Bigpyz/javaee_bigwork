// API配置文件
const isDevelopment = process.env.NODE_ENV === 'development'

// 根据环境设置不同的API基础URL
const API_BASE_URL = isDevelopment 
  ? 'http://localhost:28080'  // 开发环境
  : 'http://8.130.65.25:28080'  // 生产环境

export default {
  API_BASE_URL,
  // 图片基础URL，用于显示图片
  IMAGE_BASE_URL: API_BASE_URL,
  // API请求基础URL
  REQUEST_BASE_URL: API_BASE_URL
}
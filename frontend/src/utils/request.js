import axios from 'axios'

// 创建 axios 实例
const service = axios.create({
  // vite 代理或实际后端地址
  baseURL: '/api',
  timeout: 5000,
})

// 请求拦截器：自动给请求头加上 Token
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器：处理全局错误
service.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // 这里可以处理 401 Token 过期跳转登录页等逻辑
    console.error('请求出错:', error)
    return Promise.reject(error)
  },
)

export default service

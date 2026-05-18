import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use(config => {
  if (!config.noAuth) {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
})

http.interceptors.response.use(
  res => {
    if (res.data.code === 200) {
      return res.data.data
    }
    ElMessage.error(res.data.msg || '请求失败')
    return Promise.reject(new Error(res.data.msg))
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
      return Promise.reject(err)
    }
    const data = err.response?.data
    if (data && data.msg) {
      ElMessage.error(data.msg)
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(err)
  }
)

export default http

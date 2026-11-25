import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 统一请求封装：自动带 token、统一解包 {code,message,data}、4010 跳登录。
 * 业务错误码见后端 GlobalExceptionHandler。
 */
const instance = axios.create({ baseURL: '/api', timeout: 20000 })

instance.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('token')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

instance.interceptors.response.use(
  (resp) => {
    const r = resp.data
    if (r && typeof r.code === 'number' && r.code !== 0) {
      ElMessage.error(r.message || '请求失败')
      return Promise.reject(new Error(r.message || '请求失败'))
    }
    return r
  },
  (err) => {
    const status = err.response?.status
    const body = err.response?.data
    if (status === 401 || body?.code === 4010) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (router.currentRoute.value.path !== '/login') router.push('/login')
      ElMessage.error('登录已失效，请重新登录')
    } else if (body?.code === 4030) {
      ElMessage.error('无接口权限')
    } else {
      ElMessage.error(body?.message || err.message || '网络异常')
    }
    return Promise.reject(err)
  }
)

export default instance

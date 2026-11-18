import axios from 'axios'

const TOKEN_KEY = 'blogchi_token'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 每个请求自动带上 token，后端 JwtFilter 靠它识别用户
request.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 统一剥掉 Result 外壳，页面里直接拿 data
request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body.code === 200) {
      return body.data
    }
    if (body.code === 401) {
      localStorage.removeItem(TOKEN_KEY)
    }
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
    }
    return Promise.reject(new Error(error.response?.data?.message || '网络异常，请稍后再试'))
  }
)

export { TOKEN_KEY }
export default request

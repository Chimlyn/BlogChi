import request from './request'

// 访客能调用的接口
export const publicApi = {
  articles: (params) => request.get('/public/articles', { params }),
  article: (id, visitorKey) => request.get(`/public/articles/${id}`, { params: { visitorKey } }),
  hot: (limit = 5) => request.get('/public/articles/hot', { params: { limit } }),
  archives: () => request.get('/public/articles/archives'),
  categories: () => request.get('/public/categories'),
  tags: () => request.get('/public/tags'),
  comments: (id, params) => request.get(`/public/articles/${id}/comments`, { params }),
  addComment: (id, data) => request.post(`/public/articles/${id}/comments`, data),
  like: (id, visitorKey) => request.post(`/public/articles/${id}/like`, { visitorKey })
}

// 注册、登录
export const authApi = {
  register: (data) => request.post('/public/auth/register', data),
  login: (data) => request.post('/public/auth/login', data)
}

// 博主后台接口，全部需要 token
export const adminApi = {
  me: () => request.get('/admin/me'),
  stats: () => request.get('/admin/stats'),

  articles: (params) => request.get('/admin/articles', { params }),
  article: (id) => request.get(`/admin/articles/${id}`),
  createArticle: (data) => request.post('/admin/articles', data),
  updateArticle: (id, data) => request.put(`/admin/articles/${id}`, data),
  deleteArticle: (id) => request.delete(`/admin/articles/${id}`),

  categories: () => request.get('/admin/categories'),
  createCategory: (data) => request.post('/admin/categories', data),
  updateCategory: (id, data) => request.put(`/admin/categories/${id}`, data),
  deleteCategory: (id) => request.delete(`/admin/categories/${id}`),

  tags: () => request.get('/admin/tags'),
  createTag: (data) => request.post('/admin/tags', data),
  updateTag: (id, data) => request.put(`/admin/tags/${id}`, data),
  deleteTag: (id) => request.delete(`/admin/tags/${id}`),

  comments: (params) => request.get('/admin/comments', { params }),
  deleteComment: (id) => request.delete(`/admin/comments/${id}`)
}

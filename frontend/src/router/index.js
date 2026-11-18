import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/article/:id',
    name: 'article',
    component: () => import('../views/ArticleDetail.vue')
  },
  {
    path: '/admin/login',
    name: 'login',
    component: () => import('../views/admin/Login.vue')
  },
  {
    path: '/admin',
    component: () => import('../views/admin/Layout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'articles',
        name: 'admin-articles',
        component: () => import('../views/admin/ArticleList.vue')
      },
      {
        path: 'articles/new',
        name: 'admin-article-new',
        component: () => import('../views/admin/ArticleEdit.vue')
      },
      {
        path: 'articles/:id/edit',
        name: 'admin-article-edit',
        component: () => import('../views/admin/ArticleEdit.vue')
      },
      {
        path: 'categories',
        name: 'admin-categories',
        component: () => import('../views/admin/CategoryList.vue')
      },
      {
        path: 'tags',
        name: 'admin-tags',
        component: () => import('../views/admin/TagList.vue')
      },
      {
        path: 'comments',
        name: 'admin-comments',
        component: () => import('../views/admin/CommentList.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 后台页面必须登录，没有 token 直接跳登录页
router.beforeEach((to) => {
  if (to.path.startsWith('/admin') && to.name !== 'login') {
    const token = localStorage.getItem('blogchi_token')
    if (!token) {
      return { name: 'login' }
    }
  }
  return true
})

export default router

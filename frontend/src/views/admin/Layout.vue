<template>
  <div class="admin-shell">
    <aside class="admin-side">
      <div class="logo">Blog<span>Chi</span> 后台</div>
      <router-link to="/admin/dashboard" active-class="active">数据概览</router-link>
      <router-link to="/admin/articles" active-class="active">文章管理</router-link>
      <router-link to="/admin/categories" active-class="active">分类管理</router-link>
      <router-link to="/admin/tags" active-class="active">标签管理</router-link>
      <router-link to="/admin/comments" active-class="active">评论管理</router-link>

      <div class="side-bottom">
        <router-link to="/">查看前台博客</router-link>
        <span>{{ store.user?.nickname || '' }}</span>
        <a href="#" @click.prevent="logout">退出登录</a>
      </div>
    </aside>

    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const router = useRouter()

function logout() {
  store.logout()
  router.push('/admin/login')
}

onMounted(() => {
  store.loadMe()
})
</script>

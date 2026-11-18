<template>
  <div>
    <div class="admin-title">
      <h2>数据概览</h2>
      <span style="color: var(--muted); font-size: 13px">只统计你自己的数据</span>
    </div>

    <div class="stat-grid">
      <div class="card stat-card">
        <div class="label">文章总数</div>
        <div class="value">{{ stats.articleCount }}</div>
      </div>
      <div class="card stat-card">
        <div class="label">已发布</div>
        <div class="value">{{ stats.publishedCount }}</div>
      </div>
      <div class="card stat-card">
        <div class="label">草稿</div>
        <div class="value">{{ stats.draftCount }}</div>
      </div>
      <div class="card stat-card">
        <div class="label">评论数</div>
        <div class="value">{{ stats.commentCount }}</div>
      </div>
      <div class="card stat-card">
        <div class="label">总阅读量</div>
        <div class="value">{{ stats.viewCount }}</div>
      </div>
      <div class="card stat-card">
        <div class="label">分类 / 标签</div>
        <div class="value">{{ stats.categoryCount }} / {{ stats.tagCount }}</div>
      </div>
    </div>

    <div class="card card-pad" style="margin-top: 22px">
      <h3 style="font-size: 16px; margin-bottom: 10px">下一步做什么</h3>
      <p style="color: var(--ink-soft); margin: 0; font-size: 14px">
        先去「分类管理」建一个分类，再去「标签管理」加几个标签，然后到「文章管理」写第一篇 Markdown 文章。
      </p>
      <div style="margin-top: 16px; display: flex; gap: 10px">
        <router-link class="btn btn-primary" to="/admin/articles/new">写文章</router-link>
        <router-link class="btn" to="/admin/categories">管理分类</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { adminApi } from '../../api'

const stats = reactive({
  articleCount: 0,
  publishedCount: 0,
  draftCount: 0,
  commentCount: 0,
  viewCount: 0,
  categoryCount: 0,
  tagCount: 0
})

onMounted(async () => {
  try {
    const data = await adminApi.stats()
    Object.assign(stats, data)
  } catch (e) {
    // 概览失败不影响其他页面
  }
})
</script>

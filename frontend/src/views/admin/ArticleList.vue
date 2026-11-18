<template>
  <div>
    <div class="admin-title">
      <h2>文章管理</h2>
      <router-link class="btn btn-primary" to="/admin/articles/new">写文章</router-link>
    </div>

    <div class="card card-pad" style="margin-bottom: 18px">
      <div class="toolbar">
        <input
          v-model="keyword"
          class="input"
          placeholder="按标题搜索"
          @keyup.enter="search"
        />
        <select v-model="status" class="select" style="width: 140px" @change="search">
          <option :value="null">全部状态</option>
          <option :value="1">已发布</option>
          <option :value="0">草稿</option>
        </select>
        <button class="btn" @click="search">刷新</button>
      </div>
      <div class="toast" v-if="error" style="margin: 14px 0 0">{{ error }}</div>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>标题</th>
          <th>分类</th>
          <th>状态</th>
          <th>阅读</th>
          <th>创建时间</th>
          <th style="width: 160px">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="a in list" :key="a.id">
          <td>{{ a.title }}</td>
          <td>{{ a.categoryName }}</td>
          <td>
            <span class="badge" :class="a.status === 1 ? 'published' : 'draft'">
              {{ a.status === 1 ? '已发布' : '草稿' }}
            </span>
          </td>
          <td>{{ a.viewCount }}</td>
          <td>{{ a.createTime }}</td>
          <td>
            <div class="ops">
              <button class="btn btn-sm" @click="edit(a.id)">编辑</button>
              <button class="btn btn-sm btn-danger" @click="remove(a)">删除</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="list.length === 0" class="empty">还没有文章，点右上角写一篇吧</div>

    <Pagination :page="page" :size="size" :total="total" @change="turn" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Pagination from '../../components/Pagination.vue'
import { adminApi } from '../../api'

const router = useRouter()
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const status = ref(null)
const error = ref('')

async function load() {
  try {
    const data = await adminApi.articles({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      status: status.value === null ? undefined : status.value
    })
    list.value = data.records
    total.value = data.total
  } catch (e) {
    error.value = e.message
  }
}

function search() {
  page.value = 1
  load()
}

function turn(target) {
  page.value = target
  load()
}

function edit(id) {
  router.push(`/admin/articles/${id}/edit`)
}

async function remove(article) {
  if (!window.confirm(`确定删除《${article.title}》吗？评论和标签关联会一起删除。`)) return
  try {
    await adminApi.deleteArticle(article.id)
    load()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

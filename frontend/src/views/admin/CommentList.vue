<template>
  <div>
    <div class="admin-title">
      <h2>评论管理</h2>
      <span style="color: var(--muted); font-size: 13px">展示你所有文章下的评论</span>
    </div>

    <div class="toast" v-if="error">{{ error }}</div>

    <table class="table">
      <thead>
        <tr>
          <th>所属文章</th>
          <th>昵称</th>
          <th>内容</th>
          <th style="width: 160px">时间</th>
          <th style="width: 100px">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="c in list" :key="c.id">
          <td style="max-width: 220px">{{ c.articleTitle }}</td>
          <td>{{ c.nickname }}</td>
          <td style="max-width: 320px">{{ c.content }}</td>
          <td>{{ c.createTime }}</td>
          <td>
            <button class="btn btn-sm btn-danger" @click="remove(c)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="list.length === 0" class="empty">还没有评论</div>

    <Pagination :page="page" :size="size" :total="total" @change="turn" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import Pagination from '../../components/Pagination.vue'
import { adminApi } from '../../api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const error = ref('')

async function load() {
  try {
    const data = await adminApi.comments({ page: page.value, size })
    list.value = data.records
    total.value = data.total
  } catch (e) {
    error.value = e.message
  }
}

function turn(target) {
  page.value = target
  load()
}

async function remove(comment) {
  if (!window.confirm(`确定删除 ${comment.nickname} 的这条评论吗？`)) return
  try {
    await adminApi.deleteComment(comment.id)
    load()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

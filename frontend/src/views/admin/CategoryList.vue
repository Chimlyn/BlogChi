<template>
  <div>
    <div class="admin-title">
      <h2>分类管理</h2>
      <span style="color: var(--muted); font-size: 13px">分类下还有文章时不允许删除</span>
    </div>

    <div class="card card-pad" style="margin-bottom: 18px">
      <div class="toolbar">
        <input
          v-model="name"
          class="input"
          maxlength="20"
          placeholder="新的分类名，例如「技术笔记」"
          @keyup.enter="add"
        />
        <button class="btn btn-primary" @click="add">新增分类</button>
      </div>
      <div class="toast" v-if="error" style="margin: 14px 0 0">{{ error }}</div>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>分类名</th>
          <th style="width: 120px">文章数</th>
          <th style="width: 160px">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="c in list" :key="c.id">
          <td>{{ c.name }}</td>
          <td>{{ c.articleCount }}</td>
          <td>
            <div class="ops">
              <button class="btn btn-sm" @click="rename(c)">重命名</button>
              <button class="btn btn-sm btn-danger" @click="remove(c)">删除</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="list.length === 0" class="empty">还没有分类，先建一个吧</div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '../../api'

const list = ref([])
const name = ref('')
const error = ref('')

async function load() {
  try {
    list.value = await adminApi.categories()
  } catch (e) {
    error.value = e.message
  }
}

async function add() {
  error.value = ''
  if (!name.value.trim()) {
    error.value = '分类名不能为空'
    return
  }
  try {
    await adminApi.createCategory({ name: name.value.trim() })
    name.value = ''
    load()
  } catch (e) {
    error.value = e.message
  }
}

async function rename(category) {
  const value = window.prompt('新的分类名', category.name)
  if (!value || !value.trim()) return
  try {
    await adminApi.updateCategory(category.id, { name: value.trim() })
    load()
  } catch (e) {
    error.value = e.message
  }
}

async function remove(category) {
  if (!window.confirm(`确定删除分类「${category.name}」吗？`)) return
  try {
    await adminApi.deleteCategory(category.id)
    load()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

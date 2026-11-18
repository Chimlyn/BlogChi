<template>
  <div>
    <div class="admin-title">
      <h2>标签管理</h2>
      <span style="color: var(--muted); font-size: 13px">删除标签会同时移除它与文章的关联</span>
    </div>

    <div class="card card-pad" style="margin-bottom: 18px">
      <div class="toolbar">
        <input
          v-model="name"
          class="input"
          maxlength="20"
          placeholder="新的标签名，例如「Spring Boot」"
          @keyup.enter="add"
        />
        <button class="btn btn-primary" @click="add">新增标签</button>
      </div>
      <div class="toast" v-if="error" style="margin: 14px 0 0">{{ error }}</div>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>标签名</th>
          <th style="width: 120px">关联文章</th>
          <th style="width: 160px">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="t in list" :key="t.id">
          <td><span class="tag">{{ t.name }}</span></td>
          <td>{{ t.articleCount }}</td>
          <td>
            <div class="ops">
              <button class="btn btn-sm" @click="rename(t)">重命名</button>
              <button class="btn btn-sm btn-danger" @click="remove(t)">删除</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="list.length === 0" class="empty">还没有标签</div>
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
    list.value = await adminApi.tags()
  } catch (e) {
    error.value = e.message
  }
}

async function add() {
  error.value = ''
  if (!name.value.trim()) {
    error.value = '标签名不能为空'
    return
  }
  try {
    await adminApi.createTag({ name: name.value.trim() })
    name.value = ''
    load()
  } catch (e) {
    error.value = e.message
  }
}

async function rename(tag) {
  const value = window.prompt('新的标签名', tag.name)
  if (!value || !value.trim()) return
  try {
    await adminApi.updateTag(tag.id, { name: value.trim() })
    load()
  } catch (e) {
    error.value = e.message
  }
}

async function remove(tag) {
  if (!window.confirm(`确定删除标签「${tag.name}」吗？`)) return
  try {
    await adminApi.deleteTag(tag.id)
    load()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

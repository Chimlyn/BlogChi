<template>
  <div class="login-wrap">
    <div class="card login-card">
      <h1>Blog<span style="color: var(--accent)">Chi</span> 后台</h1>
      <p class="sub">{{ isRegister ? '注册一个博主账号，开始写文章' : '登录后管理你的分类、标签和文章' }}</p>

      <div class="toast" v-if="error">{{ error }}</div>

      <div class="field">
        <label>用户名</label>
        <input v-model="form.username" class="input" placeholder="3-20 个字符" />
      </div>

      <div class="field" v-if="isRegister">
        <label>昵称</label>
        <input v-model="form.nickname" class="input" placeholder="前台展示的名字" />
      </div>

      <div class="field">
        <label>密码</label>
        <input
          v-model="form.password"
          type="password"
          class="input"
          placeholder="至少 6 位"
          @keyup.enter="submit"
        />
      </div>

      <button class="btn btn-primary" style="width: 100%" :disabled="loading" @click="submit">
        {{ loading ? '请稍候…' : isRegister ? '注册并登录' : '登录' }}
      </button>

      <div class="login-switch">
        <template v-if="isRegister">
          已经有账号？<span @click="toggle">去登录</span>
        </template>
        <template v-else>
          还没有账号？<span @click="toggle">注册一个</span>
        </template>
      </div>

      <div class="login-switch">
        <router-link to="/">返回博客首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()

const isRegister = ref(false)
const loading = ref(false)
const error = ref('')
const form = reactive({ username: '', nickname: '', password: '' })

function toggle() {
  isRegister.value = !isRegister.value
  error.value = ''
}

async function submit() {
  error.value = ''
  if (!form.username || !form.password) {
    error.value = '用户名和密码不能为空'
    return
  }
  if (isRegister.value && !form.nickname) {
    error.value = '注册需要填写昵称'
    return
  }

  loading.value = true
  try {
    if (isRegister.value) {
      await store.register({
        username: form.username,
        nickname: form.nickname,
        password: form.password
      })
    } else {
      await store.login({ username: form.username, password: form.password })
    }
    router.push('/admin/dashboard')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

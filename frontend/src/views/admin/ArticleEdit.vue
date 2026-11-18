<template>
  <div>
    <div class="admin-title">
      <h2>{{ isEdit ? '编辑文章' : '写文章' }}</h2>
      <div style="display: flex; gap: 10px">
        <button class="btn" @click="back">返回列表</button>
        <button class="btn btn-primary" :disabled="saving" @click="save(1)">
          {{ saving ? '保存中…' : '保存并发布' }}
        </button>
      </div>
    </div>

    <div class="toast" v-if="error">{{ error }}</div>

    <div class="editor-grid">
      <div>
        <div class="card card-pad">
          <div class="field">
            <label>标题</label>
            <input v-model="form.title" class="input" maxlength="100" placeholder="给文章起个名字" />
          </div>

          <div class="field">
            <label>摘要（不填会自动截取正文前 120 字）</label>
            <input v-model="form.summary" class="input" maxlength="500" placeholder="一句话介绍这篇文章" />
          </div>

          <div class="field">
            <label>分类</label>
            <select v-model="form.categoryId" class="select">
              <option :value="null" disabled>请选择分类</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
            <span v-if="categories.length === 0" style="font-size: 12px; color: var(--danger)">
              还没有分类，先去「分类管理」建一个
            </span>
          </div>

          <div class="field">
            <label>标签（可多选）</label>
            <div class="checkbox-group">
              <label v-for="t in tags" :key="t.id">
                <input type="checkbox" :value="t.id" v-model="form.tagIds" />
                {{ t.name }}
              </label>
              <span v-if="tags.length === 0" style="font-size: 12px; color: var(--muted)">
                还没有标签，可以先去「标签管理」加几个
              </span>
            </div>
          </div>

          <div class="field">
            <label>正文（Markdown）</label>
            <textarea
              v-model="form.content"
              class="textarea code"
              placeholder="支持 Markdown：## 标题、- 列表、**加粗**、```代码块```"
            ></textarea>
          </div>

          <div style="display: flex; gap: 10px; justify-content: flex-end">
            <button class="btn" :disabled="saving" @click="save(0)">存为草稿</button>
            <button class="btn btn-primary" :disabled="saving" @click="save(1)">保存并发布</button>
          </div>
        </div>
      </div>

      <div class="preview-pane">
        <h2 style="font-size: 20px; margin-bottom: 12px">{{ form.title || '标题预览' }}</h2>
        <div class="md-body" v-html="preview"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminApi } from '../../api'
import { renderMarkdown } from '../../utils/markdown'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = reactive({
  title: '',
  summary: '',
  content: '',
  categoryId: null,
  status: 1,
  tagIds: []
})

const categories = ref([])
const tags = ref([])
const error = ref('')
const saving = ref(false)

const preview = computed(() => renderMarkdown(form.content))

async function loadMeta() {
  try {
    const [c, t] = await Promise.all([adminApi.categories(), adminApi.tags()])
    categories.value = c
    tags.value = t
  } catch (e) {
    error.value = e.message
  }
}

async function loadArticle() {
  if (!isEdit) return
  try {
    const data = await adminApi.article(route.params.id)
    form.title = data.title
    form.summary = data.summary || ''
    form.content = data.content
    form.categoryId = data.categoryId
    form.status = data.status
    form.tagIds = data.tagIds || []
  } catch (e) {
    error.value = e.message
  }
}

async function save(status) {
  error.value = ''
  if (!form.title.trim() || !form.content.trim()) {
    error.value = '标题和正文不能为空'
    return
  }
  if (!form.categoryId) {
    error.value = '请先选择一个分类'
    return
  }

  saving.value = true
  try {
    const payload = { ...form, status }
    if (isEdit) {
      await adminApi.updateArticle(route.params.id, payload)
    } else {
      await adminApi.createArticle(payload)
    }
    router.push('/admin/articles')
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

function back() {
  router.push('/admin/articles')
}

onMounted(() => {
  loadMeta()
  loadArticle()
})
</script>

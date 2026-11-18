<template>
  <SiteHeader />
  <main class="page">
    <div class="container">
      <div v-if="detail" class="card article-body">
        <div class="article-head">
          <h1>{{ detail.title }}</h1>
          <div class="meta-row">
            <span>{{ detail.authorName }}</span>
            <span>{{ detail.createTime }}</span>
            <span v-if="detail.categoryName" class="tag">{{ detail.categoryName }}</span>
            <span v-for="t in detail.tags" :key="t" class="tag">{{ t }}</span>
            <span style="margin-left: auto">{{ detail.viewCount }} 阅读</span>
          </div>
        </div>

        <div ref="bodyRef" class="md-body" v-html="html"></div>

        <div style="margin-top: 32px; text-align: center">
          <button class="btn" :class="{ 'btn-primary': detail.liked }" @click="toggleLike">
            {{ detail.liked ? '已点赞' : '点赞' }} {{ detail.likeCount }}
          </button>
        </div>
      </div>

      <div v-if="detail" class="card card-pad" style="margin-top: 20px">
        <div class="section-title">
          <h2>评论（{{ commentTotal }}）</h2>
          <span class="hint">不需要登录，留个昵称就能说两句</span>
        </div>

        <div class="field">
          <label>昵称</label>
          <input v-model="form.nickname" class="input" maxlength="20" placeholder="怎么称呼你" />
        </div>
        <div class="field">
          <label>评论内容</label>
          <textarea
            v-model="form.content"
            class="textarea"
            maxlength="1000"
            placeholder="说点什么吧，友善一点"
          ></textarea>
        </div>
        <div class="toast" v-if="error">{{ error }}</div>
        <button class="btn btn-primary" :disabled="sending" @click="submitComment">
          {{ sending ? '提交中…' : '发表评论' }}
        </button>

        <div style="margin-top: 22px">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="who">
              <b>{{ c.nickname }}</b>
              <span>{{ c.createTime }}</span>
            </div>
            <div>{{ c.content }}</div>
          </div>
          <div v-if="comments.length === 0" class="empty">还没有评论，来抢个沙发</div>
        </div>

        <Pagination :page="cpage" :size="csize" :total="commentTotal" @change="turnComment" />
      </div>

      <div v-if="!detail && loaded" class="empty">文章不存在或已下架</div>
    </div>
  </main>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import hljs from '../utils/highlight'
import SiteHeader from '../components/SiteHeader.vue'
import Pagination from '../components/Pagination.vue'
import { publicApi } from '../api'
import { getVisitorKey, renderMarkdown } from '../utils/markdown'

const route = useRoute()
const id = route.params.id
const visitorKey = getVisitorKey()

const detail = ref(null)
const html = ref('')
const bodyRef = ref(null)
const loaded = ref(false)

const comments = ref([])
const commentTotal = ref(0)
const cpage = ref(1)
const csize = 10

const form = reactive({ nickname: '', content: '' })
const error = ref('')
const sending = ref(false)

async function loadDetail() {
  try {
    detail.value = await publicApi.article(id, visitorKey)
    html.value = renderMarkdown(detail.value.content)
    // v-html 渲染完再交给 highlight.js 处理代码块
    await nextTick()
    bodyRef.value?.querySelectorAll('pre code').forEach((block) => {
      hljs.highlightElement(block)
    })
  } catch (e) {
    detail.value = null
  } finally {
    loaded.value = true
  }
}

async function loadComments() {
  try {
    const data = await publicApi.comments(id, { page: cpage.value, size: csize })
    comments.value = data.records
    commentTotal.value = data.total
  } catch (e) {
    comments.value = []
  }
}

async function toggleLike() {
  try {
    const data = await publicApi.like(id, visitorKey)
    detail.value.liked = data.liked
    detail.value.likeCount = data.likeCount
  } catch (e) {
    error.value = e.message
  }
}

async function submitComment() {
  error.value = ''
  if (!form.nickname.trim() || !form.content.trim()) {
    error.value = '昵称和评论内容都要填哦'
    return
  }
  sending.value = true
  try {
    await publicApi.addComment(id, { nickname: form.nickname, content: form.content })
    form.content = ''
    cpage.value = 1
    await loadComments()
  } catch (e) {
    error.value = e.message
  } finally {
    sending.value = false
  }
}

function turnComment(target) {
  cpage.value = target
  loadComments()
}

onMounted(() => {
  loadDetail()
  loadComments()
})
</script>

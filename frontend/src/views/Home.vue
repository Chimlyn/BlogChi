<template>
  <SiteHeader />
  <main class="page">
    <div class="container">
      <div class="home-grid">
        <section>
          <div class="card card-pad" style="margin-bottom: 18px">
            <div class="toolbar">
              <input
                v-model="keyword"
                class="input"
                placeholder="搜索标题或正文关键词"
                @keyup.enter="search"
              />
              <button class="btn btn-primary" @click="search">搜索</button>
              <button class="btn" @click="reset">重置</button>
            </div>

            <div class="chip-wrap" style="margin-top: 14px">
              <span
                class="tag clickable"
                :class="{ active: categoryId === null }"
                @click="pickCategory(null)"
                >全部分类</span
              >
              <span
                v-for="c in categories"
                :key="c.id"
                class="tag clickable"
                :class="{ active: categoryId === c.id }"
                @click="pickCategory(c.id)"
                >{{ c.name }} ({{ c.articleCount }})</span
              >
            </div>

            <div class="chip-wrap" style="margin-top: 10px">
              <span
                class="tag clickable"
                :class="{ active: tagId === null }"
                @click="pickTag(null)"
                >全部标签</span
              >
              <span
                v-for="t in tags"
                :key="t.id"
                class="tag clickable"
                :class="{ active: tagId === t.id }"
                @click="pickTag(t.id)"
                >{{ t.name }}</span
              >
            </div>
          </div>

          <div class="article-list">
            <article v-for="item in list" :key="item.id" class="card article-card">
              <h3>
                <router-link :to="`/article/${item.id}`">{{ item.title }}</router-link>
              </h3>
              <p class="summary">{{ item.summary }}</p>
              <div class="meta-row">
                <span>{{ item.authorName }}</span>
                <span>{{ item.createTime }}</span>
                <span v-if="item.categoryName" class="tag">{{ item.categoryName }}</span>
                <span v-for="t in item.tags" :key="t" class="tag">{{ t }}</span>
                <span style="margin-left: auto">{{ item.viewCount }} 阅读</span>
              </div>
            </article>
          </div>

          <div v-if="!loading && list.length === 0" class="empty">还没有符合条件的文章</div>

          <Pagination :page="page" :size="size" :total="total" @change="turn" />
        </section>

        <aside>
          <div class="card card-pad side-block">
            <h3>热门文章</h3>
            <div class="side-list">
              <router-link
                v-for="h in hot"
                :key="h.id"
                :to="`/article/${h.id}`"
                class="row"
              >
                <span>{{ h.title }}</span>
                <em>{{ h.viewCount }}</em>
              </router-link>
              <span v-if="hot.length === 0" class="empty" style="padding: 10px 0">暂无数据</span>
            </div>
          </div>

          <div class="card card-pad side-block">
            <h3>文章归档</h3>
            <div class="side-list">
              <span v-for="a in archives" :key="a.month" class="row">
                <span>{{ a.month }}</span>
                <em>{{ a.total }} 篇</em>
              </span>
              <span v-if="archives.length === 0" class="empty" style="padding: 10px 0">暂无数据</span>
            </div>
          </div>

          <div class="card card-pad side-block">
            <h3>关于本站</h3>
            <p style="color: var(--ink-soft); font-size: 14px; margin: 0">
              BlogChi 是一个多博主共用的博客平台，博主写 Markdown，访客自由浏览、评论和点赞。
            </p>
          </div>
        </aside>
      </div>
    </div>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import SiteHeader from '../components/SiteHeader.vue'
import Pagination from '../components/Pagination.vue'
import { publicApi } from '../api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const categoryId = ref(null)
const tagId = ref(null)
const categories = ref([])
const tags = ref([])
const hot = ref([])
const archives = ref([])
const loading = ref(false)

async function loadList() {
  loading.value = true
  try {
    const data = await publicApi.articles({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      categoryId: categoryId.value || undefined,
      tagId: tagId.value || undefined
    })
    list.value = data.records
    total.value = data.total
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadSide() {
  try {
    const [c, t, h, a] = await Promise.all([
      publicApi.categories(),
      publicApi.tags(),
      publicApi.hot(),
      publicApi.archives()
    ])
    categories.value = c
    tags.value = t
    hot.value = h
    archives.value = a
  } catch (e) {
    // 侧边栏拉取失败不影响主列表
  }
}

function search() {
  page.value = 1
  loadList()
}

function reset() {
  keyword.value = ''
  categoryId.value = null
  tagId.value = null
  page.value = 1
  loadList()
}

function pickCategory(id) {
  categoryId.value = id
  page.value = 1
  loadList()
}

function pickTag(id) {
  tagId.value = id
  page.value = 1
  loadList()
}

function turn(target) {
  page.value = target
  loadList()
}

onMounted(() => {
  loadList()
  loadSide()
})
</script>

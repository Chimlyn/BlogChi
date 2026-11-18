import { marked } from 'marked'
import DOMPurify from 'dompurify'

// Markdown 渲染 + XSS 过滤：渲染结果一定先过 DOMPurify 再插入页面
marked.setOptions({ breaks: true, gfm: true })

export function renderMarkdown(source) {
  if (!source) return ''
  return DOMPurify.sanitize(marked.parse(source))
}

// 访客唯一标识：第一次访问时生成并存在 localStorage，点赞去重靠它
export function getVisitorKey() {
  const key = 'blogchi_visitor_key'
  let value = localStorage.getItem(key)
  if (!value) {
    value = 'v-' + Math.random().toString(36).slice(2) + Date.now().toString(36)
    localStorage.setItem(key, value)
  }
  return value
}

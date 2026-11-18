# BlogChi 前端

BlogChi 多博主博客平台的前端，Vue 3 + Vite。包含两套界面：

- 前台博客站：文章列表、搜索筛选、详情、评论、点赞
- 博主后台：登录注册、文章编辑器（Markdown + 实时预览）、分类 / 标签 / 评论管理

## 技术栈

| 用途 | 选型 |
| --- | --- |
| 框架 | Vue 3（`<script setup>` 组合式 API） |
| 构建 | Vite 6 |
| 路由 | Vue Router 4 |
| 状态 | Pinia |
| 请求 | axios（统一拦截器） |
| Markdown | marked + DOMPurify（防 XSS） |
| 代码高亮 | highlight.js |

## 目录结构

```
src/
├── api/
│   ├── request.js      axios 实例、token 注入、统一拆包
│   └── index.js        所有接口函数（publicApi / authApi / adminApi）
├── components/         SiteHeader、Pagination
├── router/index.js     路由 + 后台登录守卫
├── stores/user.js      登录态（token 存 localStorage）
├── styles/main.css     全局样式
├── utils/markdown.js   Markdown 渲染 + 访客标识
└── views/
    ├── Home.vue             首页：列表 + 搜索 + 分类标签筛选 + 侧栏
    ├── ArticleDetail.vue    详情：Markdown 渲染、评论、点赞
    └── admin/               后台：登录、概览、文章、分类、标签、评论
```

## 运行

先启动后端（默认 8081），再：

```bash
npm install
npm run dev
```

打开 http://localhost:5173 。`vite.config.js` 里已经把 `/api` 代理到 `http://localhost:8081`，
所以本地开发不需要处理跨域。

打包：

```bash
npm run build
```

产物在 `dist/`，可以直接丢给 Nginx 或任意静态服务器。

## 演示账号

数据库脚本里内置了两个博主，密码都是 `123456`：

| 用户名 | 昵称 |
| --- | --- |
| admin | 小林同学 |
| demo | 小周同学 |

## 实现要点

- **Markdown 一定先净化再渲染**：`marked` 转 HTML 后统一过 `DOMPurify.sanitize`，避免文章里写脚本
- **访客标识**：点赞需要区分访客，前端首访时生成一个 uuid 存 `localStorage`，作为 `visitorKey` 传给后端
- **统一错误处理**：axios 响应拦截器把 `{code,message,data}` 拆成 `data`，非 200 直接 reject 成 `Error`
- **后台守卫**：路由 `beforeEach` 判断本地是否有 token，没有就跳登录页

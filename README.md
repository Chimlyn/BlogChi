# BlogChi · 博客系统

一个前后端分离的个人博客练习项目，目前仍在进一步维护
访客可以浏览文章、搜索、查看分类和标签、评论和点赞；博主登录后可以管理自己的文章、分类、标签和评论。

## 技术栈

- 后端：Spring Boot、MyBatis-Flex、MySQL、JWT、BCrypt
- 前端：Vue 3、Vue Router、Pinia、Vite、axios
- 其他：Markdown 渲染、代码高亮、评论点赞

## 功能

### 访客端
- 文章列表、关键词搜索、按分类和标签筛选、分页
- 文章详情：Markdown 渲染、阅读量、点赞、评论
- 热门文章、归档、分类和标签列表

### 博主端
- 注册、登录，登录后返回 token
- 文章管理：写草稿、发布、编辑、删除
- 分类和标签管理
- 查看和删除自己文章下的评论
- 简单数据统计

## 数据库

主要包含 7 张表：

- `blog_user`：用户
- `category`：分类
- `tag`：标签
- `article`：文章
- `article_tag`：文章和标签关联表
- `article_comment`：评论
- `article_like`：点赞记录

## 接口说明

接口统一返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

常见状态码：

- 200：成功
- 400：参数错误
- 401：未登录或 token 无效
- 403：没有权限操作他人数据
- 404：资源不存在
- 409：数据冲突
- 500：服务器错误

后台接口通过请求头携带 token：

```http
Authorization: Bearer <token>
```

## 本地运行

### 环境要求

- JDK 25
- Maven 3.9+
- MySQL 8.0
- Node.js 18+

### 1. 初始化数据库

```bash
mysql -u root -p < sql/blogchi.sql
```

### 2. 修改数据库配置

编辑 `backend/src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blogchi?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认端口：`8081`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认端口：`5173`，开发环境下 `/api` 会代理到后端 `8081`。

## 目录结构

```text
backend/
├─ sql/                  建表脚本和演示数据
└─ src/main/java/com/blogchi/
   ├─ common/            通用返回对象、分页对象
   ├─ config/            配置类
   ├─ security/          JWT 工具、过滤器、登录用户
   ├─ exception/         业务异常和全局异常处理
   ├─ entity/            数据库实体
   ├─ mapper/            数据访问层
   ├─ dto/               请求参数
   ├─ vo/                返回视图对象
   ├─ service/           业务逻辑
   └─ controller/        接口层

frontend/
└─ src/
   ├─ api/               axios 封装和接口定义
   ├─ stores/            Pinia 状态
   ├─ router/            路由
   ├─ utils/             工具方法
   ├─ components/        公共组件
   └─ views/             页面
```

## 目前不足

- 搜索目前使用 `like` 查询，数据量大时需要换成全文索引或搜索引擎
- 点赞标识保存在浏览器本地，清除缓存后可能重复点赞
- JWT 不能主动注销，需要时可以引入 Redis 黑名单
- 暂时没有编写单元测试和 CI 流水线

## License

MIT

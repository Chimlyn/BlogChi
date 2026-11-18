# BlogChi 后端

多博主博客平台的服务端，基于 Spring Boot 4 + MyBatis-Flex + MySQL + JWT。

前台是访客浏览文章的博客站，后台是博主写文章的控制台。多个博主共用一套表，
靠 `user_id` 做轻量数据隔离，不引入 RBAC。

## 技术栈

| 层次 | 选型 |
| --- | --- |
| 语言 / 运行时 | Java 25 |
| 框架 | Spring Boot 4.1.0、Spring MVC、Spring Security 7 |
| ORM | MyBatis-Flex 1.11.8（`mybatis-flex-spring-boot4-starter`） |
| 数据库 | MySQL 8（InnoDB、utf8mb4） |
| 鉴权 | JWT（`com.auth0:java-jwt`）+ 自定义 `JwtFilter` |
| 参数校验 | Jakarta Bean Validation |
| 其他 | Lombok、BCrypt 密码加密 |

## 功能

博主（注册用户）

- 注册、登录，JWT 鉴权，token 有效期 7 天
- 分类管理：新增、修改、删除；同一博主下分类名唯一
- 标签管理：新增、修改、删除；同一博主下标签名唯一
- 文章管理：Markdown 正文、绑定分类与多个标签、草稿 / 已发布、只能操作自己的文章
- 评论管理：查看自己文章下的评论、删除评论
- 数据概览：文章数、草稿数、评论数、总阅读量

访客（无需登录）

- 浏览所有博主已发布的文章，分页
- 关键词搜索、按分类筛选、按标签筛选
- 文章详情：阅读量自增、返回 Markdown 原文由前端渲染
- 发表评论（昵称 + 内容）
- 点赞 / 取消点赞（同一访客对同一文章只算一次）
- 热门文章、按年月归档

## 目录结构

```
src/main/java/com/blogchi
├── BlogChiApplication.java      启动类
├── common/                      Result、PageResult、Code、TimeUtil
├── config/                      SecurityConfig、WebConfig(CORS)
├── controller/                  公开接口 + 后台接口
├── dto/                         入参对象（带校验注解）
├── entity/                      实体，@Table 映射表名
├── exception/                   BizException、全局异常处理
├── mapper/                      BaseMapper 子接口
├── security/                    JwtUtil、JwtFilter、LoginUser、CurrentUser
├── service/                     业务逻辑（含数据归属校验）
└── vo/                          出参对象
```

`entity/table/*TableDef` 由 MyBatis-Flex 的注解处理器在编译期生成，用来构造类型安全的条件。

## 快速开始

### 1. 建库

```bash
mysql -uroot -p --default-character-set=utf8mb4 < sql/blogchi.sql
```

脚本会创建 `blogchi` 库、7 张表和一批演示数据（两个博主，密码都是 `123456`）。

### 2. 改配置

编辑 `src/main/resources/application.yaml`，把数据库账号密码改成你自己的：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blogchi?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码
```

### 3. 启动

```bash
mvn spring-boot:run
```

服务跑在 `http://localhost:8081`，前端工程默认把 `/api` 代理到这里。

### 4. 冒烟测试

```bash
curl "http://localhost:8081/api/public/articles?page=1&size=5"
curl -X POST http://localhost:8081/api/public/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"123456\"}"
```

## 接口约定

- 所有接口返回 `{ code, message, data }`，`code=200` 表示成功
- 需要登录的接口在请求头带 `Authorization: Bearer <token>`
- 公开接口前缀 `/api/public/**`，后台接口前缀 `/api/admin/**`

完整接口说明见配套的《BlogChi 接口文档》。

## 说明

- 文章内容存 Markdown 原文，渲染交给前端，后端不参与 HTML 拼接
- 阅读量用 `update article set view_count = view_count + 1` 原子自增，避免并发丢更新
- 删除文章会级联清理它的标签关联、评论和点赞，避免脏数据

-- ============================================================
-- BlogChi 数据库初始化脚本
-- 执行方式（Windows cmd / PowerShell）：
--   mysql -uroot -p --default-character-set=utf8mb4 < sql/blogchi.sql
-- 说明：脚本会先删除同名库，请确认本地没有同名数据再执行。
-- ============================================================

DROP DATABASE IF EXISTS blogchi;
CREATE DATABASE blogchi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE blogchi;

-- ------------------------------------------------------------
-- 博主账号
-- ------------------------------------------------------------
CREATE TABLE blog_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(20)  NOT NULL COMMENT '登录名，全局唯一',
    nickname    VARCHAR(20)  NOT NULL COMMENT '展示用昵称',
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
    bio         VARCHAR(200) DEFAULT NULL COMMENT '个人简介',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '博主';

-- ------------------------------------------------------------
-- 分类：归属于某个博主，同博主下名字唯一
-- ------------------------------------------------------------
CREATE TABLE category (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL COMMENT '所属博主',
    name        VARCHAR(20) NOT NULL COMMENT '分类名',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_name (user_id, name),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '分类';

-- ------------------------------------------------------------
-- 标签：同样归属于某个博主，同博主下名字唯一
-- ------------------------------------------------------------
CREATE TABLE tag (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL COMMENT '所属博主',
    name        VARCHAR(20) NOT NULL COMMENT '标签名',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_name (user_id, name),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '标签';

-- ------------------------------------------------------------
-- 文章：content 存 Markdown 原文
-- ------------------------------------------------------------
CREATE TABLE article (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL COMMENT '作者',
    category_id BIGINT       NOT NULL COMMENT '所属分类',
    title       VARCHAR(100) NOT NULL COMMENT '标题',
    summary     VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    content     LONGTEXT     NOT NULL COMMENT 'Markdown 正文',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0 草稿 1 已发布',
    view_count  INT          NOT NULL DEFAULT 0 COMMENT '阅读量',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_status (user_id, status),
    KEY idx_category (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '文章';

-- ------------------------------------------------------------
-- 文章-标签 多对多中间表
-- ------------------------------------------------------------
CREATE TABLE article_tag (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    article_id  BIGINT   NOT NULL,
    tag_id      BIGINT   NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    KEY idx_tag (tag_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '文章标签关联';

-- ------------------------------------------------------------
-- 评论：访客不登录，只留昵称
-- ------------------------------------------------------------
CREATE TABLE article_comment (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    article_id  BIGINT        NOT NULL,
    nickname    VARCHAR(20)   NOT NULL COMMENT '访客昵称',
    content     VARCHAR(1000) NOT NULL COMMENT '评论内容',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_article (article_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '文章评论';

-- ------------------------------------------------------------
-- 点赞：visitorKey 由前端生成，同一访客对同一文章只能点一次
-- ------------------------------------------------------------
CREATE TABLE article_like (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    article_id  BIGINT      NOT NULL,
    visitor_key VARCHAR(64) NOT NULL COMMENT '访客标识',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_visitor (article_id, visitor_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '文章点赞';

-- ============================================================
-- 演示数据
-- 两个博主，密码都是 123456（BCrypt 密文）
-- ============================================================
INSERT INTO blog_user (id, username, nickname, password, bio) VALUES
(1, 'admin', '小林同学', '$2a$10$grX1slc06z6O3hNGYqmPe.fJ2cA6/vBatFy4m6HFPOaIVv4Ug9bqC', '一个正在找实习的软件工程学生，喜欢把学过的东西写成笔记。'),
(2, 'demo', '小周同学', '$2a$10$WTv0Oi5Y9iRRXq4yKvh5MeLNyZh4FXRLNOQOcw0CAJ5K5LPF5PtGi', '后端开发方向，日常折腾 Java 和数据库。');

-- 小林同学的分类
INSERT INTO category (id, user_id, name) VALUES
(1, 1, '技术笔记'),
(2, 1, '项目复盘'),
(3, 1, '生活随笔');

-- 小周同学的分类（和技术笔记同名，验证不同博主可以重名）
INSERT INTO category (id, user_id, name) VALUES
(4, 2, '技术笔记'),
(5, 2, '数据库');

-- 标签
INSERT INTO tag (id, user_id, name) VALUES
(1, 1, 'Spring Boot'),
(2, 1, 'MyBatis-Flex'),
(3, 1, 'MySQL'),
(4, 1, 'Vue'),
(5, 1, '随笔'),
(6, 2, 'Java'),
(7, 2, 'Docker');

-- 文章
INSERT INTO article (id, user_id, category_id, title, summary, status, view_count, content) VALUES
(1, 1, 1, 'Spring Boot 4 项目从零搭建笔记',
 '记一次从空目录到能跑起来的完整过程，把踩过的坑都写下来了。',
 1, 128,
 '## 为什么重新搭一遍

新建项目这件事，看起来只是点几下 IDEA，其实每一步都在做选择：用哪个 JDK、加哪些 starter、包怎么分。把这些想清楚了，后面写业务才不会乱。

## 一、JDK 和 Spring Boot 版本

- JDK 25 是目前比较新的 LTS，编译参数直接用 `release 25`
- Spring Boot 4.x 要求 JDK 17 起步，JDK 25 完全兼容

## 二、依赖怎么选

只加真正用得上的：

1. `spring-boot-starter-web`：提供内嵌 Tomcat 和 Spring MVC
2. `spring-boot-starter-validation`：参数校验，少写很多 if
3. `spring-boot-starter-jdbc`：数据源和 JdbcTemplate
4. `mybatis-flex-spring-boot4-starter`：ORM
5. `mysql-connector-j`：MySQL 驱动

## 三、一个最小的启动类

```java
@SpringBootApplication
public class BlogChiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogChiApplication.class, args);
    }
}
```

## 四、踩坑记录

> 启动报 `Failed to configure a DataSource`，八成是没配数据库，或者驱动没加。

| 现象 | 原因 | 解决 |
| --- | --- | --- |
| 启动失败 | 缺少数据源配置 | 补 application.yaml |
| 接口 404 | 包没被扫描到 | 启动类放到最外层包 |

最后提醒一句：**依赖不是越多越好**，每加一个都要能说出它是干什么的。'),
(2, 1, 1, 'MyBatis-Flex 常用写法整理',
 '把增删改查、分页、条件构造这几件事整理成一份速查表。',
 1, 96,
 '## 为什么用它

MyBatis 用起来已经很熟，但每个 Mapper 都要手写 XML 或者注解 SQL，重复劳动太多。MyBatis-Flex 在 MyBatis 之上加了一层 `BaseMapper`，单表增删改查基本不用写 SQL。

## 继承 BaseMapper

```java
public interface ArticleMapper extends BaseMapper<Article> {
}
```

## 条件构造

```java
QueryWrapper qw = QueryWrapper.create()
        .where(ARTICLE.USER_ID.eq(uid))
        .orderBy(ARTICLE.ID.desc());
```

## 分页

```java
Page<Article> page = articleMapper.paginate(1, 10, qw);
long total = page.getTotalRow();
```

## 实体注解

- `@Table("article")`：指定表名
- `@Id(keyType = KeyType.Auto)`：主键自增
- `@Column(onInsertValue = "now()")`：插入时自动填时间

这套东西最大的好处是：SQL 是编译期由实体推导出来的，字段改名时 IDEA 会直接报错。'),
(3, 1, 2, 'BlogChi 项目复盘：数据隔离是怎么做的',
 '多博主共用一套表，怎么保证谁都不能动别人的数据。',
 1, 64,
 '## 问题

平台上有多个博主，分类、标签、文章都带 `user_id`。如果不做隔离，A 博主能删掉 B 博主的文章。

## 思路

没有引入 RBAC，只在 service 层做一次归属校验：

```java
private Article getOwnArticle(Long id) {
    Long uid = CurrentUser.id();
    Article article = articleMapper.selectOneById(id);
    if (article == null) {
        throw new BizException(Code.NOT_FOUND, "文章不存在");
    }
    if (!article.getUserId().equals(uid)) {
        throw new BizException(Code.FORBIDDEN);
    }
    return article;
}
```

## 为什么不在 SQL 里带条件

两个原因：

1. 查询和校验分开写，逻辑清楚，出错时错误码也能区分（404 还是 403）
2. 列表查询本来就带了 `user_id` 条件，详情类接口才需要额外校验

## 结论

轻量隔离的关键不是拦截器多强，而是**每个写操作都必须经过一次归属校验**。'),
(4, 1, 3, '关于写技术笔记这件小事',
 '写给自己的提醒：别等到全都懂了才动手记。',
 1, 41,
 '## 起因

以前总觉得，笔记要写得漂亮、要成体系，才配发出来。结果就是一直没写。

后来想通了：**笔记首先是给三个月后的自己看的**。

## 现在的做法

- 学完一个知识点，当天写三行也行
- 遇到报错，把报错原文和解决办法一起记下来
- 不追求排版，先追求能看懂

> 写下来的东西才是自己的，脑子里过的都只是感觉自己会了。

共勉。'),
(5, 2, 4, '索引为什么能加快查询',
 '从 B+ 树讲到最左前缀，尽量用大白话。',
 1, 33,
 '## 一句话版本

索引就是给数据排了一次序，把全表扫描换成了一次有序查找。

## B+ 树结构

- 非叶子节点只存键，能存更多，树更矮
- 叶子节点用链表连起来，范围查询很快

## 最左前缀原则

联合索引 `(a, b, c)` 相当于排序时的第一关键字是 a，第二是 b，第三是 c：

- `where a = ?` 能用
- `where a = ? and b = ?` 能用
- `where b = ?` 用不上

## 什么时候索引会失效

1. 在索引列上做运算，比如 `where id + 1 = 3`
2. `like` 以百分号开头
3. 类型不匹配的隐式转换

记住一条：**索引列上不要做计算**。'),
(6, 2, 5, 'MySQL 事务隔离级别实验记录',
 '未完待续：准备用两个会话验证 RR 下的不可重复读和幻读。',
 0, 0,
 '## 待补充

- [ ] RC 下的不可重复读复现
- [ ] RR 下的快照读与当前读
- [ ] 间隙锁的范围

（草稿，前台不可见）');

-- 文章-标签关联
INSERT INTO article_tag (article_id, tag_id) VALUES
(1, 1), (1, 3),
(2, 2), (2, 1),
(3, 2), (3, 3),
(4, 5),
(5, 3),
(6, 3);

-- 评论
INSERT INTO article_comment (article_id, nickname, content) VALUES
(1, '路人甲', '写得很细，正好跟着做了一遍。'),
(1, '小张', 'JDK 25 会不会太新了，公司里还在用 17。'),
(2, '阿May', 'BaseMapper 这块确实省事，收藏了。'),
(3, '路人乙', '数据隔离这段讲得很清楚。'),
(5, '数据库小白', '最左前缀终于看懂了。');

-- 点赞
INSERT INTO article_like (article_id, visitor_key) VALUES
(1, 'v-demo-0001'),
(1, 'v-demo-0002'),
(2, 'v-demo-0001'),
(3, 'v-demo-0003'),
(5, 'v-demo-0004');

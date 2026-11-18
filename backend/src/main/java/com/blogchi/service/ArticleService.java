package com.blogchi.service;

import com.blogchi.common.Code;
import com.blogchi.common.PageResult;
import com.blogchi.common.TimeUtil;
import com.blogchi.dto.ArticleDTO;
import com.blogchi.entity.Article;
import com.blogchi.entity.ArticleLike;
import com.blogchi.entity.ArticleTag;
import com.blogchi.entity.Category;
import com.blogchi.entity.Tag;
import com.blogchi.entity.User;
import com.blogchi.exception.BizException;
import com.blogchi.mapper.ArticleCommentMapper;
import com.blogchi.mapper.ArticleLikeMapper;
import com.blogchi.mapper.ArticleMapper;
import com.blogchi.mapper.ArticleTagMapper;
import com.blogchi.mapper.CategoryMapper;
import com.blogchi.mapper.TagMapper;
import com.blogchi.mapper.UserMapper;
import com.blogchi.security.CurrentUser;
import com.blogchi.vo.ArticleDetailVO;
import com.blogchi.vo.ArticleVO;
import com.blogchi.vo.ArchiveVO;
import com.blogchi.vo.LikeVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.blogchi.entity.table.ArticleCommentTableDef.ARTICLE_COMMENT;
import static com.blogchi.entity.table.ArticleLikeTableDef.ARTICLE_LIKE;
import static com.blogchi.entity.table.ArticleTableDef.ARTICLE;
import static com.blogchi.entity.table.ArticleTagTableDef.ARTICLE_TAG;
import static com.blogchi.entity.table.CategoryTableDef.CATEGORY;
import static com.blogchi.entity.table.TagTableDef.TAG;
import static com.blogchi.entity.table.UserTableDef.USER;

/**
 * 文章相关业务：后台增删改查、前台列表与详情、点赞。
 */
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleCommentMapper commentMapper;
    private final ArticleLikeMapper likeMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;

    // ==================== 博主后台 ====================

    public PageResult<ArticleVO> adminList(int page, int size, Integer status, String keyword) {
        Long uid = CurrentUser.id();
        QueryCondition condition = ARTICLE.USER_ID.eq(uid);
        if (status != null) {
            condition = condition.and(ARTICLE.STATUS.eq(status));
        }
        if (StringUtils.hasText(keyword)) {
            condition = condition.and(ARTICLE.TITLE.like(keyword));
        }
        Page<Article> result = articleMapper.paginate(page, size,
                QueryWrapper.create().where(condition).orderBy(ARTICLE.ID.desc()));
        return PageResult.of(toVOList(result.getRecords()), result.getTotalRow(), page, size);
    }

    public ArticleDetailVO adminDetail(Long id) {
        Article article = getOwnArticle(id);
        ArticleDetailVO vo = toDetailVO(article, null);
        vo.setContent(article.getContent());
        return vo;
    }

    @Transactional
    public void create(ArticleDTO dto) {
        Long uid = CurrentUser.id();
        checkCategory(dto.getCategoryId(), uid);
        checkStatus(dto.getStatus());

        Article article = new Article();
        article.setUserId(uid);
        article.setCategoryId(dto.getCategoryId());
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setStatus(dto.getStatus());
        article.setViewCount(0);
        articleMapper.insertSelective(article);

        saveTags(article.getId(), uid, dto.getTagIds());
    }

    @Transactional
    public void update(Long id, ArticleDTO dto) {
        Long uid = CurrentUser.id();
        Article article = getOwnArticle(id);
        checkCategory(dto.getCategoryId(), uid);
        checkStatus(dto.getStatus());

        article.setCategoryId(dto.getCategoryId());
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setStatus(dto.getStatus());
        articleMapper.update(article);

        // 标签是多对多关系，改动时先清空旧关联再重新写入
        articleTagMapper.deleteByQuery(
                QueryWrapper.create().where(ARTICLE_TAG.ARTICLE_ID.eq(article.getId())));
        saveTags(article.getId(), uid, dto.getTagIds());
    }

    @Transactional
    public void delete(Long id) {
        Article article = getOwnArticle(id);
        // 文章删掉后，它的标签关联、评论、点赞都没有存在意义，一起清理
        articleTagMapper.deleteByQuery(
                QueryWrapper.create().where(ARTICLE_TAG.ARTICLE_ID.eq(article.getId())));
        commentMapper.deleteByQuery(
                QueryWrapper.create().where(ARTICLE_COMMENT.ARTICLE_ID.eq(article.getId())));
        likeMapper.deleteByQuery(
                QueryWrapper.create().where(ARTICLE_LIKE.ARTICLE_ID.eq(article.getId())));
        articleMapper.deleteById(article.getId());
    }

    // ==================== 访客前台 ====================

    public PageResult<ArticleVO> publicList(int page, int size, String keyword,
                                            Long categoryId, Long tagId) {
        QueryCondition condition = ARTICLE.STATUS.eq(Article.STATUS_PUBLISHED);
        if (StringUtils.hasText(keyword)) {
            condition = condition.and(ARTICLE.TITLE.like(keyword).or(ARTICLE.CONTENT.like(keyword)));
        }
        if (categoryId != null) {
            condition = condition.and(ARTICLE.CATEGORY_ID.eq(categoryId));
        }
        if (tagId != null) {
            List<Long> articleIds = articleTagMapper.selectListByQuery(
                            QueryWrapper.create().where(ARTICLE_TAG.TAG_ID.eq(tagId)))
                    .stream().map(ArticleTag::getArticleId).toList();
            if (articleIds.isEmpty()) {
                return PageResult.of(List.of(), 0, page, size);
            }
            condition = condition.and(ARTICLE.ID.in(articleIds));
        }
        Page<Article> result = articleMapper.paginate(page, size,
                QueryWrapper.create().where(condition).orderBy(ARTICLE.ID.desc()));
        return PageResult.of(toVOList(result.getRecords()), result.getTotalRow(), page, size);
    }

    /**
     * 访客看详情：阅读量原子自增，同时返回点赞数与当前访客是否点过赞。
     */
    public ArticleDetailVO publicDetail(Long id, String visitorKey) {
        Article article = articleMapper.selectOneById(id);
        if (article == null || article.getStatus() != Article.STATUS_PUBLISHED) {
            throw new BizException(Code.NOT_FOUND, "文章不存在或已下架");
        }
        articleMapper.increaseViewCount(id);
        article.setViewCount(article.getViewCount() + 1);

        ArticleDetailVO vo = toDetailVO(article, visitorKey);
        vo.setContent(article.getContent());
        return vo;
    }

    public List<ArticleVO> hot(int limit) {
        Page<Article> result = articleMapper.paginate(1, limit,
                QueryWrapper.create()
                        .where(ARTICLE.STATUS.eq(Article.STATUS_PUBLISHED))
                        .orderBy(ARTICLE.VIEW_COUNT.desc()));
        return toVOList(result.getRecords());
    }

    public List<ArchiveVO> archives() {
        return articleMapper.selectArchives();
    }

    /**
     * 点赞/取消点赞二合一：同一个 visitorKey 再次调用就是取消。
     */
    @Transactional
    public LikeVO toggleLike(Long id, String visitorKey) {
        Article article = articleMapper.selectOneById(id);
        if (article == null || article.getStatus() != Article.STATUS_PUBLISHED) {
            throw new BizException(Code.NOT_FOUND, "文章不存在或已下架");
        }

        QueryWrapper qw = QueryWrapper.create()
                .where(ARTICLE_LIKE.ARTICLE_ID.eq(id).and(ARTICLE_LIKE.VISITOR_KEY.eq(visitorKey)));
        ArticleLike exists = likeMapper.selectOneByQuery(qw);

        boolean liked;
        if (exists == null) {
            ArticleLike like = new ArticleLike();
            like.setArticleId(id);
            like.setVisitorKey(visitorKey);
            likeMapper.insertSelective(like);
            liked = true;
        } else {
            likeMapper.deleteById(exists.getId());
            liked = false;
        }

        long count = likeMapper.selectCountByQuery(
                QueryWrapper.create().where(ARTICLE_LIKE.ARTICLE_ID.eq(id)));
        return new LikeVO(liked, count);
    }

    public long likeCount(Long id) {
        return likeMapper.selectCountByQuery(
                QueryWrapper.create().where(ARTICLE_LIKE.ARTICLE_ID.eq(id)));
    }

    // ==================== 私有工具方法 ====================

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

    private void checkCategory(Long categoryId, Long uid) {
        Category category = categoryMapper.selectOneById(categoryId);
        if (category == null || !category.getUserId().equals(uid)) {
            throw new BizException(Code.BAD_REQUEST, "分类不存在或不属于你");
        }
    }

    private void checkStatus(Integer status) {
        if (status == null
                || (status != Article.STATUS_DRAFT && status != Article.STATUS_PUBLISHED)) {
            throw new BizException(Code.BAD_REQUEST, "文章状态只能是 0（草稿）或 1（已发布）");
        }
    }

    /**
     * 写入文章-标签关联。标签必须属于当前博主，重复的 tagId 会被去重。
     */
    private void saveTags(Long articleId, Long uid, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        Set<Long> ids = new HashSet<>(tagIds);
        ids.remove(null);
        if (ids.isEmpty()) {
            return;
        }

        List<Tag> tags = tagMapper.selectListByQuery(
                QueryWrapper.create().where(TAG.ID.in(ids)));
        for (Tag tag : tags) {
            if (!tag.getUserId().equals(uid)) {
                throw new BizException(Code.BAD_REQUEST, "只能绑定自己的标签");
            }
        }

        for (Tag tag : tags) {
            ArticleTag relation = new ArticleTag();
            relation.setArticleId(articleId);
            relation.setTagId(tag.getId());
            articleTagMapper.insertSelective(relation);
        }
    }

    /**
     * 批量把实体转成列表 VO：先把作者、分类、标签一次性查出来，避免在循环里查库。
     */
    private List<ArticleVO> toVOList(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = articles.stream().map(Article::getUserId).distinct().toList();
        Map<Long, String> authorNames = new HashMap<>();
        userMapper.selectListByQuery(QueryWrapper.create().where(USER.ID.in(userIds)))
                .forEach(user -> authorNames.put(user.getId(), user.getNickname()));

        List<Long> categoryIds = articles.stream().map(Article::getCategoryId)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, String> categoryNames = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            categoryMapper.selectListByQuery(QueryWrapper.create().where(CATEGORY.ID.in(categoryIds)))
                    .forEach(category -> categoryNames.put(category.getId(), category.getName()));
        }

        List<Long> articleIds = articles.stream().map(Article::getId).toList();
        List<ArticleTag> relations = articleTagMapper.selectListByQuery(
                QueryWrapper.create().where(ARTICLE_TAG.ARTICLE_ID.in(articleIds)));
        Map<Long, List<Long>> tagIdsByArticle = new HashMap<>();
        for (ArticleTag relation : relations) {
            tagIdsByArticle.computeIfAbsent(relation.getArticleId(), k -> new ArrayList<>())
                    .add(relation.getTagId());
        }

        List<Long> tagIds = relations.stream().map(ArticleTag::getTagId).distinct().toList();
        Map<Long, String> tagNames = new HashMap<>();
        if (!tagIds.isEmpty()) {
            tagMapper.selectListByQuery(QueryWrapper.create().where(TAG.ID.in(tagIds)))
                    .forEach(tag -> tagNames.put(tag.getId(), tag.getName()));
        }

        List<ArticleVO> result = new ArrayList<>(articles.size());
        for (Article article : articles) {
            ArticleVO vo = new ArticleVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSummary(summaryOf(article));
            vo.setAuthorName(authorNames.get(article.getUserId()));
            vo.setCategoryId(article.getCategoryId());
            vo.setCategoryName(categoryNames.get(article.getCategoryId()));
            vo.setTags(tagIdsByArticle.getOrDefault(article.getId(), List.of()).stream()
                    .map(tagNames::get).filter(Objects::nonNull).toList());
            vo.setViewCount(article.getViewCount());
            vo.setStatus(article.getStatus());
            vo.setCreateTime(TimeUtil.format(article.getCreateTime()));
            result.add(vo);
        }
        return result;
    }

    private ArticleDetailVO toDetailVO(Article article, String visitorKey) {
        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setAuthorName(authorNameOf(article.getUserId()));
        vo.setCategoryId(article.getCategoryId());
        vo.setCategoryName(categoryNameOf(article.getCategoryId()));
        vo.setTagIds(tagIdsOf(article.getId()));
        vo.setTags(vo.getTagIds().stream().map(this::tagNameOf).filter(Objects::nonNull).toList());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(likeCount(article.getId()));
        vo.setLiked(isLiked(article.getId(), visitorKey));
        vo.setStatus(article.getStatus());
        vo.setCreateTime(TimeUtil.format(article.getCreateTime()));
        return vo;
    }

    /**
     * 摘要为空时，从 Markdown 正文里截一段纯文本兜底。
     */
    private String summaryOf(Article article) {
        if (StringUtils.hasText(article.getSummary())) {
            return article.getSummary();
        }
        String content = article.getContent() == null ? "" : article.getContent();
        String plain = content.replaceAll("#{1,6}\\s", "")
                .replaceAll("[*`>_\\[\\]()!-]", "")
                .replaceAll("\\s+", " ")
                .trim();
        return plain.length() > 120 ? plain.substring(0, 120) + "..." : plain;
    }

    private String authorNameOf(Long userId) {
        User user = userMapper.selectOneById(userId);
        return user == null ? null : user.getNickname();
    }

    private String categoryNameOf(Long categoryId) {
        Category category = categoryMapper.selectOneById(categoryId);
        return category == null ? null : category.getName();
    }

    private List<Long> tagIdsOf(Long articleId) {
        return articleTagMapper.selectListByQuery(
                        QueryWrapper.create().where(ARTICLE_TAG.ARTICLE_ID.eq(articleId)))
                .stream().map(ArticleTag::getTagId).toList();
    }

    private String tagNameOf(Long tagId) {
        Tag tag = tagMapper.selectOneById(tagId);
        return tag == null ? null : tag.getName();
    }

    private boolean isLiked(Long articleId, String visitorKey) {
        if (!StringUtils.hasText(visitorKey)) {
            return false;
        }
        return likeMapper.selectCountByQuery(QueryWrapper.create()
                .where(ARTICLE_LIKE.ARTICLE_ID.eq(articleId)
                        .and(ARTICLE_LIKE.VISITOR_KEY.eq(visitorKey)))) > 0;
    }
}

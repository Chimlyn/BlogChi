package com.blogchi.service;

import com.blogchi.entity.Article;
import com.blogchi.mapper.ArticleCommentMapper;
import com.blogchi.mapper.ArticleMapper;
import com.blogchi.mapper.CategoryMapper;
import com.blogchi.mapper.TagMapper;
import com.blogchi.security.CurrentUser;
import com.blogchi.vo.StatsVO;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.blogchi.entity.table.ArticleCommentTableDef.ARTICLE_COMMENT;
import static com.blogchi.entity.table.ArticleTableDef.ARTICLE;
import static com.blogchi.entity.table.CategoryTableDef.CATEGORY;
import static com.blogchi.entity.table.TagTableDef.TAG;

/**
 * 后台首页的数据概览。
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final ArticleMapper articleMapper;
    private final ArticleCommentMapper commentMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    public StatsVO stats() {
        Long uid = CurrentUser.id();
        StatsVO vo = new StatsVO();

        long articleCount = articleMapper.selectCountByQuery(
                QueryWrapper.create().where(ARTICLE.USER_ID.eq(uid)));
        long publishedCount = articleMapper.selectCountByQuery(
                QueryWrapper.create().where(ARTICLE.USER_ID.eq(uid)
                        .and(ARTICLE.STATUS.eq(Article.STATUS_PUBLISHED))));

        List<Article> articles = articleMapper.selectListByQuery(
                QueryWrapper.create().where(ARTICLE.USER_ID.eq(uid)));

        long commentCount = 0;
        if (!articles.isEmpty()) {
            commentCount = commentMapper.selectCountByQuery(
                    QueryWrapper.create().where(ARTICLE_COMMENT.ARTICLE_ID.in(
                            articles.stream().map(Article::getId).toList())));
        }

        vo.setArticleCount(articleCount);
        vo.setPublishedCount(publishedCount);
        vo.setDraftCount(articleCount - publishedCount);
        vo.setCommentCount(commentCount);
        vo.setViewCount(articles.stream()
                .mapToLong(article -> article.getViewCount() == null ? 0L : article.getViewCount())
                .sum());
        vo.setCategoryCount(categoryMapper.selectCountByQuery(
                QueryWrapper.create().where(CATEGORY.USER_ID.eq(uid))));
        vo.setTagCount(tagMapper.selectCountByQuery(
                QueryWrapper.create().where(TAG.USER_ID.eq(uid))));
        return vo;
    }
}

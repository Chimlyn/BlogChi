package com.blogchi.service;

import com.blogchi.common.Code;
import com.blogchi.common.PageResult;
import com.blogchi.common.TimeUtil;
import com.blogchi.dto.CommentDTO;
import com.blogchi.entity.Article;
import com.blogchi.entity.ArticleComment;
import com.blogchi.exception.BizException;
import com.blogchi.mapper.ArticleCommentMapper;
import com.blogchi.mapper.ArticleMapper;
import com.blogchi.security.CurrentUser;
import com.blogchi.vo.CommentVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.blogchi.entity.table.ArticleCommentTableDef.ARTICLE_COMMENT;
import static com.blogchi.entity.table.ArticleTableDef.ARTICLE;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final ArticleCommentMapper commentMapper;
    private final ArticleMapper articleMapper;

    /**
     * 访客查看某篇文章的评论，按时间倒序。
     */
    public PageResult<CommentVO> listByArticle(Long articleId, int page, int size) {
        Page<ArticleComment> result = commentMapper.paginate(page, size,
                QueryWrapper.create()
                        .where(ARTICLE_COMMENT.ARTICLE_ID.eq(articleId))
                        .orderBy(ARTICLE_COMMENT.ID.desc()));

        List<CommentVO> records = new ArrayList<>(result.getRecords().size());
        for (ArticleComment comment : result.getRecords()) {
            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setArticleId(comment.getArticleId());
            vo.setNickname(comment.getNickname());
            vo.setContent(comment.getContent());
            vo.setCreateTime(TimeUtil.format(comment.getCreateTime()));
            records.add(vo);
        }
        return PageResult.of(records, result.getTotalRow(), page, size);
    }

    @Transactional
    public void add(Long articleId, CommentDTO dto) {
        Article article = articleMapper.selectOneById(articleId);
        if (article == null || article.getStatus() != Article.STATUS_PUBLISHED) {
            throw new BizException(Code.NOT_FOUND, "文章不存在或已下架");
        }
        ArticleComment comment = new ArticleComment();
        comment.setArticleId(articleId);
        comment.setNickname(dto.getNickname());
        comment.setContent(dto.getContent());
        commentMapper.insertSelective(comment);
    }

    /**
     * 博主查看评论：为了做数据隔离，只看自己文章下的评论。
     */
    public PageResult<CommentVO> adminList(int page, int size) {
        Long uid = CurrentUser.id();
        List<Article> ownArticles = articleMapper.selectListByQuery(
                QueryWrapper.create().where(ARTICLE.USER_ID.eq(uid)));
        if (ownArticles.isEmpty()) {
            return PageResult.of(List.of(), 0, page, size);
        }

        List<Long> articleIds = ownArticles.stream().map(Article::getId).toList();
        Map<Long, String> titles = new HashMap<>();
        ownArticles.forEach(article -> titles.put(article.getId(), article.getTitle()));

        Page<ArticleComment> result = commentMapper.paginate(page, size,
                QueryWrapper.create()
                        .where(ARTICLE_COMMENT.ARTICLE_ID.in(articleIds))
                        .orderBy(ARTICLE_COMMENT.ID.desc()));

        List<CommentVO> records = new ArrayList<>(result.getRecords().size());
        for (ArticleComment comment : result.getRecords()) {
            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setArticleId(comment.getArticleId());
            vo.setArticleTitle(titles.get(comment.getArticleId()));
            vo.setNickname(comment.getNickname());
            vo.setContent(comment.getContent());
            vo.setCreateTime(TimeUtil.format(comment.getCreateTime()));
            records.add(vo);
        }
        return PageResult.of(records, result.getTotalRow(), page, size);
    }

    @Transactional
    public void delete(Long id) {
        Long uid = CurrentUser.id();
        ArticleComment comment = commentMapper.selectOneById(id);
        if (comment == null) {
            throw new BizException(Code.NOT_FOUND, "评论不存在");
        }
        Article article = articleMapper.selectOneById(comment.getArticleId());
        if (article == null || !article.getUserId().equals(uid)) {
            throw new BizException(Code.FORBIDDEN);
        }
        commentMapper.deleteById(id);
    }
}

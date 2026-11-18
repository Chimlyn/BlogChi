package com.blogchi.service;

import com.blogchi.common.Code;
import com.blogchi.dto.NameDTO;
import com.blogchi.entity.Tag;
import com.blogchi.exception.BizException;
import com.blogchi.mapper.ArticleTagMapper;
import com.blogchi.mapper.TagMapper;
import com.blogchi.security.CurrentUser;
import com.blogchi.vo.TagVO;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.blogchi.entity.table.ArticleTagTableDef.ARTICLE_TAG;
import static com.blogchi.entity.table.TagTableDef.TAG;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    public List<TagVO> publicList() {
        return tagMapper.selectPublicList();
    }

    public List<TagVO> list() {
        return tagMapper.selectListByUserId(CurrentUser.id());
    }

    @Transactional
    public TagVO create(NameDTO dto) {
        Long uid = CurrentUser.id();
        checkNameUnique(uid, dto.getName(), null);

        Tag tag = new Tag();
        tag.setUserId(uid);
        tag.setName(dto.getName());
        tagMapper.insertSelective(tag);

        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setArticleCount(0L);
        return vo;
    }

    @Transactional
    public void update(Long id, NameDTO dto) {
        Long uid = CurrentUser.id();
        Tag tag = getOwnTag(id, uid);
        checkNameUnique(uid, dto.getName(), tag.getId());
        tag.setName(dto.getName());
        tagMapper.update(tag);
    }

    /**
     * 标签可以直接删，但要顺手把中间表里的关联记录一起删掉，否则会留下脏数据。
     */
    @Transactional
    public void delete(Long id) {
        Long uid = CurrentUser.id();
        Tag tag = getOwnTag(id, uid);
        articleTagMapper.deleteByQuery(
                QueryWrapper.create().where(ARTICLE_TAG.TAG_ID.eq(tag.getId())));
        tagMapper.deleteById(tag.getId());
    }

    private Tag getOwnTag(Long id, Long uid) {
        Tag tag = tagMapper.selectOneById(id);
        if (tag == null) {
            throw new BizException(Code.NOT_FOUND, "标签不存在");
        }
        if (!tag.getUserId().equals(uid)) {
            throw new BizException(Code.FORBIDDEN);
        }
        return tag;
    }

    private void checkNameUnique(Long uid, String name, Long excludeId) {
        QueryCondition condition = TAG.USER_ID.eq(uid).and(TAG.NAME.eq(name));
        if (excludeId != null) {
            condition = condition.and(TAG.ID.ne(excludeId));
        }
        if (tagMapper.selectCountByQuery(QueryWrapper.create().where(condition)) > 0) {
            throw new BizException(Code.CONFLICT, "标签名已存在");
        }
    }
}

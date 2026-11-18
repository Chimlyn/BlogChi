package com.blogchi.service;

import com.blogchi.common.Code;
import com.blogchi.dto.NameDTO;
import com.blogchi.entity.Category;
import com.blogchi.exception.BizException;
import com.blogchi.mapper.ArticleMapper;
import com.blogchi.mapper.CategoryMapper;
import com.blogchi.security.CurrentUser;
import com.blogchi.vo.CategoryVO;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.blogchi.entity.table.ArticleTableDef.ARTICLE;
import static com.blogchi.entity.table.CategoryTableDef.CATEGORY;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    /**
     * 前台筛选用的分类（全站已发布文章维度）。
     */
    public List<CategoryVO> publicList() {
        return categoryMapper.selectPublicList();
    }

    /**
     * 后台列表：只返回当前博主自己的分类。
     */
    public List<CategoryVO> list() {
        return categoryMapper.selectListByUserId(CurrentUser.id());
    }

    @Transactional
    public CategoryVO create(NameDTO dto) {
        Long uid = CurrentUser.id();
        checkNameUnique(uid, dto.getName(), null);

        Category category = new Category();
        category.setUserId(uid);
        category.setName(dto.getName());
        categoryMapper.insertSelective(category);

        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setArticleCount(0L);
        return vo;
    }

    @Transactional
    public void update(Long id, NameDTO dto) {
        Long uid = CurrentUser.id();
        Category category = getOwnCategory(id, uid);
        checkNameUnique(uid, dto.getName(), category.getId());
        category.setName(dto.getName());
        categoryMapper.update(category);
    }

    @Transactional
    public void delete(Long id) {
        Long uid = CurrentUser.id();
        Category category = getOwnCategory(id, uid);

        // 业务规则：分类下还有文章就禁止删除
        long articleCount = articleMapper.selectCountByQuery(
                QueryWrapper.create().where(ARTICLE.CATEGORY_ID.eq(category.getId())));
        if (articleCount > 0) {
            throw new BizException(Code.CONFLICT, "该分类下还有文章，不能删除");
        }
        categoryMapper.deleteById(category.getId());
    }

    /**
     * 取出分类并校验归属，防止操作别人的数据。
     */
    private Category getOwnCategory(Long id, Long uid) {
        Category category = categoryMapper.selectOneById(id);
        if (category == null) {
            throw new BizException(Code.NOT_FOUND, "分类不存在");
        }
        if (!category.getUserId().equals(uid)) {
            throw new BizException(Code.FORBIDDEN);
        }
        return category;
    }

    /**
     * 同一个博主下分类名唯一；excludeId 用于修改时排除自己。
     */
    private void checkNameUnique(Long uid, String name, Long excludeId) {
        QueryCondition condition = CATEGORY.USER_ID.eq(uid).and(CATEGORY.NAME.eq(name));
        if (excludeId != null) {
            condition = condition.and(CATEGORY.ID.ne(excludeId));
        }
        if (categoryMapper.selectCountByQuery(QueryWrapper.create().where(condition)) > 0) {
            throw new BizException(Code.CONFLICT, "分类名已存在");
        }
    }
}

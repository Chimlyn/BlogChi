package com.blogchi.mapper;

import com.blogchi.entity.Category;
import com.blogchi.vo.CategoryVO;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 前台筛选用的分类列表：只统计已发布文章。
     */
    @Select("""
            select c.id, c.name, count(a.id) as articleCount
            from category c
            left join article a on a.category_id = c.id and a.status = 1
            group by c.id, c.name
            having articleCount > 0
            order by articleCount desc
            """)
    List<CategoryVO> selectPublicList();

    /**
     * 博主后台的分类列表：统计自己的全部文章（含草稿）。
     */
    @Select("""
            select c.id, c.name, count(a.id) as articleCount
            from category c
            left join article a on a.category_id = c.id
            where c.user_id = #{uid}
            group by c.id, c.name
            order by c.id desc
            """)
    List<CategoryVO> selectListByUserId(@Param("uid") Long uid);
}

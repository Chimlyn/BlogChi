package com.blogchi.mapper;

import com.blogchi.entity.Tag;
import com.blogchi.vo.TagVO;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 前台标签列表：只统计已发布文章，按热度倒序。
     */
    @Select("""
            select t.id, t.name, count(a.id) as articleCount
            from tag t
            join article_tag at on at.tag_id = t.id
            join article a on a.id = at.article_id and a.status = 1
            group by t.id, t.name
            order by articleCount desc
            """)
    List<TagVO> selectPublicList();

    /**
     * 博主后台的标签列表。
     */
    @Select("""
            select t.id, t.name, count(at.article_id) as articleCount
            from tag t
            left join article_tag at on at.tag_id = t.id
            where t.user_id = #{uid}
            group by t.id, t.name
            order by t.id desc
            """)
    List<TagVO> selectListByUserId(@Param("uid") Long uid);
}

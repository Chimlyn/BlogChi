package com.blogchi.mapper;

import com.blogchi.entity.Article;
import com.blogchi.vo.ArchiveVO;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 阅读量自增。用 SQL 原子自增，避免并发下先查后改丢失更新。
     */
    @Update("update article set view_count = view_count + 1 where id = #{id}")
    int increaseViewCount(@Param("id") Long id);

    /**
     * 按年月归档已发布文章，一次查完免去在 Java 里分组。
     */
    @Select("""
            select date_format(create_time, '%Y-%m') as month, count(*) as total
            from article
            where status = 1
            group by month
            order by month desc
            """)
    List<ArchiveVO> selectArchives();
}

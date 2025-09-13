package com.xingmiao.blog.app.repository;

import com.xingmiao.blog.common.domain.entity.Post;
import com.xingmiao.blog.common.domain.enums.PostStatus;
import com.xingmiao.blog.common.domain.enums.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findBySlug(String slug);
    Page<Post> findByCategory_Id(Long categoryId, Pageable pageable);
    Page<Post> findByStatusAndVisibility(PostStatus status, Visibility visibility, Pageable pageable);
    
    /**
     * 根据ID查询Post并预加载Category，避免懒加载问题
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Post> findByIdWithCategory(@Param("id") Long id);
}



package com.xingmiao.blog.app.repository;

import com.xingmiao.blog.common.domain.entity.Post;
import com.xingmiao.blog.common.domain.enums.PostStatus;
import com.xingmiao.blog.common.domain.enums.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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
    
    // ========== 软删除相关查询方法 ==========
    
    /**
     * 查询未删除的文章（正常文章）
     */
    Page<Post> findByDeletedAtIsNull(Pageable pageable);
    
    /**
     * 查询回收站中的文章（已软删除）
     */
    Page<Post> findByDeletedAtIsNotNull(Pageable pageable);
    
    /**
     * 根据ID查询未删除的文章
     */
    Optional<Post> findByIdAndDeletedAtIsNull(Long id);
    
    /**
     * 根据ID查询回收站中的文章
     */
    Optional<Post> findByIdAndDeletedAtIsNotNull(Long id);
    
    /**
     * 根据slug查询未删除的文章
     */
    Optional<Post> findBySlugAndDeletedAtIsNull(String slug);
    
    /**
     * 按分类查询未删除的文章
     */
    Page<Post> findByCategory_IdAndDeletedAtIsNull(Long categoryId, Pageable pageable);
    
    /**
     * 按状态和可见性查询未删除的文章
     */
    Page<Post> findByStatusAndVisibilityAndDeletedAtIsNull(PostStatus status, Visibility visibility, Pageable pageable);
    
    /**
     * 统计指定分类下未删除的文章数量
     */
    long countByCategory_IdAndDeletedAtIsNull(Long categoryId);
    
    /**
     * 查找回收站中指定分类下的所有文章
     */
    List<Post> findByCategory_IdAndDeletedAtIsNotNull(Long categoryId);
}



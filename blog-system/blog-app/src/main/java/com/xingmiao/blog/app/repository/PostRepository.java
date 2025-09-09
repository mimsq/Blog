package com.xingmiao.blog.app.repository;

import com.xingmiao.blog.common.domain.entity.Post;
import com.xingmiao.blog.common.domain.enums.PostStatus;
import com.xingmiao.blog.common.domain.enums.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findBySlug(String slug);
    Page<Post> findByCategory_Id(Long categoryId, Pageable pageable);
    Page<Post> findByStatusAndVisibility(PostStatus status, Visibility visibility, Pageable pageable);
}



package com.xingmiao.blog.app.service;

import com.xingmiao.blog.common.domain.entity.Post;
import com.xingmiao.blog.common.dto.PostCreateRequest;
import com.xingmiao.blog.common.dto.PostDto;
import com.xingmiao.blog.common.dto.PostUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostService {
    PostDto create(PostCreateRequest request);
    PostDto update(Long id, PostUpdateRequest request);
    void delete(Long id);
    Optional<PostDto> getById(Long id);
    Optional<PostDto> getBySlug(String slug);
    Page<PostDto> list(Pageable pageable);
    Page<PostDto> listByCategory(Long categoryId, Pageable pageable);
    Boolean SyncToDify(Post post);
    boolean existsById(Long id);
}



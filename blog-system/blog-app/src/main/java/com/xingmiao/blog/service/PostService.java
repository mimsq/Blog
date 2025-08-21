package com.xingmiao.blog.service;

import com.xingmiao.blog.dto.PostCreateRequest;
import com.xingmiao.blog.dto.PostDto;
import com.xingmiao.blog.dto.PostUpdateRequest;
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
}



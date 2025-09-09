package com.xingmiao.blog.app.service.impl;

import com.xingmiao.blog.common.domain.entity.Category;
import com.xingmiao.blog.common.domain.entity.Post;
import com.xingmiao.blog.common.dto.PostCreateRequest;
import com.xingmiao.blog.common.dto.PostDto;
import com.xingmiao.blog.common.dto.PostUpdateRequest;
import com.xingmiao.blog.app.repository.CategoryRepository;
import com.xingmiao.blog.app.repository.PostRepository;
import com.xingmiao.blog.app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public PostDto create(PostCreateRequest request) {
        // 检查slug是否已存在
        if (postRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new RuntimeException("当前Slug已经存在，Slug不能重复:" + request.getSlug());
        }
        
        // 处理分类
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("当前分类不存在,CategoryId:" + request.getCategoryId()));
        }
        
        Post post = Post.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .excerpt(request.getExcerpt())
                .content(request.getContent())
                .contentType(request.getContentType())
                .status(request.getStatus())
                .visibility(request.getVisibility())
                .password(request.getPassword())
                .category(category)
                .coverImageUrl(request.getCoverImageUrl())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .build();

        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost);
    }

    @Override
    public PostDto update(Long id, PostUpdateRequest request) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在，ID:" + id));
        
        // 检查slug是否重复（排除当前文章）
        if (request.getSlug() != null && !request.getSlug().equals(existingPost.getSlug())) {
            if (postRepository.findBySlug(request.getSlug()).isPresent()) {
                throw new RuntimeException("当前Slug已经存在，Slug不能重复:" + request.getSlug());
            }
        }
        
        // 处理分类更新
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("当前分类不存在,CategoryId:" + request.getCategoryId()));
        }
        
        // 更新字段
        if (request.getTitle() != null) {
            existingPost.setTitle(request.getTitle());
        }
        if (request.getSlug() != null) {
            existingPost.setSlug(request.getSlug());
        }
        if (request.getExcerpt() != null) {
            existingPost.setExcerpt(request.getExcerpt());
        }
        if (request.getContent() != null) {
            existingPost.setContent(request.getContent());
        }
        if (request.getContentType() != null) {
            existingPost.setContentType(request.getContentType());
        }
        if (request.getStatus() != null) {
            existingPost.setStatus(request.getStatus());
        }
        if (request.getVisibility() != null) {
            existingPost.setVisibility(request.getVisibility());
        }
        if (request.getPassword() != null) {
            existingPost.setPassword(request.getPassword());
        }
        if (category != null) {
            existingPost.setCategory(category);
        }
        if (request.getCoverImageUrl() != null) {
            existingPost.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getMetaTitle() != null) {
            existingPost.setMetaTitle(request.getMetaTitle());
        }
        if (request.getMetaDescription() != null) {
            existingPost.setMetaDescription(request.getMetaDescription());
        }
        if (request.getMetaKeywords() != null) {
            existingPost.setMetaKeywords(request.getMetaKeywords());
        }
        
        Post updatedPost = postRepository.save(existingPost);
        return convertToDto(updatedPost);
    }

    @Override
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在，ID:" + id));
        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostDto> getById(Long id) {
        return postRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostDto> getBySlug(String slug) {
        return postRepository.findBySlug(slug)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> list(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> listByCategory(Long categoryId, Pageable pageable) {
        return postRepository.findByCategory_Id(categoryId, pageable)
                .map(this::convertToDto);
    }

    private PostDto convertToDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .content(post.getContent())
                .contentType(post.getContentType())
                .status(post.getStatus())
                .visibility(post.getVisibility())
                .password(post.getPassword())
                .categoryId(post.getCategory() != null ? post.getCategory().getId() : null)
                .coverImageUrl(post.getCoverImageUrl())
                .metaTitle(post.getMetaTitle())
                .metaDescription(post.getMetaDescription())
                .metaKeywords(post.getMetaKeywords())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    @Override
    public Boolean SyncToDify(Post post) {

        return null;
    }
}

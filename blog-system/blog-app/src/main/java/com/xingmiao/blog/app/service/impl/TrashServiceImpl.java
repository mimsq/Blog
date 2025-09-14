package com.xingmiao.blog.app.service.impl;

import com.xingmiao.blog.app.repository.PostRepository;
import com.xingmiao.blog.app.service.DifySyncService;
import com.xingmiao.blog.app.service.TrashService;
import com.xingmiao.blog.common.domain.entity.Post;
import com.xingmiao.blog.common.dto.PostDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

/**
 * 回收站服务实现类
 * 
 * @author 星喵博客系统
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrashServiceImpl implements TrashService {

    private final PostRepository postRepository;
    private final DifySyncService difySyncService;

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> listPosts(Pageable pageable) {
        return postRepository.findByDeletedAtIsNotNull(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostDto> getPost(Long id) {
        return postRepository.findByIdAndDeletedAtIsNotNull(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public void restorePost(Long id) {
        Post post = postRepository.findByIdAndDeletedAtIsNotNull(id)
                .orElseThrow(() -> new RuntimeException("回收站中文章不存在，ID:" + id));
        
        post.setDeletedAt(null);
        postRepository.save(post);
        
        // 异步重新同步到Dify
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                difySyncService.syncPost(id);
            }
        });
        
        log.info("文章已从回收站恢复，ID:{} 标题:{}", id, post.getTitle());
    }

    @Override
    @Transactional
    public void hardDeletePost(Long id) {
        Post post = postRepository.findByIdAndDeletedAtIsNotNull(id)
                .orElseThrow(() -> new RuntimeException("回收站中文章不存在，ID:" + id));
        
        String title = post.getTitle();
        postRepository.delete(post);
        
        log.info("文章已从回收站硬删除，ID:{} 标题:{}", id, title);
    }

    @Override
    @Transactional
    public void batchRestore(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("文章ID列表不能为空");
        }
        
        List<Post> posts = postRepository.findAllById(ids);
        List<Post> trashPosts = posts.stream()
                .filter(post -> post.getDeletedAt() != null)
                .toList();
        
        if (trashPosts.isEmpty()) {
            throw new RuntimeException("没有找到可恢复的文章");
        }
        
        // 批量恢复
        trashPosts.forEach(post -> post.setDeletedAt(null));
        postRepository.saveAll(trashPosts);
        
        // 异步重新同步到Dify
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                trashPosts.forEach(post -> difySyncService.syncPost(post.getId()));
            }
        });
        
        log.info("批量恢复文章完成，恢复数量:{} 文章ID:{}", trashPosts.size(), 
                trashPosts.stream().map(Post::getId).toList());
    }

    @Override
    @Transactional
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("文章ID列表不能为空");
        }
        
        List<Post> posts = postRepository.findAllById(ids);
        List<Post> trashPosts = posts.stream()
                .filter(post -> post.getDeletedAt() != null)
                .toList();
        
        if (trashPosts.isEmpty()) {
            throw new RuntimeException("没有找到可删除的文章");
        }
        
        // 批量硬删除
        postRepository.deleteAll(trashPosts);
        
        log.info("批量硬删除文章完成，删除数量:{} 文章ID:{}", trashPosts.size(),
                trashPosts.stream().map(Post::getId).toList());
    }

    /**
     * 转换Post实体为PostDto
     */
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
                .difyDocumentId(post.getDifyDocumentId())
                .syncStatus(post.getSyncStatus())
                .syncError(post.getSyncError())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}

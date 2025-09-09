package com.xingmiao.blog.app.repository;

import com.xingmiao.blog.common.domain.entity.PostAccessKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostAccessKeyRepository extends JpaRepository<PostAccessKey, Long> {
    List<PostAccessKey> findByPost_IdAndIsActiveTrue(Long postId);
    List<PostAccessKey> findByPost_IdAndIsActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanEqual(Long postId, LocalDateTime start, LocalDateTime end);
}




package com.xingmiao.blog.app.service.impl;

import com.xingmiao.blog.common.domain.entity.Post;
import com.xingmiao.blog.common.domain.entity.PostAccessKey;
import com.xingmiao.blog.common.dto.PostAccessKeyItem;
import com.xingmiao.blog.common.dto.PostPasswordsUpdateRequest;
import com.xingmiao.blog.app.repository.PostAccessKeyRepository;
import com.xingmiao.blog.app.repository.PostRepository;
import com.xingmiao.blog.app.service.PostAccessKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostAccessKeyServiceImpl implements PostAccessKeyService {

    private final PostAccessKeyRepository accessKeyRepository;
    private final PostRepository postRepository;

    @Override
    public void updatePostPasswords(Long postId, PostPasswordsUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章不存在: " + postId));

        if ("replace".equalsIgnoreCase(request.getMode())) {
            // 软删除旧口令：标记失效
            accessKeyRepository.findByPost_IdAndIsActiveTrue(postId).forEach(k -> {
                k.setIsActive(false);
            });
        }

        for (PostAccessKeyItem item : request.getItems()) {
            LocalDateTime startsAt = item.getStartsAt() != null ? item.getStartsAt() : LocalDateTime.now();
            LocalDateTime endsAt = item.getEndsAt();
            if (endsAt == null && item.getDurationHours() != null) {
                endsAt = startsAt.plusHours(item.getDurationHours());
            }
            if (endsAt == null) {
                throw new RuntimeException("必须提供截止时间或持续时长");
            }

            String salt = generateSalt();
            String hash = sha256WithSalt(item.getPassword(), salt);

            PostAccessKey key = PostAccessKey.builder()
                    .post(post)
                    .passcodeSalt(salt)
                    .passcodeHash(hash)
                    .label(item.getLabel())
                    .startsAt(startsAt)
                    .endsAt(endsAt)
                    .isActive(true)
                    .build();
            accessKeyRepository.save(key);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostAccessKeyItem> listPostPasswords(Long postId) {
        LocalDateTime now = LocalDateTime.now();
        return accessKeyRepository.findByPost_IdAndIsActiveTrue(postId).stream()
                .map(k -> PostAccessKeyItem.builder()
                        .label(k.getLabel())
                        .startsAt(k.getStartsAt())
                        .endsAt(k.getEndsAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePassword(Long postId, String rawPassword) {
        LocalDateTime now = LocalDateTime.now();
        List<PostAccessKey> keys = accessKeyRepository
                .findByPost_IdAndIsActiveTrue(postId);
        for (PostAccessKey key : keys) {
            if (key.getStartsAt() != null && now.isBefore(key.getStartsAt())) {
                continue;
            }
            if (key.getEndsAt() != null && now.isAfter(key.getEndsAt())) {
                continue;
            }
            String calc = sha256WithSalt(rawPassword, key.getPasscodeSalt());
            if (calc.equals(key.getPasscodeHash())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Long matchKeyId(Long postId, String rawPassword) {
        LocalDateTime now = LocalDateTime.now();
        List<PostAccessKey> keys = accessKeyRepository
                .findByPost_IdAndIsActiveTrue(postId);
        for (PostAccessKey key : keys) {
            if (key.getStartsAt() != null && now.isBefore(key.getStartsAt())) {
                continue;
            }
            if (key.getEndsAt() != null && now.isAfter(key.getEndsAt())) {
                continue;
            }
            String calc = sha256WithSalt(rawPassword, key.getPasscodeSalt());
            if (calc.equals(key.getPasscodeHash())) {
                return key.getId();
            }
        }
        return null;
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(salt);
    }

    private String sha256WithSalt(String raw, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashed = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception e) {
            throw new RuntimeException("口令哈希失败", e);
        }
    }
}



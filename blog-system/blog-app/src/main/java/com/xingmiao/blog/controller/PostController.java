package com.xingmiao.blog.controller;

import com.xingmiao.blog.domain.enums.Visibility;
import com.xingmiao.blog.dto.PostCreateRequest;
import com.xingmiao.blog.dto.PostDto;
import com.xingmiao.blog.dto.PostUpdateRequest;
import com.xingmiao.blog.repository.PostAccessKeyRepository;
import com.xingmiao.blog.service.AccessTokenService;
import com.xingmiao.blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AccessTokenService accessTokenService;
    private final PostAccessKeyRepository postAccessKeyRepository;

    @PostMapping
    public ResponseEntity<PostDto> create(@Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(postService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getById(@PathVariable Long id, HttpServletRequest request) {
        String token = getCookieValue(request, "pa_" + id);
        return postService.getById(id)
                .map(post -> handleProtectedPost(id, post, token))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostDto> getBySlug(@PathVariable String slug, HttpServletRequest request) {
        return postService.getBySlug(slug)
                .map(post -> {
                    String token = getCookieValue(request, "pa_" + post.getId());
                    return handleProtectedPost(post.getId(), post, token);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<PostDto>> list(Pageable pageable) {
        return ResponseEntity.ok(postService.list(pageable));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<PostDto>> listByCategory(@PathVariable Long categoryId, Pageable pageable) {
        return ResponseEntity.ok(postService.listByCategory(categoryId, pageable));
    }

    private ResponseEntity<PostDto> handleProtectedPost(Long postId, PostDto post, String token) {
        if (post.getVisibility() != Visibility.PASSWORD) {
            return ResponseEntity.ok(post);
        }
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long keyId = accessTokenService.validateAndGetKeyId(token, postId);
        if (keyId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 校验 key 是否仍有效
        var keyOpt = postAccessKeyRepository.findById(keyId);
        if (keyOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        var key = keyOpt.get();
        var now = java.time.LocalDateTime.now();
        if (!Boolean.TRUE.equals(key.getIsActive())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (key.getStartsAt() != null && now.isBefore(key.getStartsAt())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (key.getEndsAt() != null && now.isAfter(key.getEndsAt())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(post);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}



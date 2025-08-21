package com.xingmiao.blog.controller;

import com.xingmiao.blog.dto.PostPasswordsUpdateRequest;
import com.xingmiao.blog.service.AccessTokenService;
import com.xingmiao.blog.service.PostAccessKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostPasswordController {

    private final PostAccessKeyService postAccessKeyService;
    private final AccessTokenService accessTokenService;

    @PutMapping("/{postId}/passwords")
    public ResponseEntity<Void> updatePasswords(@PathVariable Long postId,
                                                @Valid @RequestBody PostPasswordsUpdateRequest request) {
        postAccessKeyService.updatePostPasswords(postId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/access")
    public ResponseEntity<Void> verify(@PathVariable Long postId,
                                       @RequestBody String password) {
        Long keyId = postAccessKeyService.matchKeyId(postId, password);
        if (keyId == null) {
            return ResponseEntity.status(401).build();
        }
        // 令牌有效期：可取所选口令的 endsAt 与 24h 的较小值。为简单，先给 24h。
        String token = accessTokenService.generatePostAccessToken(postId, keyId, 24 * 3600);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "pa_" + postId + "=" + token + "; Path=/; HttpOnly; Max-Age=" + (24 * 3600));
        return ResponseEntity.ok().headers(headers).build();
    }
}



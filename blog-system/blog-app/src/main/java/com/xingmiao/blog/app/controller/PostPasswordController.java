package com.xingmiao.blog.app.controller;

import com.xingmiao.blog.common.dto.PostPasswordsUpdateRequest;
import com.xingmiao.blog.app.service.AccessTokenService;
import com.xingmiao.blog.app.service.PostAccessKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 文章密码管理控制器
 * 
 * <p>提供博客文章密码保护相关的功能，包括密码设置和访问验证。</p>
 * <p>主要功能包括：</p>
 * <ul>
 *   <li>更新文章的访问密码</li>
 *   <li>验证文章访问密码并生成访问令牌</li>
 * </ul>
 * 
 * @author 星喵博客系统
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "文章密码管理", description = "文章密码保护和访问控制接口")
public class PostPasswordController {

    private final PostAccessKeyService postAccessKeyService;
    private final AccessTokenService accessTokenService;

    /**
     * 更新文章的访问密码
     * 
     * @param postId 文章ID
     * @param request 密码更新请求对象，包含新的密码列表
     * @return 更新成功返回204状态码
     */
    @PutMapping("/{postId}/passwords")
    @Operation(summary = "更新文章密码", description = "更新指定文章的访问密码列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "密码更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数无效"),
        @ApiResponse(responseCode = "404", description = "文章不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Void> updatePasswords(
            @Parameter(description = "文章ID", required = true) @PathVariable Long postId,
            @Valid @RequestBody PostPasswordsUpdateRequest request) {
        postAccessKeyService.updatePostPasswords(postId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 验证文章访问密码
     * 
     * @param postId 文章ID
     * @param password 访问密码
     * @return 验证成功返回200状态码，并设置访问令牌Cookie
     */
    @PostMapping("/{postId}/access")
    @Operation(summary = "验证文章密码", description = "验证文章访问密码，验证成功后生成访问令牌并设置Cookie")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "密码验证成功，已设置访问令牌"),
        @ApiResponse(responseCode = "401", description = "密码验证失败"),
        @ApiResponse(responseCode = "404", description = "文章不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Void> verify(
            @Parameter(description = "文章ID", required = true) @PathVariable Long postId,
            @Parameter(description = "访问密码", required = true) @RequestBody String password) {
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



package com.xingmiao.blog.app.controller;

import com.xingmiao.blog.app.repository.CategoryRepository;
import com.xingmiao.blog.app.service.DifySyncService;
import com.xingmiao.blog.app.service.PostService;
import com.xingmiao.blog.common.domain.entity.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Dify同步管理", description = "Dify知识库同步相关接口")
public class DifySyncController {

    private final DifySyncService difySyncService;
    private final CategoryRepository categoryRepository;
    private final PostService postService;

    // ========== Category同步接口 ==========

    @PostMapping("/categories/{id}/sync")
    @Operation(summary = "同步分类到Dify", description = "手动触发分类同步到Dify知识库")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "同步操作已启动"),
        @ApiResponse(responseCode = "404", description = "分类不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Void> syncCategoryToDify(
            @Parameter(description = "分类ID", required = true) @PathVariable("id") Long id) {
        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        difySyncService.syncCategory(id);
        return ResponseEntity.accepted().build();
    }

    // ========== Post同步接口 ==========

    @PostMapping("/posts/{id}/sync")
    @Operation(summary = "同步文章到Dify", description = "手动触发文章同步到Dify知识库")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "同步操作已启动"),
        @ApiResponse(responseCode = "404", description = "文章不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Void> syncPostToDify(
            @Parameter(description = "文章ID", required = true) @PathVariable("id") Long id) {
        if (!postService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        difySyncService.syncPost(id);
        return ResponseEntity.accepted().build();
    }
}



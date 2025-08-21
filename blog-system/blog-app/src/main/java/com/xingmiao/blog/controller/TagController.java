package com.xingmiao.blog.controller;

import com.xingmiao.blog.dto.TagCreateRequest;
import com.xingmiao.blog.dto.TagDto;
import com.xingmiao.blog.dto.TagUpdateRequest;
import com.xingmiao.blog.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    /**
     * 创建标签
     */
    @PostMapping
    public ResponseEntity<TagDto> createTag(@Valid @RequestBody TagCreateRequest request) {
        TagDto createdTag = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }
    
    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagDto> updateTag(@PathVariable Long id, 
                                          @Valid @RequestBody TagUpdateRequest request) {
        TagDto updatedTag = tagService.updateTag(id, request);
        return ResponseEntity.ok(updatedTag);
    }
    
    /**
     * 根据ID获取标签
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagDto> getTagById(@PathVariable Long id) {
        return tagService.getTagById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根据名称获取标签
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<TagDto> getTagByName(@PathVariable String name) {
        return tagService.getTagByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根据别名获取标签
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<TagDto> getTagBySlug(@PathVariable String slug) {
        return tagService.getTagBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 分页获取所有标签
     */
    @GetMapping
    public ResponseEntity<Page<TagDto>> getAllTags(Pageable pageable) {
        Page<TagDto> tags = tagService.getAllTags(pageable);
        return ResponseEntity.ok(tags);
    }
    
    /**
     * 获取所有标签（不分页）
     */
    @GetMapping("/all")
    public ResponseEntity<List<TagDto>> getAllTags() {
        List<TagDto> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }
    
    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 检查标签名称是否存在
     */
    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> existsByName(@PathVariable String name) {
        boolean exists = tagService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 检查标签别名是否存在
     */
    @GetMapping("/exists/slug/{slug}")
    public ResponseEntity<Boolean> existsBySlug(@PathVariable String slug) {
        boolean exists = tagService.existsBySlug(slug);
        return ResponseEntity.ok(exists);
    }
}

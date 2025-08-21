package com.xingmiao.blog.service.impl;

import com.xingmiao.blog.domain.entity.Tag;
import com.xingmiao.blog.dto.TagCreateRequest;
import com.xingmiao.blog.dto.TagDto;
import com.xingmiao.blog.dto.TagUpdateRequest;
import com.xingmiao.blog.repository.TagRepository;
import com.xingmiao.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TagServiceImpl implements TagService {
    
    private final TagRepository tagRepository;
    
    @Override
    public TagDto createTag(TagCreateRequest request) {
        // 检查名称和别名是否已存在
        if (tagRepository.existsByName(request.getName())) {
            throw new RuntimeException("标签名称已存在: " + request.getName());
        }
        if (tagRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("标签别名已存在: " + request.getSlug());
        }
        
        Tag tag = Tag.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .color(request.getColor())
                .build();
        
        Tag savedTag = tagRepository.save(tag);
        return convertToDto(savedTag);
    }
    
    @Override
    public TagDto updateTag(Long id, TagUpdateRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签不存在: " + id));
        
        // 检查名称和别名是否与其他标签冲突
        if (request.getName() != null && !request.getName().equals(tag.getName())) {
            if (tagRepository.existsByName(request.getName())) {
                throw new RuntimeException("标签名称已存在: " + request.getName());
            }
            tag.setName(request.getName());
        }
        
        if (request.getSlug() != null && !request.getSlug().equals(tag.getSlug())) {
            if (tagRepository.existsBySlug(request.getSlug())) {
                throw new RuntimeException("标签别名已存在: " + request.getSlug());
            }
            tag.setSlug(request.getSlug());
        }
        
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
        
        Tag updatedTag = tagRepository.save(tag);
        return convertToDto(updatedTag);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TagDto> getTagById(Long id) {
        return tagRepository.findById(id).map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TagDto> getTagByName(String name) {
        return tagRepository.findByName(name).map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TagDto> getTagBySlug(String slug) {
        return tagRepository.findBySlug(slug).map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TagDto> getAllTags(Pageable pageable) {
        return tagRepository.findAll(pageable).map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TagDto> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new RuntimeException("标签不存在: " + id);
        }
        tagRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return tagRepository.existsBySlug(slug);
    }
    
    private TagDto convertToDto(Tag tag) {
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .color(tag.getColor())
                .postCount(tag.getPostCount())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}

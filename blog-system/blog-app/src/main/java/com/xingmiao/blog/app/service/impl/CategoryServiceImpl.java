package com.xingmiao.blog.app.service.impl;

import com.xingmiao.blog.common.domain.entity.Category;
import com.xingmiao.blog.common.dto.CategoryCreateRequest;
import com.xingmiao.blog.common.dto.CategoryDto;
import com.xingmiao.blog.common.dto.CategoryUpdateRequest;
import com.xingmiao.blog.app.repository.CategoryRepository;
import com.xingmiao.blog.app.service.DifySyncService;
import com.xingmiao.blog.common.domain.enums.SyncStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.xingmiao.blog.app.service.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final DifySyncService difySyncService;

    @Override
    public CategoryDto createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("分类别名已存在: " + request.getSlug());
        }
        if (request.getName() != null && categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("分类名称已存在: " + request.getName());
        }

        Category parent = null;
        Integer level = 1;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父级分类不存在: " + request.getParentId()));
            level = (parent.getLevel() == null ? 1 : parent.getLevel()) + 1;
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .parent(parent)
                .level(level)
                .sortOrder(request.getSortOrder())
                .isActive(request.getIsActive())
                .build();

        category.setSyncStatus(SyncStatus.UNSYNCED);
        Category saved = categoryRepository.save(category);
        // 事务提交后异步执行
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                difySyncService.syncCategory(saved.getId());
            }
        });
        return convertToDto(saved);
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new RuntimeException("分类名称已存在: " + request.getName());
            }
            category.setName(request.getName());
        }

        if (request.getSlug() != null && !request.getSlug().equals(category.getSlug())) {
            if (categoryRepository.existsBySlug(request.getSlug())) {
                throw new RuntimeException("分类别名已存在: " + request.getSlug());
            }
            category.setSlug(request.getSlug());
        }

        if (request.getParentId() != null) {
            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父级分类不存在: " + request.getParentId()));
            category.setParent(newParent);
            category.setLevel((newParent.getLevel() == null ? 1 : newParent.getLevel()) + 1);
        }

        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        category.setSyncStatus(SyncStatus.UNSYNCED);
        Category updated = categoryRepository.save(category);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                difySyncService.syncCategory(updated.getId());
            }
        });
        return convertToDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryById(Long id) {
        return categoryRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryByName(String name) {
        return categoryRepository.findByName(name).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));
        // 最简：将 isActive 置为 false 作为软删占位
        category.setIsActive(false);
        categoryRepository.save(category);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                difySyncService.syncCategory(id);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getChildren(Long parentId) {
        return categoryRepository.findByParent_Id(parentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private CategoryDto convertToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() == null ? null : category.getParent().getId())
                .level(category.getLevel())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}



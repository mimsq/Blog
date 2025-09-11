package com.xingmiao.blog.app.controller;

import com.xingmiao.blog.app.repository.CategoryRepository;
import com.xingmiao.blog.app.service.DifySyncService;
import com.xingmiao.blog.common.domain.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DifySyncController {

    private final DifySyncService difySyncService;
    private final CategoryRepository categoryRepository;

    @PostMapping("/categories/{id}/sync")
    public ResponseEntity<Void> syncCategoryToDify(@PathVariable("id") Long id) {
        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        difySyncService.syncCategory(id);
        return ResponseEntity.accepted().build();
    }
}



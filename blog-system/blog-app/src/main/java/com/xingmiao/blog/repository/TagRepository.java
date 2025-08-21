package com.xingmiao.blog.repository;

import com.xingmiao.blog.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    Optional<Tag> findBySlug(String slug);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
}





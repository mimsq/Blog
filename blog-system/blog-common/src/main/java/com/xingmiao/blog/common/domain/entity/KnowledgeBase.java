package com.xingmiao.blog.common.domain.entity;

import com.xingmiao.blog.common.domain.enums.KnowledgeBaseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "knowledge_bases",
       indexes = {
               @Index(name = "idx_status", columnList = "status")
       },
       uniqueConstraints = {
               @UniqueConstraint(name = "uk_knowledge_bases_name", columnNames = {"name"}),
               @UniqueConstraint(name = "uk_dify_kb_id", columnNames = {"dify_kb_id"})
       })
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "dify_kb_id", length = 100)
    private String difyKbId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private KnowledgeBaseStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}



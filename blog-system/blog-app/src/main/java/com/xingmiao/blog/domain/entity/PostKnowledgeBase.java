package com.xingmiao.blog.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post_knowledge_bases",
       indexes = {
               @Index(name = "idx_knowledge_base_id", columnList = "knowledge_base_id")
       },
       uniqueConstraints = {
               @UniqueConstraint(name = "uk_post_kb", columnNames = {"post_id", "knowledge_base_id"})
       })
public class PostKnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pkb_post_id"))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_base_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pkb_knowledge_base_id"))
    private KnowledgeBase knowledgeBase;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}



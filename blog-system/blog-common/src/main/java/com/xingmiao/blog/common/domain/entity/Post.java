package com.xingmiao.blog.common.domain.entity;

import com.xingmiao.blog.common.domain.enums.ContentType;
import com.xingmiao.blog.common.domain.enums.PostStatus;
import com.xingmiao.blog.common.domain.enums.SyncStatus;
import com.xingmiao.blog.common.domain.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posts",
       indexes = {
               @Index(name = "idx_category_id", columnList = "category_id"),
               @Index(name = "idx_status", columnList = "status"),
               @Index(name = "idx_visibility", columnList = "visibility"),
               @Index(name = "idx_published_at", columnList = "published_at"),
               @Index(name = "idx_created_at", columnList = "created_at")
       },
       uniqueConstraints = {
               @UniqueConstraint(name = "uk_posts_slug", columnNames = {"slug"})
       })
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "slug", nullable = false, length = 200)
    private String slug;

    @Column(name = "excerpt", columnDefinition = "text")
    private String excerpt;

    @Column(name = "content", columnDefinition = "longtext", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 20, nullable = false)
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PostStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 20, nullable = false)
    private Visibility visibility;

    @Column(name = "password", length = 100)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",
            foreignKey = @ForeignKey(name = "fk_posts_category_id"))
    private Category category;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "text")
    private String metaDescription;

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "comment_count")
    private Long commentCount;

    @Column(name = "dify_document_id", length = 100)
    private String difyDocumentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private SyncStatus syncStatus;

    @Column(name = "sync_error", columnDefinition = "TEXT")
    private String syncError;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (viewCount == null) {
            viewCount = 0L;
        }
        if (likeCount == null) {
            likeCount = 0L;
        }
        if (commentCount == null) {
            commentCount = 0L;
        }
        if (syncStatus == null) {
            syncStatus = SyncStatus.UNSYNCED;
        }
        if (status == PostStatus.PUBLISHED && publishedAt == null) {
            publishedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == PostStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }
}



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
@Table(name = "post_access_keys",
       indexes = {
               @Index(name = "idx_post_id", columnList = "post_id"),
               @Index(name = "idx_is_active", columnList = "is_active"),
               @Index(name = "idx_starts_at", columnList = "starts_at"),
               @Index(name = "idx_ends_at", columnList = "ends_at")
       })
public class PostAccessKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "fk_post_access_keys_post_id"), nullable = false)
    private Post post;

    @Column(name = "passcode_hash", nullable = false, length = 128)
    private String passcodeHash;

    @Column(name = "passcode_salt", nullable = false, length = 64)
    private String passcodeSalt;

    @Column(name = "label", length = 100)
    private String label;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



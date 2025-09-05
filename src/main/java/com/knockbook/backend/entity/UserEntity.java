package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Immutable // readonly
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA default constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder only
@Builder
public class UserEntity {

    public enum Status { ACTIVE, PENDING, LOCKED }

    @Id
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    private Long id; // BIGINT(UNSIGNED) ↔ Java Long

    @Column(name = "email", length = 255, nullable = false, insertable = false, updatable = false)
    private String email;

    @Column(name = "display_name", length = 100, nullable = false, insertable = false, updatable = false)
    private String displayName;

    @Column(name = "avatar_url", length = 2048, insertable = false, updatable = false)
    private String avatarUrl;

    @Column(name = "mbti", length = 4, insertable = false, updatable = false)
    private String mbti;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, insertable = false, updatable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", insertable = false, updatable = false)
    private LocalDateTime deletedAt;
}

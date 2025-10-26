package com.knockbook.backend.entity;

import com.knockbook.backend.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA default constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder only
@Builder
public class UserEntity {

    public enum Status {ACTIVE, PENDING, LOCKED}
    public enum Role {USER, ADMIN, MODERATOR}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // BIGINT(UNSIGNED) ↔ Java Long

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "display_name", length = 100, nullable = false)
    private String displayName;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Column(name = "mbti", length = 4)
    private String mbti;

    @Column(name = "bio", length = 160)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at", insertable = false, updatable = true)
    private Instant deletedAt;

    public User toDomain(List<String> favoriteCategories) {
        return User.builder()
                .id(id)
                .email(email)
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .mbti(mbti)
                .role(User.Role.valueOf(role.name()))
                .bio(bio)
                .favoriteBookCategories(favoriteCategories)
                .status(User.Status.valueOf(status.name()))
                .build();
    }
}

package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "auth_providers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA default constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder only
@Builder
public class AuthProividerEntity {

    public enum Kind { oauth, local, oidc }

    @Id
    @Column(name = "provider_code", length = 32, nullable = false)
    private String providerCode;

    @Column(name = "display_name", length = 64, nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", length = 32, nullable = false)
    private Kind kind;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Column(name = "icon_url", length = 512)
    private String iconUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

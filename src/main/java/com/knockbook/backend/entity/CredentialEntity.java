package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "credentials")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA default constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder only
@Builder
public class CredentialEntity {

    @Id
    @Column(name = "identity_id", nullable = false)
    private Long identityId; // PK = FK(identities.id)

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "password_updated_at", nullable = false)
    private Instant passwordUpdatedAt;
}

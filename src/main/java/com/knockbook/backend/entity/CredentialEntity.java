package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Table(name = "credentials")
@Immutable // readonly
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA default constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder only
@Builder
public class CredentialEntity {

    public enum Algorithm { argon2id, bcrypt }

    @Id
    @Column(name = "identity_id", nullable = false, updatable = false, insertable = false)
    private Long identityId; // PK = FK(identities.id)

    @Column(name = "password_hash", length = 255, nullable = false, updatable = false, insertable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "password_algo", length = 32, nullable = false, updatable = false, insertable = false)
    private Algorithm passwordAlgo;

    @Column(name = "password_updated_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime passwordUpdatedAt;
}

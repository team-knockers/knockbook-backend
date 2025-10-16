package com.knockbook.backend.entity;

import com.knockbook.backend.domain.PointBalance;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "point_balances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PointBalanceEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private Integer balance;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    public static PointBalanceEntity toEntity(PointBalance d) {
        if (d == null) {
            return null;
        }
        return PointBalanceEntity.builder()
                .userId(d.getUserId())
                .balance(d.getBalance())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    public PointBalance toDomain() {
        return PointBalance.builder()
                .userId(userId)
                .balance(balance)
                .updatedAt(updatedAt)
                .build();
    }
}

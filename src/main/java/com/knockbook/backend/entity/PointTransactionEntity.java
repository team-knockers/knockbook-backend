package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "point_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PointTransactionEntity {

    public enum PointTransactionKind { EARN, SPEND, EXPIRE, ADJUST }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointTransactionKind kind;

    @Column(name = "amount_signed", nullable = false)
    private Integer amountSigned;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "order_id")
    private Long orderId;

    @Column(length = 200)
    private String memo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}

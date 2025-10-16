package com.knockbook.backend.entity;

import com.knockbook.backend.domain.OrderPayment;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderPaymentEntity {

    public enum PaymentMethod { KAKAOPAY, TOSSPAY }
    public enum PaymentTxStatus { READY, APPROVED, PARTIAL_CANCELLED, CANCELLED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Column(name = "provider")
    private String provider;

    @Column(name = "tx_id")
    private String txId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentTxStatus status;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public static OrderPaymentEntity toEntity(OrderPayment d) {
        if (d == null) {
            return null;
        }
        return OrderPaymentEntity.builder()
                .id(d.getId())
                .orderId(d.getOrderId())
                .method(OrderPaymentEntity.PaymentMethod.valueOf(d.getMethod().name()))
                .provider(d.getProvider())
                .txId(d.getTxId())
                .amount(d.getAmount())
                .status(OrderPaymentEntity.PaymentTxStatus.valueOf(d.getStatus().name()))
                .approvedAt(LocalDateTime.from(d.getApprovedAt()))
                .cancelledAt(LocalDateTime.from(d.getCancelledAt()))
                .build();
    }

    public OrderPayment toDomain() {
        return OrderPayment.builder()
                .id(id)
                .orderId(orderId)
                .method(OrderPayment.Method.valueOf(method.name()))
                .provider(provider)
                .txId(txId)
                .amount(amount)
                .status(OrderPayment.TxStatus.valueOf(status.name()))
                .approvedAt(Instant.from(approvedAt))
                .cancelledAt(Instant.from(cancelledAt))
                .createdAt(Instant.from(createdAt))
                .updatedAt(Instant.from(updatedAt))
                .build();
    }
}

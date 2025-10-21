package com.knockbook.backend.dto;

import com.knockbook.backend.domain.PointTransaction;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PointTransactionResponse {
    private String id;
    private String kind;
    private Integer amountSigned;
    private String orderNo;
    private String memo;
    private String createdAt;

    public static PointTransactionResponse fromDomain(PointTransaction tx) {
        return PointTransactionResponse.builder()
                .id(tx.getId() != null ? tx.getId().toString() : null)
                .kind(tx.getKind() != null ? tx.getKind().name() : null)
                .amountSigned(tx.getAmountSigned())
                .orderNo(tx.getOrderNo() != null ? tx.getOrderNo() : null)
                .memo(tx.getMemo())
                .createdAt(tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : null)
                .build();
    }
}

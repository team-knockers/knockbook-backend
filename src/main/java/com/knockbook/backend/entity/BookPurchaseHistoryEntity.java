package com.knockbook.backend.entity;

import com.knockbook.backend.domain.BookPurchaseHistory;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

@Entity
@Table(name = "book_purchase_histories")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookPurchaseHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="order_id")
    private Long orderId;

    @Column(name="book_id", nullable=false)
    private Long bookId;

    @Column(name="purchase_count", nullable=false)
    private Integer purchaseCount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="first_purchased_at", nullable=false)
    private Date firstPurchasedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="last_purchased_at", nullable=false)
    private Date lastPurchasedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_at", nullable=false, updatable=false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="updated_at", nullable=false)
    private Date updatedAt;

    /* Entity -> Domain */
    public BookPurchaseHistory toDomain() {
        return BookPurchaseHistory.builder()
                .id(id)
                .userId(userId)
                .bookId(bookId)
                .purchaseCount(purchaseCount)
                .firstPurchasedAt(firstPurchasedAt.toInstant())
                .lastPurchasedAt(lastPurchasedAt.toInstant())
                .build();
    }

    /* Domain -> Entity (new) */
    public static BookPurchaseHistoryEntity fromDomain(BookPurchaseHistory d) {
        return BookPurchaseHistoryEntity.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .bookId(d.getBookId())
                .purchaseCount(d.getPurchaseCount() == null ? 1 : d.getPurchaseCount())
                .firstPurchasedAt(Date.from(nz(d.getFirstPurchasedAt())))
                .lastPurchasedAt(Date.from(nz(d.getLastPurchasedAt())))
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    private static Instant nz(Instant i) {
        return i == null ? Instant.now().atOffset(ZoneOffset.UTC).toInstant() : i;
    }
}

package com.knockbook.backend.entity;

import com.knockbook.backend.domain.BookRentalHistory;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

@Entity
@Table(name = "book_rental_histories")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookRentalHistoryEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="order_id")
    private Long orderId;

    @Column(name="book_id", nullable=false)
    private Long bookId;

    @Column(name="rental_count", nullable=false)
    private Integer rentalCount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="last_rental_start_at", nullable=false)
    private Date lastRentalStartAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="last_rental_end_at", nullable=false)
    private Date lastRentalEndAt;

    @Column(name="last_rental_days", nullable=false)
    private Integer lastRentalDays;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_at", nullable=false, updatable=false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="updated_at", nullable=false)
    private Date updatedAt;

    public BookRentalHistory toDomain() {
        return BookRentalHistory.builder()
                .id(id)
                .userId(userId)
                .bookId(bookId)
                .rentalCount(rentalCount)
                .lastRentalStartAt(lastRentalStartAt.toInstant())
                .lastRentalEndAt(lastRentalEndAt.toInstant())
                .lastRentalDays(lastRentalDays)
                .build();
    }

    public static BookRentalHistoryEntity fromDomain(BookRentalHistory d) {
        return BookRentalHistoryEntity.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .bookId(d.getBookId())
                .rentalCount(d.getRentalCount() == null ? 1 : d.getRentalCount())
                .lastRentalStartAt(Date.from(nz(d.getLastRentalStartAt())))
                .lastRentalEndAt(Date.from(nz(d.getLastRentalEndAt())))
                .lastRentalDays(d.getLastRentalDays())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    private static Instant nz(Instant i) {
        return i == null ? Instant.now().atOffset(ZoneOffset.UTC).toInstant() : i;
    }
}

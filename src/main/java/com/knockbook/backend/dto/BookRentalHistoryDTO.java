package com.knockbook.backend.dto;

import com.knockbook.backend.domain.BookRentalHistory;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookRentalHistoryDTO {
    private String id;
    private String bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookImageUrl;
    private Integer rentalCount;
    private Instant lastRentalStartAt;
    private Instant lastRentalEndAt;
    private Integer lastRentalDays;

    public static BookRentalHistoryDTO fromDomain(BookRentalHistory d) {
        return BookRentalHistoryDTO.builder()
                .id(d.getId().toString())
                .bookId(d.getBookId().toString())
                .bookTitle(d.getBookTitle())
                .bookAuthor(d.getBookAuthor())
                .bookImageUrl(d.getBookImageUrl())
                .rentalCount(d.getRentalCount())
                .lastRentalStartAt(d.getLastRentalStartAt())
                .lastRentalEndAt(d.getLastRentalEndAt())
                .lastRentalDays(d.getLastRentalDays())
                .build();
    }
}

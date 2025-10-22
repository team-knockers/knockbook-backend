package com.knockbook.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.knockbook.backend.domain.BookRentalHistoryDetails;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
    private Instant lastRentalStartAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
    private Instant lastRentalEndAt;

    private Integer lastRentalDays;

    public static BookRentalHistoryDTO fromDomain(final BookRentalHistoryDetails d) {
        final var history = d.getHistory();
        return BookRentalHistoryDTO.builder()
                .id(history.getId().toString())
                .bookId(history.getBookId().toString())
                .bookTitle(d.getBookTitle())
                .bookAuthor(d.getBookAuthor())
                .bookImageUrl(d.getBookImageUrl())
                .rentalCount(history.getRentalCount())
                .lastRentalStartAt(history.getLastRentalStartAt())
                .lastRentalEndAt(history.getLastRentalEndAt())
                .lastRentalDays(history.getLastRentalDays())
                .build();
    }
}

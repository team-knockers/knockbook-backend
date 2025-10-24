package com.knockbook.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.knockbook.backend.domain.BookPurchaseHistoryDetails;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookPurchaseHistoryDTO {
    private String id;
    private String bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookImageUrl;
    private Integer purchaseCount;
    private Boolean reviewdByMe;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
    private Instant firstPurchasedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
    private Instant lastPurchasedAt;

    public static BookPurchaseHistoryDTO fromDomain(final BookPurchaseHistoryDetails d) {
        final var history = d.getHistory();
        return BookPurchaseHistoryDTO.builder()
                .id(history.getId().toString())
                .bookId(history.getBookId().toString())
                .bookTitle(d.getBookTitle())
                .bookAuthor(d.getBookAuthor())
                .bookImageUrl(d.getBookImageUrl())
                .purchaseCount(history.getPurchaseCount())
                .firstPurchasedAt(history.getFirstPurchasedAt())
                .lastPurchasedAt(history.getLastPurchasedAt())
                .build();
    }
}

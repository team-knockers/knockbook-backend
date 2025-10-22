package com.knockbook.backend.dto;

import com.knockbook.backend.domain.BookPurchaseHistory;
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
    private Instant firstPurchasedAt;
    private Instant lastPurchasedAt;

    public static BookPurchaseHistoryDTO fromDomain(BookPurchaseHistory d) {
        return BookPurchaseHistoryDTO.builder()
                .id(d.getId().toString())
                .bookId(d.getBookId().toString())
                .bookTitle(d.getBookTitle())
                .bookAuthor(d.getBookAuthor())
                .bookImageUrl(d.getBookImageUrl())
                .purchaseCount(d.getPurchaseCount())
                .firstPurchasedAt(d.getFirstPurchasedAt())
                .lastPurchasedAt(d.getLastPurchasedAt())
                .build();
    }
}

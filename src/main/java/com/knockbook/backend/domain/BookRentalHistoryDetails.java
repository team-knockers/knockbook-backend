package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookRentalHistoryDetails {
    private BookRentalHistory history;
    private String bookTitle;
    private String bookAuthor;
    private String bookImageUrl;
}

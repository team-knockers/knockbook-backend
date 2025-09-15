package com.knockbook.backend.exception;

public class BookNotFoundException extends ApplicationException {

    public BookNotFoundException(String bookId) {
        super("BOOK_NOT_FOUND", "BOOK not found: bookId=%s".formatted(bookId));
    }
}
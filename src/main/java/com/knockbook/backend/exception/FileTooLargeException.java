package com.knockbook.backend.exception;

public class FileTooLargeException extends ApplicationException {

    public FileTooLargeException(String original) {
        super("FILE_TOO_LARGE",
                "file too large (20MB) %s".formatted(original));
    }
}


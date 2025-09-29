package com.knockbook.backend.exception;

public class EmptyFileException extends ApplicationException {

    public EmptyFileException(String name) {
        super("EMPTY_FILE", "file is empty name=%s".formatted(name));
    }
}


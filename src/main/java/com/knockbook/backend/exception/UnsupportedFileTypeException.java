package com.knockbook.backend.exception;

public class UnsupportedFileTypeException extends ApplicationException {

    public UnsupportedFileTypeException(String ext) {
        super("UNSUPPORTED_FILE_TYPE",
                "unsupported file type: %s".formatted(ext));
    }
}


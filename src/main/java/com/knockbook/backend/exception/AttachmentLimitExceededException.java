package com.knockbook.backend.exception;

public class AttachmentLimitExceededException extends ApplicationException {

    public AttachmentLimitExceededException(int max, int requested) {
        super("ATTACHMENT_LIMIT_EXCEEDED",
                "attachment limit exceeded: max=%d, requested=%d".formatted(max, requested));
    }
}


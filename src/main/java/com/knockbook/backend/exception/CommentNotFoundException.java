package com.knockbook.backend.exception;

public class CommentNotFoundException extends ApplicationException {

    public CommentNotFoundException(String commentId) {
        super("COMMENT_NOT_FOUND", "Comment not found: commentId=%s".formatted(commentId));
    }
}

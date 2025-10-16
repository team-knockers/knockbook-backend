package com.knockbook.backend.exception;

public class PostNotFoundException extends ApplicationException {

    public PostNotFoundException(String postId) {
        super("POST_NOT_FOUND", "Post not found: postId=%s".formatted(postId));
    }
}

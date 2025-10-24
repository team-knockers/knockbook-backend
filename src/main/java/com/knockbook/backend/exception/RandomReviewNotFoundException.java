package com.knockbook.backend.exception;

public class RandomReviewNotFoundException extends ApplicationException {

    public RandomReviewNotFoundException(Integer rating) {
        super("REVIEW_NOT_FOUND",
                rating != null
                        ? "No review found with rating: " + rating
                        : "No review found"
        );
    }
}

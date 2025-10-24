package com.knockbook.backend.service;

import com.knockbook.backend.domain.ReviewedItem;
import com.knockbook.backend.repository.BookReviewRepository;
import com.knockbook.backend.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final BookReviewRepository bookReviewRepo;
    private final ProductReviewRepository productReviewRepo;

    public List<ReviewedItem> getReviewedItemIdsAll(final Long userId) {
        final var reviewedBookItems = bookReviewRepo.findAllBy(userId).stream()
                .map(r -> ReviewedItem.builder()
                        .itemType(r.getTransactionType().name().contains("RENTAL") ?
                                ReviewedItem.ItemType.BOOK_RENTAL
                                : ReviewedItem.ItemType.BOOK_PURCHASE)
                        .id(String.valueOf(r.getBookId()))
                        .build())
                .toList();

        final var reviewedProductItems = productReviewRepo.findProductIdsReviewedByUser(userId).stream()
                .map(pid -> ReviewedItem.builder()
                        .itemType(ReviewedItem.ItemType.PRODUCT)
                        .id(String.valueOf(pid))
                        .build())
                .toList();

        final var result = new ArrayList<ReviewedItem>(reviewedBookItems.size() + reviewedProductItems.size());
        result.addAll(reviewedBookItems);
        result.addAll(reviewedProductItems);

        return result;
    }
}

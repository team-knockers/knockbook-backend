package com.knockbook.backend.service;

import com.knockbook.backend.domain.Cart;
import com.knockbook.backend.domain.CartItem;
import com.knockbook.backend.domain.PointsPolicy;
import com.knockbook.backend.exception.BookNotFoundException;
import com.knockbook.backend.exception.OpenCartNotFoundException;
import com.knockbook.backend.exception.ProductNotFoundException;
import com.knockbook.backend.repository.BookRepository;
import com.knockbook.backend.repository.CartRepository;
import com.knockbook.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final ProductRepository productRepository;

    public Cart getOrCreateOpenCart(final Long userId) {
        return cartRepository.findOpenByUserId(userId)
                .orElseGet(() -> cartRepository.createEmpty(userId));
    }

    public Cart addBookPurchase(final Long userId,
                                final Long bookId,
                                final int qty) {
        final var refType = CartItem.RefType.BOOK_PURCHASE;
        final var cart = getOrCreateOpenCart(userId);
        final var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId.toString()));

        final var item = CartItem.builder()
                .id(null)
                .cartId(cart.getId())
                .refType(refType)
                .refId(bookId)
                .titleSnapshot(book.getTitle())
                .thumbnailUrl(book.getCoverThumbnailUrl())
                .listPriceSnapshot(book.getPurchaseAmount())
                .salePriceSnapshot(book.getDiscountedPurchaseAmount())
                .rentalDays(0)
                .rentalPriceSnapshot(null)
                .quantity(Math.max(1, qty)) // += qty
                .pointsRate(PointsPolicy.of(refType))
                .build();

        return cartRepository.addItem(cart.getId(), item);
    }

    public Cart addBookRental(final Long userId,
                              final Long bookId,
                              final int days,
                              final int qty) {

        final var refType = CartItem.RefType.BOOK_RENTAL;
        final var cart = getOrCreateOpenCart(userId);
        final var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId.toString()));

        final var item = CartItem.builder()
                .id(null)
                .cartId(cart.getId())
                .refType(refType)
                .refId(bookId)
                .titleSnapshot(book.getTitle())
                .thumbnailUrl(book.getCoverThumbnailUrl())
                .listPriceSnapshot(null)
                .salePriceSnapshot(null)
                .rentalDays(days)
                .rentalPriceSnapshot(book.getRentalAmount())
                .quantity(Math.max(1, qty)) // += qty
                .pointsRate(PointsPolicy.of(refType))
                .build();
        return cartRepository.addItem(cart.getId(), item);
    }

    public Cart addProduct(final Long userId,
                           final Long productId,
                           final int qty) {

        final var refType = CartItem.RefType.PRODUCT;
        final var cart = getOrCreateOpenCart(userId);
        final var product = productRepository.findProductById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        final var summary = product.getProductSummary();

        final var item = CartItem.builder()
                .id(null)
                .cartId(cart.getId())
                .refType(refType)
                .refId(productId)
                .titleSnapshot(summary.getName())
                .thumbnailUrl(summary.getThumbnailUrl())
                .listPriceSnapshot(summary.getUnitPriceAmount())
                .salePriceSnapshot(summary.getSalePriceAmount())
                .rentalDays(0)
                .rentalPriceSnapshot(null)
                .quantity(Math.max(1, qty)) // += qty
                .pointsRate(PointsPolicy.of(refType))
                .build();
        return cartRepository.addItem(cart.getId(), item);
    }

    public Cart removeAllOfThem(final Long userId,
                                final Long cartItemId) {
        final var cart = cartRepository.findOpenByUserId(userId)
                .orElseThrow(() -> new OpenCartNotFoundException(userId));
        return cartRepository.deleteItem(cart.getId(), cartItemId);
    }

    public Cart decrementItem(final Long userId,
                              final Long cartItemId,
                              final int qty) {
        final var cart = cartRepository.findOpenByUserId(userId)
                .orElseThrow(() -> new OpenCartNotFoundException(userId));
        return cartRepository.decrementItem(cart.getId(), cartItemId, qty);
    }

    public Cart getCart(final Long userId) {
        return getOrCreateOpenCart(userId);
    }
}

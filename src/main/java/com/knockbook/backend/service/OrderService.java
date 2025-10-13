package com.knockbook.backend.service;

import com.knockbook.backend.domain.OrderAggregate;
import com.knockbook.backend.exception.InvalidCartItemsException;
import com.knockbook.backend.exception.OrderNotFoundException;
import com.knockbook.backend.repository.CartRepository;
import com.knockbook.backend.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderAggregate createDraftFromCart(final Long userId,
                                              final List<String> cartItemIds) {
        final var ids = cartItemIds.stream().map(Long::valueOf).toList();
        final var items = cartRepository.findSelectableItems(userId, ids);
        if (items.isEmpty() || items.size() != ids.size()) {
            throw new InvalidCartItemsException();
        }

        final var aggregate = OrderAggregate.builder()
                .id(null)
                .userId(userId)
                .cartId(null)
                .status(OrderAggregate.Status.PENDING)
                .paymentStatus(OrderAggregate.PaymentStatus.READY)
                .itemCount(0)
                .subtotalAmount(0)
                .discountAmount(0)
                .shippingAmount(0)
                .rentalAmount(0)
                .totalAmount(0)
                .pointsSpent(0)
                .pointsEarned(0)
                .build();

        return orderRepository.saveDraftFromCart(aggregate, items);
    }

    public OrderAggregate getById(final Long userId,
                                  final Long orderId) {
        return orderRepository.findDraftById(userId, orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}

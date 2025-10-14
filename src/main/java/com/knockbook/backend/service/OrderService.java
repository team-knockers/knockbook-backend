package com.knockbook.backend.service;

import com.knockbook.backend.domain.CouponIssuance;
import com.knockbook.backend.domain.OrderAggregate;
import com.knockbook.backend.domain.OrderItem;
import com.knockbook.backend.exception.CouponIssuanceNotFoundException;
import com.knockbook.backend.exception.InvalidCartItemsException;
import com.knockbook.backend.exception.OrderNotFoundException;
import com.knockbook.backend.repository.CartRepository;
import com.knockbook.backend.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final CouponService couponService;

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

    @Transactional
    public OrderAggregate applyCoupon(final Long userId,
                                      final Long orderId,
                                      final String issuanceIdRaw,
                                      final String codeRaw) {

        final var order = orderRepository.findDraftById(userId, orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        final var issuance = resolveIssuance(userId, issuanceIdRaw, codeRaw);
        validateIssuanceUsable(order, issuance);
        final var repriced = repriceWithCoupon(order, issuance);
        return orderRepository.updateDraftAmountsAndCoupon(repriced);
    }

    @Transactional
    public OrderAggregate removeCoupon(final Long userId,
                                       final Long orderId) {
        final var draft = orderRepository.findDraftById(userId, orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        final var repriced = repriceWithCoupon(draft, null);
        return orderRepository.updateDraftAmountsAndCoupon(repriced);
    }

    private CouponIssuance resolveIssuance(final Long userId,
                                           final String issuanceIdRaw,
                                           final String codeRaw) {
        if (issuanceIdRaw != null && !issuanceIdRaw.isBlank()) {
            return couponService.getOne(userId, Long.valueOf(issuanceIdRaw));
        }
        if (codeRaw != null && !codeRaw.isBlank()) {
            final var list = couponService.listByUser(userId, CouponIssuance.Status.AVAILABLE);
            return list.stream().filter(ci -> codeRaw.equalsIgnoreCase(ci.getCode()))
                    .findFirst()
                    .orElseThrow(() -> new CouponIssuanceNotFoundException(null, userId));
        }
        throw new IllegalArgumentException("couponIssuanceId or code required");
    }

    private void validateIssuanceUsable(OrderAggregate order, CouponIssuance ci) {
        if (ci.getStatus() != CouponIssuance.Status.AVAILABLE) {
            throw new IllegalStateException("COUPON_NOT_AVAILABLE");
        }
        final var now = Instant.now();
        if (ci.getExpiresAt() != null && now.isAfter(ci.getExpiresAt())) {
            throw new IllegalStateException("COUPON_EXPIRED");
        }
        // scope 매칭 & 최소주문금액 체크
        final var eligible = eligibleAmountByScope(order, ci.getScope()); // (아래 함수)
        if (ci.getMinOrderAmount() != null && eligible < ci.getMinOrderAmount()) {
            throw new IllegalStateException("MIN_ORDER_NOT_MET");
        }
    }

    private OrderAggregate repriceWithCoupon(OrderAggregate order, CouponIssuance ciOrNull) {
        final var subtotal = order.getItems().stream()
                .mapToInt(OrderItem::getLineSubtotalAmount).sum();
        final var lineDiscount = order.getItems().stream()
                .mapToInt(OrderItem::getLineDiscountAmount).sum();
        var shipping = nz(order.getShippingAmount());
        var rental   = nz(order.getRentalAmount());

        var couponDiscount = 0;
        Long appliedId = null;

        if (ciOrNull != null) {
            appliedId = ciOrNull.getId();

            if ("FREESHIP".equalsIgnoreCase(ciOrNull.getType())) {
                shipping = 0;
            } else {
                final var eligible = eligibleAmountByScope(order, ciOrNull.getScope());
                if ("PERCENT".equalsIgnoreCase(ciOrNull.getType())) {
                    final var raw = (eligible * nz(ciOrNull.getDiscountRateBp())) / 10_000; // bp → %
                    couponDiscount = ciOrNull.getMaxDiscountAmount() == null
                            ? raw : Math.min(raw, ciOrNull.getMaxDiscountAmount());
                } else {
                    couponDiscount = Math.min(eligible, nz(ciOrNull.getDiscountAmount()));
                }
            }
        }

        final var discount = lineDiscount + couponDiscount;
        final var total    = subtotal - discount + shipping;

        return OrderAggregate.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .cartId(order.getCartId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .itemCount(order.getItemCount())
                .subtotalAmount(subtotal)
                .discountAmount(discount)
                .shippingAmount(shipping)
                .rentalAmount(rental)
                .totalAmount(total)
                .pointsSpent(order.getPointsSpent())
                .pointsEarned(calcPoints(total, order.getItems())) // 네 규칙대로 바꿔도 됨
                .placedAt(order.getPlacedAt())
                .cancelledAt(order.getCancelledAt())
                .completedAt(order.getCompletedAt())
                .items(order.getItems())
                .appliedCouponIssuanceId(appliedId)
                .build();
    }

    private int eligibleAmountByScope(OrderAggregate o, String scope) {
        if (scope == null || scope.equalsIgnoreCase("ALL") || scope.equalsIgnoreCase("CART")) {
            return o.getItems().stream().mapToInt(OrderItem::getLineTotalAmount).sum();
        }
        return switch (scope) {
            case "BOOK_PURCHASE" -> sumLines(o, OrderItem.RefType.BOOK_PURCHASE);
            case "BOOK_RENTAL"   -> sumLines(o, OrderItem.RefType.BOOK_RENTAL);
            case "PRODUCT"       -> sumLines(o, OrderItem.RefType.PRODUCT);
            default -> 0;
        };
    }

    private int sumLines(OrderAggregate o, OrderItem.RefType t) {
        return o.getItems().stream().filter(i -> i.getRefType() == t)
                .mapToInt(OrderItem::getLineTotalAmount).sum();
    }

    private int calcPoints(int total, List<OrderItem> items) {
        return items.stream().mapToInt(OrderItem::getPointsEarnedItem).sum();
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }
}

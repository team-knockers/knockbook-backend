package com.knockbook.backend.service;

import com.knockbook.backend.domain.CouponIssuance;
import com.knockbook.backend.domain.OrderAggregate;
import com.knockbook.backend.domain.OrderItem;
import com.knockbook.backend.exception.InvalidCartItemsException;
import com.knockbook.backend.exception.OrderNotFoundException;
import com.knockbook.backend.exception.UserAddressNotFoundException;
import com.knockbook.backend.repository.CartRepository;
import com.knockbook.backend.repository.OrderRepository;
import com.knockbook.backend.repository.UserAddressRepository;
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
    private final PointsService pointsService;
    private final UserAddressRepository userAddressRepository;

    @Transactional
    public OrderAggregate createDraftFromCart(final Long userId,
                                              final List<String> cartItemIds) {
        final var ids = cartItemIds.stream().map(Long::valueOf).toList();
        final var items = cartRepository.findSelectableItems(userId, ids);
        if (items.isEmpty() || items.size() != ids.size()) {
            throw new InvalidCartItemsException();
        }

        final var existing = orderRepository.findPendingDraftByUser(userId);
        if (existing.isPresent()) {
            return orderRepository.replaceDraftFromCart(existing.get(), items, /*resetDiscounts=*/true);
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
    public List<OrderAggregate> listPaidByUser(final Long userId) {
        return orderRepository.findPaidByUser(userId);
    }

    @Transactional
    public OrderAggregate applyCoupon(final Long userId,
                                      final Long orderId,
                                      final String issuanceIdRaw) {

        final var order = orderRepository.findDraftById(userId, orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        final var issuance = resolveIssuance(userId, issuanceIdRaw);
        validateIssuanceUsable(order, issuance);
        final var repriced = reprice(order, issuance, order.getPointsSpent());
        return orderRepository.updateDraftAmountsAndCoupon(repriced);
    }

    @Transactional
    public OrderAggregate removeCoupon(final Long userId,
                                       final Long orderId) {
        final var order = orderRepository.findDraftById(userId, orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        final var repriced = reprice(order, null, order.getPointsSpent());
        return orderRepository.updateDraftAmountsAndCoupon(repriced);
    }

    @Transactional
    public OrderAggregate applyPoints(final Long userId,
                                      final Long orderId, final Integer requestedPoints) {
        final var draft = orderRepository.findDraftById(userId, orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        final int want = Math.max(0, requestedPoints == null ? 0 : requestedPoints);
        final int have = pointsService.getAvailablePoints(userId);

        final CouponIssuance appliedCoupon =
                draft.getAppliedCouponIssuanceId() == null ? null
                        : couponService.getOne(userId, draft.getAppliedCouponIssuanceId());

        final var pricedWithCoupon = reprice(draft, appliedCoupon, /*pointsToUse*/ 0);
        final int payableBase = pricedWithCoupon.getSubtotalAmount()
                - pricedWithCoupon.getDiscountAmount();
        final int maxByPayable = Math.max(0, payableBase);
        final int pointsToUse = Math.min(Math.min(want, have), maxByPayable);

        final var repriced = reprice(draft, appliedCoupon, pointsToUse);
        return orderRepository.updateDraftAmountsAndCoupon(repriced);
    }

    @Transactional
    public OrderAggregate removePoints(final Long userId, final Long orderId) {
        final var draft = orderRepository.findDraftById(userId, orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        final CouponIssuance appliedCoupon =
                draft.getAppliedCouponIssuanceId() == null ? null
                        : couponService.getOne(userId, draft.getAppliedCouponIssuanceId());

        final var repriced = reprice(draft, appliedCoupon, 0);
        return orderRepository.updateDraftAmountsAndCoupon(repriced);
    }

    @Transactional
    public OrderAggregate applyAddress(final Long userId, final Long orderId, final Long addressId) {

        final var orderOpt = orderRepository.findByIdAndUserIdForUpdate(userId, orderId);
        final var order = orderOpt.orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

         final var address = userAddressRepository.findById(addressId)
                 .orElseThrow(() -> new UserAddressNotFoundException(addressId));

        final var updated = OrderAggregate.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .cartId(order.getCartId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .itemCount(order.getItemCount())
                .subtotalAmount(order.getSubtotalAmount())
                .discountAmount(order.getDiscountAmount())
                .couponDiscountAmount(order.getCouponDiscountAmount())
                .shippingAmount(order.getShippingAmount())
                .rentalAmount(order.getRentalAmount())
                .totalAmount(order.getTotalAmount())
                .pointsSpent(order.getPointsSpent())
                .pointsEarned(order.getPointsEarned())
                .appliedCouponIssuanceId(order.getAppliedCouponIssuanceId())
                .placedAt(order.getPlacedAt())
                .paidAt(order.getPaidAt())
                .cancelledAt(order.getCancelledAt())
                .completedAt(order.getCompletedAt())
                .orderNo(order.getOrderNo())
                .shippingAddressId(addressId)
                .items(order.getItems())
                .build();

        return orderRepository.saveAggregate(updated);
    }

    private CouponIssuance resolveIssuance(final Long userId,
                                           final String issuanceIdRaw) {
        if (issuanceIdRaw == null || issuanceIdRaw.isBlank()) {
            throw new IllegalArgumentException("couponIssuanceId is required");
        }
        final long issuanceId;
        try {
            issuanceId = Long.parseLong(issuanceIdRaw);
            if (issuanceId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("couponIssuanceId must be a positive number");
        }
        return couponService.getOne(userId, issuanceId);
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

    private OrderAggregate reprice(OrderAggregate order,
                                   CouponIssuance ciOrNull,
                                   Integer pointsToUseOrNull) {

        final var subtotal = order.getItems().stream()
                .mapToInt(OrderItem::getLineSubtotalAmount).sum();
        final var lineDiscount = order.getItems().stream()
                .mapToInt(OrderItem::getLineDiscountAmount).sum();

        var shipping = nz(order.getShippingAmount());
        final var rental = nz(order.getRentalAmount());

        var couponDiscount = 0;
        Long appliedCouponId = null;

        if (ciOrNull != null) {
            appliedCouponId = ciOrNull.getId();

            if ("FREESHIP".equalsIgnoreCase(ciOrNull.getType())) {
                shipping = 0;
            } else {
                final int eligible = eligibleAmountByScope(order, ciOrNull.getScope());
                if ("PERCENT".equalsIgnoreCase(ciOrNull.getType())) {
                    final int raw = (eligible * nz(ciOrNull.getDiscountRateBp())) / 10_000;
                    couponDiscount = (ciOrNull.getMaxDiscountAmount() == null)
                            ? raw : Math.min(raw, ciOrNull.getMaxDiscountAmount());
                } else { // AMOUNT
                    couponDiscount = Math.min(eligible, nz(ciOrNull.getDiscountAmount()));
                }
            }
        }

        final var requestedPoints = Math.max(0, pointsToUseOrNull == null ? 0 : pointsToUseOrNull);
        final var payableBase = Math.max(0, subtotal - lineDiscount - couponDiscount);
        final var pointsSpent = Math.min(requestedPoints, payableBase);

        final var total = subtotal - lineDiscount - couponDiscount - pointsSpent + shipping;

        return OrderAggregate.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .cartId(order.getCartId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .itemCount(order.getItemCount())

                .subtotalAmount(subtotal)
                .discountAmount(lineDiscount)
                .couponDiscountAmount(couponDiscount)
                .shippingAmount(shipping)
                .rentalAmount(rental)
                .totalAmount(total)

                .pointsSpent(pointsSpent)
                .pointsEarned(calcPoints(total, order.getItems()))

                .appliedCouponIssuanceId(appliedCouponId)
                .placedAt(order.getPlacedAt())
                .cancelledAt(order.getCancelledAt())
                .completedAt(order.getCompletedAt())
                .items(order.getItems())
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

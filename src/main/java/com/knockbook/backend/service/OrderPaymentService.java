package com.knockbook.backend.service;

import com.knockbook.backend.domain.*;
import com.knockbook.backend.exception.CouponExpiredException;
import com.knockbook.backend.exception.CouponIssuanceNotFoundException;
import com.knockbook.backend.exception.CouponNotAvailableException;
import com.knockbook.backend.exception.InsufficientPointBalanceException;
import com.knockbook.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final OrderRepository orderRepository;
    private final CouponIssuanceRepository couponIssuanceRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final CartRepository cartRepository;

    @Transactional
    public PaymentApprovalResult approve(final Long userId,
                                         final Long orderId,
                                         final OrderPayment.Method method,
                                         final String provider,
                                         final String txId,
                                         final Integer amount) {

        final var nowLdt = LocalDateTime.now(KST);
        final var nowInstant = nowLdt.atZone(KST).toInstant();

        if (method == null || amount == null) {
            throw new IllegalArgumentException("INVALID_REQUEST");
        }

        // 1) Lock and validate the order
        final var order = orderRepository.findByIdAndUserIdForUpdate(userId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));
        if (order.getStatus() == OrderAggregate.Status.CANCELLED) {
            throw new IllegalStateException("ORDER_ALREADY_CANCELLED");
        }
        if (order.getPaymentStatus() != OrderAggregate.PaymentStatus.READY) {
            throw new IllegalStateException("ORDER_NOT_READY_FOR_PAYMENT");
        }
        if (!amount.equals(order.getTotalAmount())) {
            throw new IllegalStateException("PAYMENT_AMOUNT_MISMATCH");
        }

        // 2) Handle coupon redemption if applied
        if (order.getAppliedCouponIssuanceId() != null) {
            final var issuanceId = order.getAppliedCouponIssuanceId();
            if (couponRedemptionRepository.existsByIssuanceId(issuanceId)) {
                throw new IllegalStateException("COUPON_ALREADY_REDEEMED");
            }

            final var issuance = couponIssuanceRepository.findByIdAndUserIdForUpdate(issuanceId, userId)
                    .orElseThrow(() -> new CouponIssuanceNotFoundException(issuanceId, userId));

            if (issuance.getStatus() != CouponIssuance.Status.AVAILABLE) {
                throw new CouponNotAvailableException(issuanceId);
            }

            if (issuance.getExpiresAt() != null && issuance.getExpiresAt().isBefore(nowInstant)) {
                throw new CouponExpiredException(issuanceId);
            }

            final var redemption = CouponRedemption.builder()
                    .issuanceId(issuance.getId())
                    .orderId(order.getId())
                    .redeemedAmount(order.getCouponDiscountAmount() == null ? 0 : order.getCouponDiscountAmount())
                    .redeemedAt(nowInstant)
                    .build();

            couponRedemptionRepository.save(redemption);

            final var updatedIssuance = CouponIssuance.builder()
                    .id(issuance.getId())
                    .couponId(issuance.getCouponId())
                    .userId(issuance.getUserId())
                    .issuedAt(issuance.getIssuedAt())
                    .expiresAt(issuance.getExpiresAt())
                    .status(CouponIssuance.Status.USED)
                    .build();
            couponIssuanceRepository.save(updatedIssuance);
        }

        // 3) Deduct and record points usage
        final var spend = order.getPointsSpent() == null ? 0 : order.getPointsSpent();
        if (spend > 0) {
            final var bal = pointBalanceRepository.findByUserIdForUpdate(userId)
                    .orElse(PointBalance.builder().userId(userId).balance(0).build());
            if (bal.getBalance() < spend) {
                throw new InsufficientPointBalanceException(userId);
            }

            final var updatedBalance = PointBalance.builder()
                    .userId(userId)
                    .balance(bal.getBalance() - spend)
                    .updatedAt(bal.getUpdatedAt())
                    .build();

            pointBalanceRepository.save(updatedBalance);

            final var transaction = PointTransaction.builder()
                    .userId(userId)
                    .kind(PointTransaction.Kind.SPEND)
                    .amountSigned(-spend)
                    .orderId(order.getId())
                    .memo("Order payment spending")
                    .build();

            pointTransactionRepository.save(transaction);
        }

        // 4) Save payment record with APPROVED status
        final var payment = orderPaymentRepository.save(
                OrderPayment.builder()
                        .orderId(order.getId())
                        .method(OrderPayment.Method.valueOf(method.name()))
                        .provider(provider)
                        .txId(txId)
                        .amount(amount)
                        .status(OrderPayment.TxStatus.APPROVED)
                        .approvedAt(nowInstant)
                        .build());

        // 5) Update order status and timeline
        final var orderUpdated = order.paid(nowInstant);
        final var orderSaved = orderRepository.saveAggregate(orderUpdated);

        // 5.1) Earn and record points (based on the finalized orderSaved)
        final var earn = orderSaved.getPointsEarned() == null ? 0 : orderSaved.getPointsEarned();
        if (earn > 0) {
            final var balAfterPay = pointBalanceRepository.findByUserIdForUpdate(userId)
                    .orElse(PointBalance.builder().userId(userId).balance(0).build());

            final var updatedBalanceAfterPay = PointBalance.builder()
                    .userId(userId)
                    .balance(balAfterPay.getBalance() + earn)
                    .updatedAt(balAfterPay.getUpdatedAt())
                    .build();

            pointBalanceRepository.save(updatedBalanceAfterPay);

            pointTransactionRepository.save(PointTransaction.builder()
                    .userId(userId)
                    .kind(PointTransaction.Kind.EARN)
                    .amountSigned(earn)
                    .orderId(orderSaved.getId())
                    .memo("Order payment earning")
                    .build());
        }

        // 6) Cleanup cart items consumed by this order
        final var refs = orderSaved.getItems().stream().map(i ->
                        CartRef.builder()
                                .refId(i.getRefId())
                                .refType(i.getRefType().name())
                                .rentalDays(i.getRefType() == OrderItem.RefType.BOOK_RENTAL ? i.getRentalDays() : 0)
                                .build())
                .distinct().toList();

        cartRepository.deleteByUserIdAndRefs(userId, refs);

        return PaymentApprovalResult.builder()
                .userId(userId)
                .orderId(orderSaved.getId())
                .order(orderSaved)
                .payment(payment)
                .build();
    }
}


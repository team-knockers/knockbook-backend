package com.knockbook.backend.service;

import com.knockbook.backend.repository.CouponIssuanceRepository;
import com.knockbook.backend.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponIssuanceRepository couponIssuanceRepository;

    @Transactional
    public void grantWelcomeCoupons(Long userId) {
        final var now = Instant.now();
        final var codes = List.of("WELCOME_PROD_10","WELCOME_BOOK_PURCHASE_10","WELCOME_BOOK_RENTAL_10","WELCOME_CART_3000");
        final var couponIds = couponRepository.findActiveIdsByCodes(codes, now);
        if (couponIds.isEmpty()) { return; }
        couponIssuanceRepository.insertIfNotExists(userId, couponIds);
    }
}

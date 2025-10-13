package com.knockbook.backend.controller;

import com.knockbook.backend.domain.CouponIssuance;
import com.knockbook.backend.dto.CouponIssuanceResponse;
import com.knockbook.backend.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/coupon-issuances")
public class CouponIssuanceController {

    private final CouponService service;

    @GetMapping
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<List<CouponIssuanceResponse>> list(
            @PathVariable String userId,
            @RequestParam(name = "status", required = false) String status) {

        final var userIdL = Long.valueOf(userId);
        final var statusOpt = parseStatus(status);

        final var models = service.listByUser(userIdL, statusOpt);
        final var body = models.stream().map(CouponIssuanceResponse::fromModel).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{issuanceId}")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<CouponIssuanceResponse> getOne(
            @PathVariable String userId,
            @PathVariable String issuanceId) {

        final var model = service.getOne(Long.valueOf(userId), Long.valueOf(issuanceId));
        return ResponseEntity.ok(CouponIssuanceResponse.fromModel(model));
    }

    private CouponIssuance.Status parseStatus(String raw) {
        if (raw == null || raw.isBlank()) { return null; }
        return CouponIssuance.Status.valueOf(raw.trim().toUpperCase());
    }
}

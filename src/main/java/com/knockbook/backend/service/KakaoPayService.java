package com.knockbook.backend.service;

import com.knockbook.backend.component.KakaoPayClient;
import com.knockbook.backend.component.KakaoPayProps;
import com.knockbook.backend.domain.KakaoReadyInfo;
import com.knockbook.backend.domain.OrderPayment;
import com.knockbook.backend.domain.PaymentApprovalResult;
import com.knockbook.backend.repository.OrderPaymentQueryRepository;
import com.knockbook.backend.repository.OrderPaymentRepository;
import com.knockbook.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoPayService {
    private final KakaoPayProps props;
    private final KakaoPayClient kakao;
    private final OrderRepository orderRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final OrderPaymentQueryRepository orderPaymentQueryRepository;
    private final PaymentApprovalService paymentApprovalService;

    // Prepare payment: call Kakao ready API and pre-save with READY status
    @Transactional
    public KakaoReadyInfo ready(Long userId, Long orderId) {
        final var draft = orderRepository.findDraftById(userId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));
        final var amount = draft.getTotalAmount();

        final var q = "orderId=" + URLEncoder.encode(String.valueOf(orderId), StandardCharsets.UTF_8);
        final var approvalUrl = props.getApprovalReturnBase() + "?" + q;
        final var cancelUrl   = props.getCancelReturnBase()   + "?" + q;
        final var failUrl     = props.getFailReturnBase()     + "?" + q;

        final var res = kakao.ready(Map.of(
                "cid", props.getCid(),
                "partner_order_id", String.valueOf(orderId),
                "partner_user_id", String.valueOf(userId),
                "item_name", "Knockbook Order #" + orderId,
                "quantity", 1,
                "total_amount", amount,
                "tax_free_amount", 0,
                "approval_url", approvalUrl,
                "cancel_url", cancelUrl,
                "fail_url", failUrl
        ));

        final var tid = (String) res.get("tid");
        final var pc  = (String) res.get("next_redirect_pc_url");
        final var mo  = (String) res.get("next_redirect_mobile_url");
        final var app = (String) res.get("next_redirect_app_url");

        // READY pre-save (idempotency / traceability)
        orderPaymentRepository.save(OrderPayment.builder()
                .orderId(orderId)
                .method(OrderPayment.Method.KAKAOPAY)
                .provider("kakao")
                .txId(tid)
                .amount(amount)
                .status(OrderPayment.TxStatus.READY)
                .build());

        return KakaoReadyInfo.builder()
                .tid(tid)
                .nextRedirectPcUrl(pc)
                .nextRedirectMobileUrl(mo)
                .nextRedirectAppUrl(app)
                .amount(amount)
                .orderId(orderId)
                .userId(userId)
                .build();
    }

    /** Approve payment: use pg_token to call the internal payment approval domain */
    @Transactional
    public PaymentApprovalResult approve(Long userId, Long orderId, String pgToken) {
        final var ready = orderPaymentQueryRepository.findReadyByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("READY_PAYMENT_NOT_FOUND"));
        final var tid = ready.getTxId();

        // Kakao approve API call
        kakao.approve(Map.of(
                "cid", props.getCid(),
                "tid", tid,
                "partner_order_id", String.valueOf(orderId),
                "partner_user_id", String.valueOf(userId),
                "pg_token", pgToken,
                "total_amount", ready.getAmount()
        ));

        // Internal approval (coupon, points, order, payment)
        return paymentApprovalService.approve(userId, orderId, OrderPayment.Method.KAKAOPAY,
                "kakao", tid, ready.getAmount());
    }
}

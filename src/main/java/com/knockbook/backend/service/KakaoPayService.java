package com.knockbook.backend.service;

import com.knockbook.backend.component.KakaoPayClient;
import com.knockbook.backend.component.KakaoPayProps;
import com.knockbook.backend.domain.KakaoReadyInfo;
import com.knockbook.backend.domain.OrderPayment;
import com.knockbook.backend.domain.PaymentApprovalResult;
import com.knockbook.backend.exception.OrderNotFoundException;
import com.knockbook.backend.repository.OrderPaymentQueryRepository;
import com.knockbook.backend.repository.OrderPaymentRepository;
import com.knockbook.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoPayService {
    private final KakaoPayProps props;
    private final KakaoPayClient kakao;
    private final OrderRepository orderRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final OrderPaymentQueryRepository orderPaymentQueryRepository;
    private final OrderPaymentService paymentApprovalService;

    // Prepare payment: call Kakao ready API and pre-save with READY status
    @Transactional
    public KakaoReadyInfo ready(Long userId, Long orderId) {
        final var draft = orderRepository.findDraftById(userId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));
        final var amount = draft.getTotalAmount();

        final var approvalUrl = appendQuery(props.getApprovalReturnBase(), String.valueOf(orderId));
        final var cancelUrl   = appendQuery(props.getCancelReturnBase(), String.valueOf(orderId));
        final var failUrl     = appendQuery(props.getFailReturnBase(), String.valueOf(orderId));

        final var body = new HashMap<String, Object>();
        body.put("cid", props.getCid());
        body.put("partner_order_id", String.valueOf(orderId));
        body.put("partner_user_id", String.valueOf(userId));
        body.put("item_name", "문앞의 책방");
        body.put("quantity", 1);
        body.put("total_amount", amount);
        body.put("tax_free_amount", 0);
        body.put("approval_url", approvalUrl);
        body.put("cancel_url", cancelUrl);
        body.put("fail_url", failUrl);

        final var res = kakao.ready(body);

        final var tid = (String) res.get("tid");
        final var pc  = (String) res.get("next_redirect_pc_url");
        final var mo  = (String) res.get("next_redirect_mobile_url");
        final var app = (String) res.get("next_redirect_app_url");

        // READY pre-save (idempotency / traceability)
        final var payment = OrderPayment.builder()
                .orderId(orderId)
                .method(OrderPayment.Method.KAKAOPAY)
                .provider("kakao")
                .txId(tid)
                .amount(amount)
                .status(OrderPayment.TxStatus.READY)
                .build();

        orderPaymentRepository.save(payment);

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

    @Transactional
    public PaymentApprovalResult approve(Long orderId, String pgToken) {
        final var ready = orderPaymentQueryRepository.findReadyByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("READY_PAYMENT_NOT_FOUND"));

        final var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        final var userId = order.getUserId();

        kakao.approve(Map.of(
                "cid", props.getCid(),
                "tid", ready.getTxId(),
                "partner_order_id", String.valueOf(orderId),
                "partner_user_id", String.valueOf(userId),
                "pg_token", pgToken,
                "total_amount", ready.getAmount()
        ));

        return paymentApprovalService.approve(
                userId, orderId, OrderPayment.Method.KAKAOPAY, "kakao", ready.getTxId(), ready.getAmount()
        );
    }

    private static String appendQuery(String base, String v) {
        var sep = base.contains("?") ? "&" : "?";
        return base + sep + URLEncoder.encode("orderId", StandardCharsets.UTF_8) + "="
                + URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}

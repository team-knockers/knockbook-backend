package com.knockbook.backend.repository;

import com.knockbook.backend.domain.OrderPayment;
import com.knockbook.backend.entity.OrderPaymentEntity;
import com.knockbook.backend.entity.QOrderPaymentEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderPaymentQueryRepositoryImpl implements OrderPaymentQueryRepository {

    private final EntityManager em;
    private final JPAQueryFactory qf;
    private static final QOrderPaymentEntity qPay = QOrderPaymentEntity.orderPaymentEntity;

    @Override
    public Optional<OrderPayment> findReadyByOrderId(Long orderId) {
        final var e = qf.selectFrom(qPay)
                .where(qPay.orderId.eq(orderId)
                        .and(qPay.status.eq(OrderPaymentEntity.PaymentTxStatus.READY)))
                .orderBy(qPay.id.desc())
                .fetchFirst();
        return Optional.ofNullable(e).map(OrderPaymentEntity::toDomain);
    }

    @Override
    public Optional<OrderPayment> findByTxId(String txId) {
        final var e = qf.selectFrom(qPay)
                .where(qPay.txId.eq(txId))
                .fetchFirst();
        return Optional.ofNullable(e).map(OrderPaymentEntity::toDomain);
    }
}

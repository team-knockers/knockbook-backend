package com.knockbook.backend.service;

import com.knockbook.backend.domain.PointTransaction;
import com.knockbook.backend.repository.PointTransactionRepository;
import com.knockbook.backend.repository.PointsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsRepository pointsRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Transactional(readOnly = true)
    public int getAvailablePoints(Long userId) {
        return Math.max(0, pointsRepository.findBalance(userId).orElse(0));
    }

    @Transactional(readOnly = true)
    public List<PointTransaction> getUserTransactions(Long userId) {
        return pointTransactionRepository.findAllByUserId(userId);
    }
}

package com.knockbook.backend.service;

import com.knockbook.backend.repository.PointsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsRepository pointsRepository;

    @Transactional(readOnly = true)
    public int getAvailablePoints(Long userId) {
        return Math.max(0, pointsRepository.findBalance(userId).orElse(0));
    }
}

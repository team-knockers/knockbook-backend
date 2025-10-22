package com.knockbook.backend.service;

import com.knockbook.backend.domain.BookPurchaseHistory;
import com.knockbook.backend.domain.BookRentalHistory;
import com.knockbook.backend.repository.BookPurchaseHistoryRepository;
import com.knockbook.backend.repository.BookRentalHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final BookPurchaseHistoryRepository purchaseRepo;
    private final BookRentalHistoryRepository rentalRepo;

    @Transactional(readOnly = true)
    public List<BookPurchaseHistory> listPurchasesByUser(Long userId) {
        return purchaseRepo.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<BookRentalHistory> listRentalsByUser(Long userId) {
        return rentalRepo.findAllByUserId(userId);
    }
}

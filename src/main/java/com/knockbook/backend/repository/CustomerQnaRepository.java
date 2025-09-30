package com.knockbook.backend.repository;

import com.knockbook.backend.domain.CustomerQna;
import com.knockbook.backend.domain.CustomerQnaFile;

import java.util.List;
import java.util.Optional;

public interface CustomerQnaRepository {
    CustomerQna insert(CustomerQna qna);
    Optional<CustomerQna> findById(Long id);
    List<CustomerQna> findAllByUserId(Long userId, int page, int size);
    long countByUserId(Long userId);
}

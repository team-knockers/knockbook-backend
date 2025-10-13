package com.knockbook.backend.repository;

import com.knockbook.backend.domain.ProductInquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductInquiryRepository {
    public Page<ProductInquiry> findProductInquiries(
            Long productId,
            Pageable pageable
    );

    public Long createInquiry(
            Long productId,
            Long userId,
            String title,
            String questionBody
    );
}

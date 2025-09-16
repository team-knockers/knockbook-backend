package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductDetail {
    private Long id;

    private String manufacturerName;
    private String isImported;
    private String importCountry;

    private List<String> galleryImageUrls;
    private List<String> descriptionImageUrls;
}

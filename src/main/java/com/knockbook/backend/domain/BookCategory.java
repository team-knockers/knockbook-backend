package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookCategory {

    public enum Category {
        fiction, essay, humanities, parenting, cooking, health,
        lifestyle, business, selfImprovement, politics, culture, religion,
        entertainment, technology, language, science, travel, it
    }

    private Long id;
    private Category codeName;
    private String displayName;
}

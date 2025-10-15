package com.knockbook.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetLoungePostSummaryResponse {

    private List<LoungePostSummaryDto> posts;
    private int page;
    private int size;
    private int totalItems;
    private int totalPages;
}

package com.knockbook.backend.domain;

public enum SortOrder {
    asc, desc;

    public static SortOrder parseOrDefault(String s) {
        return "asc".equals(s) ? asc : desc;
    }
}

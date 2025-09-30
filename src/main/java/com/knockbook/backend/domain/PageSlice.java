package com.knockbook.backend.domain;

import java.util.List;

public record PageSlice<T> (List<T> items, long total) {}

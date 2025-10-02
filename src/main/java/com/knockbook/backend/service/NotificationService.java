package com.knockbook.backend.service;

import com.knockbook.backend.domain.Notification;
import com.knockbook.backend.domain.PageSlice;
import com.knockbook.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public PageSlice<Notification> getAll(final int page, final int size) {
        final var items = repository.findAll(page, size);
        final var total = repository.count();
        return new PageSlice<>(items, total);
    }
}

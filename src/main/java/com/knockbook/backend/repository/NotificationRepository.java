package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Notification;

import java.util.List;

public interface NotificationRepository {
    List<Notification> findAll(final int page, final int size);
    long count();
}

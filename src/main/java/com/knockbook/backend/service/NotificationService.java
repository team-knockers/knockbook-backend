package com.knockbook.backend.service;

import com.knockbook.backend.domain.Notification;
import com.knockbook.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public List<Notification> getAll() {
        return repository.findAll();
    }
}

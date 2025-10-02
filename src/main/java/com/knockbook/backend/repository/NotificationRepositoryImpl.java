package com.knockbook.backend.repository;

import com.knockbook.backend.domain.Notification;
import com.knockbook.backend.entity.QNotificationEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository{

    private final JPAQueryFactory query;

    @Override
    public List<Notification> findAll() {
        final var entity = QNotificationEntity.notificationEntity;
        return query
                .selectFrom(entity)
                .orderBy(entity.createdAt.desc())
                .fetch()
                .stream()
                .map(e ->
                        Notification.builder()
                                .id(e.getId())
                                .title(e.getTitle())
                                .content(e.getContent())
                                .createdAt(e.getCreatedAt())
                                .updatedAt(e.getUpdatedAt())
                                .build())
                .collect(Collectors.toList());
    }
}

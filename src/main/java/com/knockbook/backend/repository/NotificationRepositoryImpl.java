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
    public List<Notification> findAll(final int page, final int size) {
        final var entity = QNotificationEntity.notificationEntity;
        return query
                .selectFrom(entity)
                .orderBy(entity.createdAt.desc())
                .offset((long)page * size)
                .limit(size)
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

    @Override
    public long count() {
        final var entity = QNotificationEntity.notificationEntity;
        final var total = query
                .select(entity.count())
                .from(entity)
                .fetchOne();
        return total == null ? 0L : total;
    }
}

package com.knockbook.backend.repository;

import com.knockbook.backend.domain.CustomerQna;
import com.knockbook.backend.domain.CustomerQnaFile;
import com.knockbook.backend.entity.*;
import com.knockbook.backend.exception.AttachmentLimitExceededException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CustomerQnaRepositoryImpl implements CustomerQnaRepository {

    private final int MAX_NUM_FILES = 3;
    private final EntityManager em;
    private final JPAQueryFactory query;

    private final QCustomerQnaEntity qQna = QCustomerQnaEntity.customerQnaEntity;
    private final QCustomerQnaFileEntity qFile = QCustomerQnaFileEntity.customerQnaFileEntity;

    @Override
    @Transactional
    public CustomerQna insert(CustomerQna qna) {
        final var entity = CustomerQnaEntity.builder()
                .userId(qna.getUserId())
                .title(qna.getTitle())
                .content(qna.getContent())
                .status(CustomerQnaEntity.Status.PENDING)
                .build();
        em.persist(entity);
        em.flush(); // get PK
        final var qnaId = entity.getId();

        final var files = qna.getFiles();
        if (files != null && !files.isEmpty()) {
            final var numFiles = files.size();
            if (numFiles > MAX_NUM_FILES) {
                throw new AttachmentLimitExceededException(MAX_NUM_FILES, numFiles);
            }

            for (final var file : files) {
                final var fileEntity = CustomerQnaFileEntity.builder()
                        .qnaId(qnaId)
                        .fileUrl(file.getFileUrl())
                        .fileName(file.getFileName())
                        .fileSize(file.getFileSize())
                        .fileType(file.getFileType())
                        .build();
                em.persist(fileEntity);
            }
        }
        return findById(qnaId).orElseThrow();
    }

    @Override
    public Optional<CustomerQna> findById(Long id) {
        final var qnaEntity = query
                .selectFrom(qQna)
                .where(qQna.id.eq(id))
                .fetchOne();

        if (qnaEntity == null) {
            return Optional.empty();
        }

        final var fileEntities = query
                .selectFrom(qFile)
                .where(qFile.qnaId.eq(id))
                .orderBy(qFile.id.asc())
                .fetch();

        final var qnaFiles = fileEntities == null || fileEntities.isEmpty() ?
                new ArrayList<CustomerQnaFile>() :
                fileEntities.stream()
                        .map(f -> CustomerQnaFile.builder()
                                .id(f.getId())
                                .fileName(f.getFileName())
                                .fileUrl(f.getFileUrl())
                                .fileType(f.getFileType())
                                .fileSize(f.getFileSize())
                                .build()
                ).toList();

        final var found = CustomerQna.builder()
                .id(qnaEntity.getId())
                .title(qnaEntity.getTitle())
                .content(qnaEntity.getContent())
                .status(CustomerQna.Status.valueOf(qnaEntity.getStatus().name()))
                .answeredAt(qnaEntity.getAnsweredAt())
                .createdAt(qnaEntity.getCreatedAt())
                .updatedAt(qnaEntity.getUpdatedAt())
                .files(qnaFiles)
                .build();

        return Optional.of(found);
    }

    @Override
    public List<CustomerQna> findAllByUserId(Long userId, int page, int size) {
        final var qnaEntities = query
                .selectFrom(qQna)
                .where(qQna.userId.eq(userId))
                .orderBy(qQna.createdAt.desc(), qQna.id.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();

        if (qnaEntities == null || qnaEntities.isEmpty()) {
            return List.of();
        }

        final var ids = qnaEntities.stream()
                .map(CustomerQnaEntity::getId).toList();

        final var fileEntities = query
                .selectFrom(qFile)
                .where(qFile.qnaId.in(ids))
                .orderBy(qFile.id.asc())
                .fetch();

        final var filesByQnaId =
                fileEntities == null
                        ? Map.<Long, List<CustomerQnaFileEntity>>of()
                        : fileEntities.stream()
                        .collect(Collectors.groupingBy(CustomerQnaFileEntity::getQnaId));

        return qnaEntities.stream().map(e -> {
            final var files = filesByQnaId.getOrDefault(e.getId(), List.of()).stream()
                    .map(f -> CustomerQnaFile.builder()
                            .id(f.getId())
                            .fileName(f.getFileName())
                            .fileUrl(f.getFileUrl())
                            .fileType(f.getFileType())
                            .fileSize(f.getFileSize())
                            .build())
                    .toList();

            return CustomerQna.builder()
                    .id(e.getId())
                    .userId(e.getUserId())
                    .title(e.getTitle())
                    .content(e.getContent())
                    .status(CustomerQna.Status.valueOf(e.getStatus().name()))
                    .answeredAt(e.getAnsweredAt())
                    .createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt())
                    .files(files)
                    .build();
        }).toList();
    }

    @Override
    public long countByUserId(Long userId) {
        final var total = query.select(qQna.count())
                .from(qQna)
                .where(qQna.userId.eq(userId))
                .fetchOne();
        return total == null ? 0L : total;
    }
}


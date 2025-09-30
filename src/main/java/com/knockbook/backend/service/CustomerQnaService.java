package com.knockbook.backend.service;

import com.knockbook.backend.component.ImgbbUploader;
import com.knockbook.backend.domain.CustomerQna;
import com.knockbook.backend.domain.CustomerQnaFile;
import com.knockbook.backend.domain.PageSlice;
import com.knockbook.backend.exception.AttachmentLimitExceededException;
import com.knockbook.backend.exception.FileTooLargeException;
import com.knockbook.backend.exception.UnsupportedFileTypeException;
import com.knockbook.backend.repository.CustomerQnaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerQnaService {

    private static final int MAX_NUM_FILES = 3;
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024; // 20MB
    private static final Set<String> ALLOWED =
            Set.of("gif","png","jpg","jpeg");

    private final CustomerQnaRepository repository;
    private final ImgbbUploader imgbb;

    @Transactional
    public CustomerQna create(final Long userId,
                              final String title,
                              final String content,
                              final MultipartFile[] files) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }

        final var qnaFiles = new ArrayList<CustomerQnaFile>();
        if (files != null && files.length > 0) {

            if (files.length > MAX_NUM_FILES) {
                throw new AttachmentLimitExceededException(MAX_NUM_FILES, files.length);
            }

            for (final var file : files) {

                if (file == null || file.isEmpty()) {
                    continue;
                }

                final var original = getOriginal(file);

                final var url = imgbb.upload(file);
                qnaFiles.add(CustomerQnaFile.builder()
                        .fileName(original)
                        .fileUrl(url)
                        .fileSize(file.getSize())
                        .fileType(file.getContentType())
                        .build());
            }
        }

        final var qna = CustomerQna.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .files(qnaFiles)
                .build();

        return repository.insert(qna);
    }

    @Transactional
    public PageSlice<CustomerQna> findListByUser(final Long userId,
                                                 final int page,
                                                 final int size) {
        final var items = repository.findAllByUserId(userId, page, size);
        final var total = repository.countByUserId(userId);
        return new PageSlice<>(items, total);
    }

    private static String getOriginal(MultipartFile file) {
        final var original = Optional.ofNullable(file.getOriginalFilename())
                .orElse("file");

        final var ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
                : "";

        if (!ALLOWED.contains(ext)) { throw new UnsupportedFileTypeException(ext); }
        if (file.getSize() > MAX_FILE_SIZE) { throw new FileTooLargeException(original); }
        return original;
    }
}

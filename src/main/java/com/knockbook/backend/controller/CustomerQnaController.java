package com.knockbook.backend.controller;

import com.knockbook.backend.domain.CustomerQna;
import com.knockbook.backend.domain.CustomerQnaFile;
import com.knockbook.backend.dto.CustomerQnaFileResponse;
import com.knockbook.backend.dto.CustomerQnaPageResponse;
import com.knockbook.backend.dto.CustomerQnaResponse;
import com.knockbook.backend.service.CustomerQnaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@RestController
@RequestMapping(path = "/customers")
public class CustomerQnaController {

    @Autowired
    private CustomerQnaService customerQnaService;

    @PreAuthorize("#userId == authentication.name")
    @PostMapping(
            path = "/{userId}/qna",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerQnaResponse> registerCustomerQna(
            @PathVariable("userId") final String userId,
            @RequestParam(name = "title") final String title,
            @RequestParam(name = "content") final String content,
            @RequestParam(name = "files", required = false) final List<MultipartFile> files) {
        final var qna = customerQnaService.create(Long.valueOf(userId), title, content, files);
        final var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(qna.getId())
                .toUri();

        final var fileReqs = (qna.getFiles() == null ?
                new ArrayList<CustomerQnaFileResponse>()
                : qna.getFiles().stream()
                .map(f -> CustomerQnaFileResponse.builder()
                        .fileName(f.getFileName())
                        .fileUrl(f.getFileUrl())
                        .fileType(f.getFileType())
                        .fileSize(String.valueOf(f.getFileSize()))
                        .build())
                .toList());

        final var body = CustomerQnaResponse.builder()
                .id(qna.getId())
                .userId(qna.getUserId())
                .title(qna.getTitle())
                .content(qna.getContent())
                .status(qna.getStatus().toString())
                .answer(qna.getAnswer())
                .answeredAt(qna.getAnsweredAt())
                .createdAt(qna.getCreatedAt())
                .files(fileReqs)
                .build();

        return ResponseEntity.created(location).body(body);
    }

    @PreAuthorize("#userId == authentication.name")
    @GetMapping(path = "/{userId}/qna")
    public ResponseEntity<CustomerQnaPageResponse> getCustomerQnaList(
            @PathVariable("userId") final String userId,
            @RequestParam(name = "page") final int page,
            @RequestParam(name = "size") final int size) {
        
        final var zeroBasePage = Math.max(Integer.valueOf(page) - 1, 0);
        final var userIdLong = Long.valueOf(userId);
        final var slice = customerQnaService.findListByUser(userIdLong, zeroBasePage, size);

        final var qnas = slice.items().stream().map(qna -> {
            final var files = emptyFiles.test(qna)
                    ? List.<CustomerQnaFileResponse>of()
                    : qna.getFiles().stream()
                    .map(f -> CustomerQnaFileResponse.builder()
                            .fileName(f.getFileName())
                            .fileUrl(f.getFileUrl())
                            .fileType(f.getFileType())
                            .fileSize(String.valueOf(f.getFileSize()))
                            .build()).toList();
            return CustomerQnaResponse.builder()
                    .id(qna.getId())
                    .userId(qna.getUserId())
                    .title(qna.getTitle())
                    .content(qna.getContent())
                    .status(qna.getStatus().toString())
                    .answer(qna.getAnswer())
                    .answeredAt(qna.getAnsweredAt())
                    .createdAt(qna.getCreatedAt())
                    .files(files)
                    .build();
        }).toList();

        final var body = CustomerQnaPageResponse.builder()
                .qnas(qnas)
                .totalQnas(slice.total())
                .build();

        return ResponseEntity.ok().body(body);
    }

    private static final Predicate<CustomerQna> emptyFiles =
            qna -> qna.getFiles() == null || qna.getFiles().isEmpty();
}

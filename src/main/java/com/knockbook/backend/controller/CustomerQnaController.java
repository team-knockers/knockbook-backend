package com.knockbook.backend.controller;

import com.knockbook.backend.dto.CustomerQnaFileResponse;
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
            @RequestPart(name = "title") final String title,
            @RequestPart(name = "content") final String content,
            @RequestPart(name = "files", required = false) final MultipartFile[] files) {
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
}

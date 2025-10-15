package com.knockbook.backend.service;

import com.knockbook.backend.domain.*;
import com.knockbook.backend.exception.PostNotFoundException;
import com.knockbook.backend.repository.LoungePostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LoungePostService {

    @Autowired
    private LoungePostRepository loungePostRepository;

    @Autowired
    private UserService userService;

    public Page<LoungePostSummary> getPostsSummary(Pageable pageable) {

        final var page = loungePostRepository.findPostsByPageable(pageable);

        final var userIds = page.getContent().stream()
                .map(LoungePostSummary::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final var displayNameMap = new HashMap<Long, String>();
        for (final var userId : userIds) {
            try {
                final var user = userService.getUser(userId);
                final var name = user.getDisplayName();
                displayNameMap.put(userId, name);
            } catch (final Exception e) {
                displayNameMap.put(userId, "Unknown");
            }
        }

        final var updatedContent = page.getContent().stream()
                .map(summary -> summary.toBuilder()
                        .displayName(displayNameMap.get(summary.getUserId()))
                        .build())
                .collect(Collectors.toList());

        return new PageImpl<>(updatedContent, pageable, page.getTotalElements());
    }

    public LoungePost getPostDetails(Long id) {
        final var post = loungePostRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(String.valueOf(id)));

        final var userId = post.getUserId();
        if (userId == null) {
            throw new IllegalStateException("Post has no userId, postId=" + id);
        }

        final var user = userService.getUser(userId);

        return post.toBuilder()
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .build();
    }
}

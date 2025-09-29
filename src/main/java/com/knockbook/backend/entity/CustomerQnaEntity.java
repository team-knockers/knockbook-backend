package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "customer_qna")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CustomerQnaEntity {

    public enum Status { PENDING, ANSWERED, CLOSED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "answer")
    private String answer;

    @Column(name="answered_at", insertable = false, updatable = false)
    private Instant answeredAt;

    @Column(name="created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name="updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;
}

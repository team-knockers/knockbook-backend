package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "customer_qna")
@Getter
@NoArgsConstructor
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
    private Status status = Status.PENDING;

    @Column(name = "answer")
    private String answer;

    @Column(name="created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name="updated_at", nullable = false, insertable = false, updatable = false)
    private Instant udpatedAt;
}

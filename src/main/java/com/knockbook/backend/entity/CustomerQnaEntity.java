package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_qna")
@Getter
@NoArgsConstructor
public class CustomerQnaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 300)
    private String content;

    public enum Status { PENDING, ANSWERED, CLOSED }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.PENDING;

    @Lob
    private String answer;

    @Column(name="created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name="updated_at", nullable = false, insertable = false, updatable = false)
    private Instant udpatedAt;

    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerQnaFileEntity> files = new ArrayList<>();

    public void addFile(CustomerQnaFileEntity f) {
        files.add(f);
        f.setQna(this);
    }
}

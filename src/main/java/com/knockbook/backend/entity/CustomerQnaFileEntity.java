package com.knockbook.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "customer_qna_files")
@Getter
@Setter
@NoArgsConstructor
public class CustomerQnaFileEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "qna_id", nullable = false)
    private CustomerQnaEntity qna;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}

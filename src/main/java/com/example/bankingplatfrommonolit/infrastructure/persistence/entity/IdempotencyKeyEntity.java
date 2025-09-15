package com.example.bankingplatfrommonolit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
@NoArgsConstructor
public class IdempotencyKeyEntity {
    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false, length = 20)
    private String status;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String responseJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
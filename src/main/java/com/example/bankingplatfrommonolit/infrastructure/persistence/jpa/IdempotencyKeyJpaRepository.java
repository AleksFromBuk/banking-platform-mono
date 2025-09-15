package com.example.bankingplatfrommonolit.infrastructure.persistence.jpa;

import com.example.bankingplatfrommonolit.infrastructure.persistence.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyEntity, String> {
    @Modifying
    @Query("DELETE FROM IdempotencyKeyEntity i WHERE i.createdAt < :cutoff")
    int cleanup(@Param("cutoff") Instant cutoff);
}
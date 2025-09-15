package com.example.bankingplatfrommonolit.infrastructure.persistence.jpa;

import com.example.bankingplatfrommonolit.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findByCardId(UUID cardId, org.springframework.data.domain.Pageable p);

    @Query("SELECT t FROM TransactionEntity t JOIN CardEntity c ON t.cardId = c.id WHERE t.cardId = :cardId AND c.ownerId = :ownerId")
    List<TransactionEntity> findByCardIdAndOwnerId(@Param("cardId") UUID cardId, @Param("ownerId") UUID ownerId, org.springframework.data.domain.Pageable p);
}

package com.example.bankingplatfrommonolit.infrastructure.persistence.jpa;

import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import com.example.bankingplatfrommonolit.infrastructure.persistence.entity.CardEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardJpaRepository extends JpaRepository<CardEntity, UUID>, JpaSpecificationExecutor<CardEntity> {
    Optional<CardEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<CardEntity> findByOwnerId(UUID ownerId, org.springframework.data.domain.Pageable p);

    List<CardEntity> findByExpiryDateBefore(LocalDate date);

    @Modifying
    @Query("UPDATE CardEntity c SET c.status = :s WHERE c.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("s") CardStatus s);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CardEntity c where c.id=:id")
    Optional<CardEntity> findByIdForUpdate(@Param("id") UUID id);
}

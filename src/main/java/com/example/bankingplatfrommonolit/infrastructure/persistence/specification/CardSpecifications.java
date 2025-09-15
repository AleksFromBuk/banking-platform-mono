package com.example.bankingplatfrommonolit.infrastructure.persistence.specification;

import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import com.example.bankingplatfrommonolit.infrastructure.persistence.entity.CardEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public class CardSpecifications {

    public static Specification<CardEntity> byOwnerId(UUID ownerId) {
        return (root, query, cb) ->
                ownerId == null ? null : cb.equal(root.get("ownerId"), ownerId);
    }

    public static Specification<CardEntity> byStatus(CardStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<CardEntity> byStatusIn(Set<CardStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return null;
            return root.get("status").in(
                    statuses.stream().map(Enum::name).toArray()
            );
        };
    }

    public static Specification<CardEntity> byExpiryDateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from == null) return cb.lessThanOrEqualTo(root.get("expiryDate"), to);
            if (to == null) return cb.greaterThanOrEqualTo(root.get("expiryDate"), from);
            return cb.between(root.get("expiryDate"), from, to);
        };
    }

    public static Specification<CardEntity> byLast4Digits(String last4Digits) {
        return (root, query, cb) -> (last4Digits == null || last4Digits.isBlank())
                ? null
                : cb.equal(root.get("last4"), last4Digits);
    }

    public static Specification<CardEntity> byBalanceGreaterThanOrEqual(BigDecimal minBalance) {
        return (root, query, cb) ->
                minBalance == null ? null : cb.greaterThanOrEqualTo(root.get("balance"), minBalance);
    }

    public static Specification<CardEntity> byBalanceLessThanOrEqual(BigDecimal maxBalance) {
        return (root, query, cb) ->
                maxBalance == null ? null : cb.lessThanOrEqualTo(root.get("balance"), maxBalance);
    }
}
package com.example.bankingplatfrommonolit.infrastructure.persistence.adapter;

import com.example.bankingplatfrommonolit.domain.model.Card;
import com.example.bankingplatfrommonolit.domain.port.repo.CardFilter;
import com.example.bankingplatfrommonolit.domain.port.repo.CardRepositoryPort;
import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import com.example.bankingplatfrommonolit.infrastructure.exception.CardNotFoundException;
import com.example.bankingplatfrommonolit.infrastructure.mapper.CardMapper;
import com.example.bankingplatfrommonolit.infrastructure.persistence.entity.CardEntity;
import com.example.bankingplatfrommonolit.infrastructure.persistence.jpa.CardJpaRepository;
import com.example.bankingplatfrommonolit.infrastructure.persistence.specification.CardSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class CardRepositoryAdapter implements CardRepositoryPort {
    private final CardJpaRepository jpa;
    private final CardMapper mapper;

    @Override
    @Transactional
    public Card saveWithEncryptedPan(Card c, String enc) {
        CardEntity e = mapper.toEntity(c);
        e.setEncryptedPan(enc);
        return mapper.toDomain(jpa.save(e));
    }

    @Override
    public Optional<Card> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Card> findByIdAndOwnerId(UUID id, UUID ownerId) {
        return jpa.findByIdAndOwnerId(id, ownerId).map(mapper::toDomain);
    }

    @Override
    public List<Card> findByOwnerId(UUID ownerId, int page, int size) {
        return jpa.findByOwnerId(ownerId, PageRequest.of(page, size)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Card> findAll(int page, int size) {
        return jpa.findAll(PageRequest.of(page, size)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, CardStatus s) {
        jpa.updateStatus(id, s);
    }

    @Override
    @Transactional
    public void updateBalances(UUID fromId, BigDecimal nf, UUID toId, BigDecimal nt) {
        var from = jpa.findByIdForUpdate(fromId).orElseThrow(() -> new CardNotFoundException(fromId));;
        var to = jpa.findByIdForUpdate(toId).orElseThrow(() -> new CardNotFoundException(toId));
        from.setBalance(nf);
        to.setBalance(nt);
        jpa.save(from);
        jpa.save(to);
    }

    @Transactional
    @Override
    public void updateBalance(UUID id, BigDecimal nb) {
        var e = jpa.findByIdForUpdate(id).orElseThrow();
        e.setBalance(nb);
        jpa.save(e);
    }

    @Override
    public List<Card> findByOwnerIdAndFilters(UUID ownerId, CardFilter filter, int page, int size) {
        Specification<CardEntity> spec = Specification.where(CardSpecifications.byOwnerId(ownerId))
                .and(CardSpecifications.byExpiryDateBetween(filter.expiryDateFrom(), filter.expiryDateTo()))
                .and(CardSpecifications.byLast4Digits(filter.last4Digits()))
                .and(CardSpecifications.byBalanceGreaterThanOrEqual(filter.minBalance()))
                .and(CardSpecifications.byBalanceLessThanOrEqual(filter.maxBalance()));

        if (filter.statuses() != null && !filter.statuses().isEmpty()) {
            spec = spec.and(CardSpecifications.byStatusIn(filter.statuses()));
        } else if (filter.status() != null) {
            spec = spec.and(CardSpecifications.byStatus(filter.status()));
        }

        return jpa.findAll(spec, PageRequest.of(page, size))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Card> findAllWithFilters(CardFilter filter, int page, int size) {
        Specification<CardEntity> spec = Specification.where(
                        CardSpecifications.byExpiryDateBetween(filter.expiryDateFrom(), filter.expiryDateTo()))
                .and(CardSpecifications.byLast4Digits(filter.last4Digits()))
                .and(CardSpecifications.byBalanceGreaterThanOrEqual(filter.minBalance()))
                .and(CardSpecifications.byBalanceLessThanOrEqual(filter.maxBalance()));

        if (filter.statuses() != null && !filter.statuses().isEmpty()) {
            spec = spec.and(CardSpecifications.byStatusIn(filter.statuses()));
        } else if (filter.status() != null) {
            spec = spec.and(CardSpecifications.byStatus(filter.status()));
        }

        return jpa.findAll(spec, PageRequest.of(page, size))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Card> findByExpiryDateBefore(LocalDate date) {
        return jpa.findByExpiryDateBefore(date).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}

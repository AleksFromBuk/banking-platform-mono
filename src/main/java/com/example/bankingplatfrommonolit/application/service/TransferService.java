package com.example.bankingplatfrommonolit.application.service;

import com.example.bankingplatfrommonolit.application.dto.transfer.TransferDtos;
import com.example.bankingplatfrommonolit.domain.model.Transaction;
import com.example.bankingplatfrommonolit.domain.port.repo.CardRepositoryPort;
import com.example.bankingplatfrommonolit.domain.port.repo.TransactionRepositoryPort;
import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import com.example.bankingplatfrommonolit.domain.type.TxType;
import com.example.bankingplatfrommonolit.infrastructure.exception.ConflictException;
import com.example.bankingplatfrommonolit.infrastructure.exception.ForbiddenException;
import com.example.bankingplatfrommonolit.infrastructure.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
    private final CardRepositoryPort cards;
    private final TransactionRepositoryPort txs;
    private final IdempotencyService idem;

    private static final int MAX_RETRIES = 3;

//    @Transactional(isolation = Isolation.REPEATABLE_READ)
    ////    public TransferDtos.TransferResponse transfer(UUID requesterId, boolean isAdmin, UUID fromId, UUID toId, BigDecimal amount, String idemKey) {
    ////        if (idemKey == null || idemKey.isBlank()) {
    ////            throw new ConflictException("Idempotency-Key required");
    ////        }
    ////
    ////        if (amount.signum() <= 0) {
    ////            throw new ConflictException("Amount must be positive");
    ////        }
    ////
    ////        var completed = idem.shortCircuitIfCompleted(idemKey);
    ////        if (completed.isPresent()) {
    ////            return Json.read(completed.get());
    ////        }
    ////
    ////        if (!idem.tryStart(idemKey)) {
    ////            throw new ConflictException("Duplicate request");
    ////        }
    ////
    ////        int attempt = 0;
    ////        while (true) {
    ////            try {
    ////                attempt++;
    ////                var from = cards.findById(fromId).orElseThrow(() -> new NotFoundException("from"));
    ////                var to = cards.findById(toId).orElseThrow(() -> new NotFoundException("to"));
    ////
    ////                if (!isAdmin && (!from.getOwnerId().equals(requesterId) || !to.getOwnerId().equals(requesterId))) {
    ////                    throw new ForbiddenException("Forbidden");
    ////                }
    ////
    ////                if (!from.getOwnerId().equals(to.getOwnerId())) {
    ////                    throw new ConflictException("Cards must belong to same owner");
    ////                }
    ////
    ////                if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
    ////                    throw new ConflictException("Card not ACTIVE");
    ////                }
    ////
    ////                if (from.getBalance().compareTo(amount) < 0) {
    ////                    throw new ConflictException("Insufficient funds");
    ////                }
    ////
    ////                var newFrom = from.getBalance().subtract(amount);
    ////                var newTo = to.getBalance().add(amount);
    ////
    ////                cards.updateBalances(fromId, newFrom, toId, newTo);
    ////
    ////                txs.save(Transaction.builder()
    ////                        .id(UUID.randomUUID())
    ////                        .cardId(fromId)
    ////                        .type(TxType.DEBIT)
    ////                        .amount(amount)
    ////                        .balanceAfter(newFrom)
    ////                        .description("Transfer to " + toId)
    ////                        .createdAt(Instant.now())
    ////                        .build());
    ////
    ////                txs.save(Transaction.builder()
    ////                        .id(UUID.randomUUID())
    ////                        .cardId(toId)
    ////                        .type(TxType.CREDIT)
    ////                        .amount(amount)
    ////                        .balanceAfter(newTo)
    ////                        .description("Transfer from " + fromId)
    ////                        .createdAt(Instant.now())
    ////                        .build());
    ////
    ////                var resp = new TransferDtos.TransferResponse(fromId, toId, newFrom, newTo, "OK");
    ////                idem.complete(idemKey, Json.write(resp));
    ////                log.info("transfer ok from={} to={} amount={}", fromId, toId, amount);
    ////
    ////                return resp;
    ////            } catch (OptimisticLockingFailureException e) {
    ////                if (attempt >= MAX_RETRIES) {
    ////                    log.error("transfer failed after retries", e);
    ////                    throw e;
    ////                }
    ////                log.warn("retry transfer due to optimistic lock, attempt={}", attempt);
    ////            }
    ////        }
    ////    }
    ///
    ///
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransferDtos.TransferResponse transfer(UUID requesterId, boolean isAdmin, UUID fromId, UUID toId, BigDecimal amount, String idemKey) {
        if (idemKey == null || idemKey.isBlank()) {
            throw new ConflictException("Idempotency-Key required");
        }

        if (amount.signum() <= 0) {
            throw new ConflictException("Amount must be positive");
        }

        var completed = idem.shortCircuitIfCompleted(idemKey);
        if (completed.isPresent()) {
            return Json.read(completed.get());
        }

        if (!idem.tryStart(idemKey)) {
            throw new ConflictException("Duplicate request");
        }

        int attempt = 0;
        while (true) {
            try {
                attempt++;
                var from = cards.findById(fromId).orElseThrow(() -> new NotFoundException("from"));
                var to = cards.findById(toId).orElseThrow(() -> new NotFoundException("to"));

                if (!isAdmin && (!from.getOwnerId().equals(requesterId) || !to.getOwnerId().equals(requesterId))) {
                    throw new ForbiddenException("Forbidden");
                }

                if (!from.getOwnerId().equals(to.getOwnerId())) {
                    throw new ConflictException("Cards must belong to same owner");
                }

                if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
                    throw new ConflictException("Card not ACTIVE");
                }

                if (from.getBalance().compareTo(amount) < 0) {
                    throw new ConflictException("Insufficient funds");
                }

                var newFrom = from.getBalance().subtract(amount);
                var newTo = to.getBalance().add(amount);

                cards.updateBalances(fromId, newFrom, toId, newTo);

                txs.save(Transaction.builder()
                        .id(UUID.randomUUID())
                        .cardId(fromId)
                        .type(TxType.DEBIT)
                        .amount(amount)
                        .balanceAfter(newFrom)
                        .description("Transfer to " + toId)
                        .createdAt(Instant.now())
                        .build());

                txs.save(Transaction.builder()
                        .id(UUID.randomUUID())
                        .cardId(toId)
                        .type(TxType.CREDIT)
                        .amount(amount)
                        .balanceAfter(newTo)
                        .description("Transfer from " + fromId)
                        .createdAt(Instant.now())
                        .build());

                var resp = new TransferDtos.TransferResponse(fromId, toId, newFrom, newTo, "OK");
                idem.complete(idemKey, Json.write(resp));
                log.info("transfer ok from={} to={} amount={}", fromId, toId, amount);

                return resp;
            } catch (OptimisticLockingFailureException e) {
                if (attempt >= MAX_RETRIES) {
                    log.error("transfer failed after retries", e);
                    throw e;
                }
                log.warn("retry transfer due to optimistic lock, attempt={}", attempt);
            }
        }
    }

    static class Json {
        private static final ObjectMapper M = new ObjectMapper();

        static String write(Object object) {
            try {
                return M.writeValueAsString(object);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        static <T> T read(String s) {
            try {
                return M.readValue(s, (Class<T>) TransferDtos.TransferResponse.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

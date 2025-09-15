package com.example.bankingplatfrommonolit.application.service;

import com.example.bankingplatfrommonolit.application.dto.transaction.TransactionDtos;
import com.example.bankingplatfrommonolit.domain.model.Transaction;
import com.example.bankingplatfrommonolit.domain.port.repo.TransactionRepositoryPort;
import com.example.bankingplatfrommonolit.domain.type.TxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepositoryPort transactionRepository;

    @Transactional(readOnly = true)
    public List<TransactionDtos.TransactionResponse> getTransactionsByCardId(UUID cardId, UUID ownerId, int page, int size) {
        return transactionRepository.findByCardIdAndOwnerId(cardId, ownerId, page, size).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TransactionDtos.TransactionResponse toResponse(Transaction transaction) {
        return new TransactionDtos.TransactionResponse(
                transaction.getId(),
                transaction.getCardId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }

    // запись CREDIT-транзакции пополнения
    @Transactional
    public void recordTopUp(UUID cardId, BigDecimal amount, BigDecimal balanceAfter) {
        transactionRepository.save(
                Transaction.builder()
                        .id(UUID.randomUUID())
                        .cardId(cardId)
                        .type(TxType.CREDIT)
                        .amount(amount)
                        .balanceAfter(balanceAfter)
                        .description("TopUp")
                        .createdAt(java.time.Instant.now())
                        .build()
        );
    }
}

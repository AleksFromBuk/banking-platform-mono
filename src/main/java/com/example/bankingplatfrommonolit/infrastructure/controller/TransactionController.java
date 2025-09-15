package com.example.bankingplatfrommonolit.infrastructure.controller;

import com.example.bankingplatfrommonolit.application.dto.transaction.TransactionDtos;
import com.example.bankingplatfrommonolit.application.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/card/{cardId}")
    public List<TransactionDtos.TransactionResponse> getCardTransactions(
            @PathVariable UUID cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        return transactionService.getTransactionsByCardId(cardId, userId, page, size);
    }
}
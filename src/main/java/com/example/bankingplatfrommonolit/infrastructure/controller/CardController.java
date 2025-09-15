package com.example.bankingplatfrommonolit.infrastructure.controller;

import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
import com.example.bankingplatfrommonolit.application.service.CardCommandService;
import com.example.bankingplatfrommonolit.application.service.CardQueryService;
import com.example.bankingplatfrommonolit.domain.port.repo.CardFilter;
import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardCommandService cmd;
    private final CardQueryService cardQueryService;

    @GetMapping
    public List<CardDtos.CardView> myCards(
            Authentication a,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return cardQueryService.listForUser(UUID.fromString(a.getName()), page, size);
    }

    @GetMapping("/filter")
    public List<CardDtos.CardView> myCardsWithFilter(
            Authentication authentication,
            @RequestParam(value = "statuses", required = false) Set<CardStatus> statuses,
            @RequestParam(value = "expiryDateFrom", required = false) LocalDate expiryDateFrom,
            @RequestParam(value = "expiryDateTo", required = false) LocalDate expiryDateTo,
            @RequestParam(value = "last4Digits", required = false) String last4Digits,
            @RequestParam(value = "minBalance", required = false) BigDecimal minBalance,
            @RequestParam(value = "maxBalance", required = false) BigDecimal maxBalance,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        UUID userId = UUID.fromString(authentication.getName());
        CardFilter filter = new CardFilter(null, statuses, expiryDateFrom, expiryDateTo,
                last4Digits, minBalance, maxBalance);

        return cardQueryService.listForUserWithFilters(userId, filter, page, size);
    }

    @PostMapping("/{id}/block-request")
    public void block(Authentication a, @PathVariable UUID id) {
        cmd.requestBlock(UUID.fromString(a.getName()), id);
    }
}

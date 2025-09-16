package com.example.bankingplatfrommonolit.infrastructure.controller;

import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
import com.example.bankingplatfrommonolit.application.service.CardCommandService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
//@Profile({"test","dev"})
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
class AdminTestOnlyController {
    private final CardCommandService cmd;

    @PostMapping("/cards/{id}/topup")
    public CardDtos.CardView topup(@PathVariable UUID id, @RequestParam BigDecimal amount) {
        return cmd.topUp(id, amount);
    }
}

package com.example.bankingplatfrommonolit.infrastructure.controller;

import com.example.bankingplatfrommonolit.application.dto.transfer.TransferDtos;
import com.example.bankingplatfrommonolit.application.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService service;

    @PostMapping(
            value = "/transfer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TransferDtos.TransferResponse> transfer(
            Authentication a,
            @RequestHeader(name = "Idempotency-Key", required = true) String idemKey,
            @Valid @RequestBody TransferDtos.TransferRequest r
    ) {
        //log.debug("transfer: from={} to={} amount={}", r.fromCardId(), r.toCardId(), r.amount());
        boolean admin = a.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
        UUID me = UUID.fromString(a.getName());
        var resp = service.transfer(me, admin, r.fromCardId(), r.toCardId(), r.amount(), idemKey);
        return ResponseEntity.ok(resp);
    }
}
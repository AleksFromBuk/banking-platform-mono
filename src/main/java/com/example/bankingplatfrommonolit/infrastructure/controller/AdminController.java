package com.example.bankingplatfrommonolit.infrastructure.controller;

import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
import com.example.bankingplatfrommonolit.application.dto.user.UserDtos;
import com.example.bankingplatfrommonolit.application.service.CardCommandService;
import com.example.bankingplatfrommonolit.application.service.CardQueryService;
import com.example.bankingplatfrommonolit.application.service.UserService;
import com.example.bankingplatfrommonolit.domain.port.repo.CardFilter;
import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final CardCommandService cmd;
    private final CardQueryService cardQueryService;
    private final UserService userService;

    @PatchMapping("/cards/{id}/status")
    public void status(@PathVariable UUID id, @RequestParam CardStatus status) {
        cmd.changeStatusAsAdmin(id, status);
    }

    @GetMapping("/cards")
    public List<CardDtos.CardView> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return cardQueryService.listAll(page, size);
    }

    // полноправный CRUD: создание карты админом
    @PostMapping("/cards")
    public CardDtos.CardView create(@Valid @RequestBody CardDtos.CreateCardRequest r) {
        return cmd.create(r);
    }

    // мягкое удаление по ТЗ ("Удаляет карты") через статус
    @DeleteMapping("/cards/{id}")
    public void delete(@PathVariable UUID id) {
        cmd.changeStatusAsAdmin(id, CardStatus.DELETED);
    }

    @GetMapping("/cards/filter")
    public List<CardDtos.CardView> allWithFilters(
            @RequestParam(required = false) Set<CardStatus> statuses,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDateTo,
            @RequestParam(required = false) String last4Digits,
            @RequestParam(required = false) BigDecimal minBalance,
            @RequestParam(required = false) BigDecimal maxBalance,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        CardFilter filter = new CardFilter(null, statuses, expiryDateFrom, expiryDateTo,
                last4Digits, minBalance, maxBalance);

        return cardQueryService.listAllWithFilters(filter, page, size);
    }

    @GetMapping("/users")
    public List<UserDtos.UserResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return userService.getAllUsers(page, size);
    }

    @PatchMapping("/users/{userId}/status")
    public void updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam boolean active) {
        userService.updateUserStatus(userId, active);
    }

}
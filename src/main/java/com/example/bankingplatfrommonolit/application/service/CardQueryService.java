package com.example.bankingplatfrommonolit.application.service;

import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
import com.example.bankingplatfrommonolit.domain.model.Card;
import com.example.bankingplatfrommonolit.domain.port.repo.CardFilter;
import com.example.bankingplatfrommonolit.domain.port.repo.CardRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardQueryService {
    private final CardRepositoryPort cardRepository;

    @Transactional(readOnly = true)
    public List<CardDtos.CardView> listForUser(UUID userId, int page, int size) {
        return cardRepository.findByOwnerId(userId, page, size).stream()
                .map(CardCommandService::toView)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CardDtos.CardView> listAll(int page, int size) {
        return cardRepository.findAll(page, size).stream()
                .map(CardCommandService::toView)
                .collect(Collectors.toList());
    }

//    @Transactional(readOnly = true)
//    public List<CardDtos.CardView> listForUserWithFilters(UUID userId, CardFilter filter, int page, int size) {
//        return cards.findByOwnerIdAndFilters(userId, filter, page, size).stream()
//                .map(CardCommandService::toView)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public List<CardDtos.CardView> listAllWithFilters(CardFilter filter, int page, int size) {
//        return cards.findAllWithFilters(filter, page, size).stream()
//                .map(CardCommandService::toView)
//                .collect(Collectors.toList());
//    }
@Transactional(readOnly = true)
public List<CardDtos.CardView> listForUserWithFilters(UUID userId, CardFilter filter, int page, int size) {
    List<Card> cards = cardRepository.findByOwnerIdAndFilters(userId, filter, page, size);
    return cards.stream()
            .map(CardCommandService::toView)
            .collect(Collectors.toList());
}

    @Transactional(readOnly = true)
    public List<CardDtos.CardView> listAllWithFilters(CardFilter filter, int page, int size) {
        List<Card> cards = cardRepository.findAllWithFilters(filter, page, size);
        return cards.stream()
                .map(CardCommandService::toView)
                .collect(Collectors.toList());
    }
}
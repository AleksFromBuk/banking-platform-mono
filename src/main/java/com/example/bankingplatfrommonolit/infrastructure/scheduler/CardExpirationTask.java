package com.example.bankingplatfrommonolit.infrastructure.scheduler;

import com.example.bankingplatfrommonolit.domain.port.repo.CardRepositoryPort;
import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CardExpirationTask {
    private final CardRepositoryPort cardRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkExpiredCards() {
        LocalDate today = LocalDate.now();
        cardRepository.findByExpiryDateBefore(today).forEach(card -> {
            if (card.getStatus() == CardStatus.ACTIVE) {
                cardRepository.updateStatus(card.getId(), CardStatus.EXPIRED);
            }
        });
    }
}

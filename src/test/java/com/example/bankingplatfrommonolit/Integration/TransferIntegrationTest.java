package com.example.bankingplatfrommonolit.Integration;

import com.example.bankingplatfrommonolit.application.dto.transfer.TransferDtos;
import com.example.bankingplatfrommonolit.domain.model.Card;
import com.example.bankingplatfrommonolit.domain.port.repo.CardRepositoryPort;
import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CardRepositoryPort cardRepository;

    private UUID ownerId;
    private UUID cardId1;
    private UUID cardId2;

    @BeforeEach
    void setUp() {
        ownerId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        Card card1 = Card.builder()
                .id(UUID.randomUUID())
                .ownerId(ownerId)
                .last4("1111")
                .expiryDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .version(0L)
                .createdAt(Instant.now())
                .build();

        Card card2 = Card.builder()
                .id(UUID.randomUUID())
                .ownerId(ownerId)
                .last4("2222")
                .expiryDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("50.00"))
                .version(0L)
                .createdAt(Instant.now())
                .build();

        cardId1 = cardRepository.saveWithEncryptedPan(card1, "enc1").getId();
        cardId2 = cardRepository.saveWithEncryptedPan(card2, "enc2").getId();
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
        // userId = ownerId
    void shouldTransferBetweenOwnCards() throws Exception {
        var req = new TransferDtos.TransferRequest(cardId1, cardId2, new BigDecimal("10.00"));
        mockMvc.perform(post("/transactions/transfer")
                        .header("Idempotency-Key", "test-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}

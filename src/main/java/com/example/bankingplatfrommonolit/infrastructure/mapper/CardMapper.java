package com.example.bankingplatfrommonolit.infrastructure.mapper;

import com.example.bankingplatfrommonolit.domain.model.Card;
import com.example.bankingplatfrommonolit.infrastructure.persistence.entity.CardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target = "encryptedPan", ignore = true)
    CardEntity toEntity(Card c);

    Card toDomain(CardEntity e);
}

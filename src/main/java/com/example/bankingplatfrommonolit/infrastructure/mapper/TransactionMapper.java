package com.example.bankingplatfrommonolit.infrastructure.mapper;

import com.example.bankingplatfrommonolit.domain.model.Transaction;
import com.example.bankingplatfrommonolit.infrastructure.persistence.entity.TransactionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionEntity toEntity(Transaction t);
    Transaction toDomain(TransactionEntity e);
}

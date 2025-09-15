--liquibase formatted sql

--changeset author:create-transactions-table
CREATE TABLE transactions (
                              id UUID PRIMARY KEY,
                              card_id UUID NOT NULL,
                              type VARCHAR(20) NOT NULL,
                              amount NUMERIC(19,2) NOT NULL,
                              balance_after NUMERIC(19,2) NOT NULL,
                              description VARCHAR(255),
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_card_created ON transactions(card_id, created_at);

--rollback DROP TABLE transactions;
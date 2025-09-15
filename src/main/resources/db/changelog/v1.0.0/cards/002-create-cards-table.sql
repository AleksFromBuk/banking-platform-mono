--liquibase formatted sql

--changeset author:create-cards-table
CREATE TABLE cards (
                       id UUID PRIMARY KEY,
                       owner_id UUID NOT NULL,
                       encrypted_pan TEXT NOT NULL,
                       last4 VARCHAR(4) NOT NULL,
                       expiry_date DATE NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       balance NUMERIC(19,2) NOT NULL DEFAULT 0,
                       version BIGINT NOT NULL DEFAULT 0,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cards_owner ON cards(owner_id);
CREATE INDEX idx_cards_status ON cards(status);

--rollback DROP TABLE cards;
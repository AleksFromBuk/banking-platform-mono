--liquibase formatted sql

--changeset author:create-refresh-tokens-table
CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                user_id UUID NOT NULL,
                                token_hash VARCHAR(64) NOT NULL UNIQUE,
                                user_agent VARCHAR(200),
                                ip VARCHAR(64),
                                expires_at TIMESTAMP NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                                rotated_from BIGINT,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_refresh_tokens_user_active ON refresh_tokens(user_id, revoked, expires_at);

--rollback DROP TABLE refresh_tokens;
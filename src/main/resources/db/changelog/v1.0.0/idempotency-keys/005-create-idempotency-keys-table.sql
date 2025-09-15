--liquibase formatted sql

--changeset author:create-idempotency-keys-table
CREATE TABLE idempotency_keys (
                                  id VARCHAR(255) PRIMARY KEY,
                                  status VARCHAR(20) NOT NULL,
                                  response_json TEXT,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_idempotency_keys_created ON idempotency_keys(created_at);

--rollback DROP TABLE idempotency_keys;
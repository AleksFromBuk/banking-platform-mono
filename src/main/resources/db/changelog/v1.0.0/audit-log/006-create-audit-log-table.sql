--liquibase formatted sql

--changeset author:create-audit-log-table
CREATE TABLE audit_log (
                           id UUID PRIMARY KEY,
                           user_id UUID,
                           action VARCHAR(100) NOT NULL,
                           details TEXT,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_user_created ON audit_log(user_id, created_at);

--rollback DROP TABLE audit_log;
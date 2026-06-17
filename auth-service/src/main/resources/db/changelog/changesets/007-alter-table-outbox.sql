--liquibase formatted sql

--changeset syncline:007
ALTER TABLE outbox
ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
ADD COLUMN error_reason JSONB;

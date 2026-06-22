--liquibase formatted sql

--changeset syncline:006
CREATE TABLE outbox (
    id UUID PRIMARY KEY,
    event_type VARCHAR(512) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMPTZ
);
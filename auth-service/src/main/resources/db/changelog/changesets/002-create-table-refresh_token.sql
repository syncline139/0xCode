--liquibase formatted sql

--changeset syncline:002

CREATE TABLE refresh_token (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);
--liquibase formatted sql

--changeset syncline:004

CREATE TABLE email_verification_code
(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    code VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_code_user FOREIGN KEY (user_id) REFERENCES users(id)
);
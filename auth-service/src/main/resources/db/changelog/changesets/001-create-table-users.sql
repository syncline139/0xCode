--liquibase formatted sql

--changeset syncline:001

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(512) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
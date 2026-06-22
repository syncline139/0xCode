--liquibase formatted sql

--changeset syncline:003
ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL;
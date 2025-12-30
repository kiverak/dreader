CREATE TABLE users
(
    id               VARCHAR(64) PRIMARY KEY, -- keycloak userId
    email            VARCHAR(255),
    username         VARCHAR(255),
    telegram_account VARCHAR(255),
    deleted          BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP WITH TIME ZONE,
    updated_at       TIMESTAMP WITH TIME ZONE
);
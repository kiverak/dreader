CREATE TABLE threads_token
(
    id           BIGSERIAL PRIMARY KEY,
    access_token VARCHAR(500) NOT NULL,
    channel_id   BIGINT       NOT NULL REFERENCES channel (id),
    expires_at   TIMESTAMP    NOT NULL
);

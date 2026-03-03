CREATE TABLE threads_token
(
    id           BIGSERIAL PRIMARY KEY,
    access_token VARCHAR(500) NOT NULL,
    channel_id   BIGINT       NOT NULL UNIQUE,
    expires_at   TIMESTAMP    NOT NULL,
    CONSTRAINT fk_threads_token_channel
        FOREIGN KEY (channel_id)
            REFERENCES channel (id)
            ON DELETE CASCADE
);

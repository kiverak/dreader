-- PUBLISHING POSTS — TABLES

CREATE TABLE channel
(
    id          BIGSERIAL PRIMARY KEY,
    platform    VARCHAR(32)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    credentials JSONB        NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE post
(
    id           BIGSERIAL PRIMARY KEY,
    text         TEXT        NOT NULL,
    scheduled_at TIMESTAMP,
    status       VARCHAR(32) NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE post_media
(
    id       BIGSERIAL PRIMARY KEY,
    post_id  BIGINT      NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    type     VARCHAR(32) NOT NULL,
    url      TEXT        NOT NULL,
    position INT         NOT NULL DEFAULT 0
);

CREATE TABLE post_channel
(
    post_id    BIGINT NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    channel_id BIGINT NOT NULL REFERENCES channel (id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, channel_id)
);

CREATE TABLE publish_result
(
    id            BIGSERIAL PRIMARY KEY,
    post_id       BIGINT    NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    channel_id    BIGINT    NOT NULL REFERENCES channel (id) ON DELETE CASCADE,
    success       BOOLEAN   NOT NULL,
    external_id   VARCHAR(255),
    error_message TEXT,
    published_at  TIMESTAMP NOT NULL DEFAULT NOW()
);


-- INDEXES

CREATE INDEX IF NOT EXISTS idx_channel_platform ON channel(platform);
CREATE INDEX IF NOT EXISTS idx_channel_name ON channel(name);

CREATE INDEX IF NOT EXISTS idx_post_status ON post(status);
CREATE INDEX IF NOT EXISTS idx_post_scheduled_at ON post(scheduled_at);

CREATE INDEX IF NOT EXISTS idx_post_media_post_id ON post_media(post_id);

CREATE INDEX IF NOT EXISTS idx_publish_result_post_id ON publish_result(post_id);
CREATE INDEX IF NOT EXISTS idx_publish_result_channel_id ON publish_result(channel_id);
CREATE INDEX IF NOT EXISTS idx_publish_result_success ON publish_result(success);

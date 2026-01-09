CREATE TABLE IF NOT EXISTS tag
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version    BIGINT       NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_tag_name_hash ON tag USING hash (name);

CREATE TABLE IF NOT EXISTS source
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    url        VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version    BIGINT       NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_source_name_hash ON source USING hash (name);

CREATE TABLE IF NOT EXISTS article
(
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    views_count      INT,
    comments_count   INT,
    content          TEXT         NOT NULL,
    short_content    TEXT         NOT NULL,
    url              VARCHAR(255) NOT NULL UNIQUE,
    image_url        VARCHAR(255),
    source_id        BIGINT REFERENCES source (id),
    publication_date TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP,
    created_by       UUID,
    updated_by       UUID,
    version          BIGINT       NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_article_url_hash ON article USING hash (url);

CREATE TABLE IF NOT EXISTS article_tag
(
    article_id BIGINT NOT NULL REFERENCES article (id),
    tag_id     BIGINT NOT NULL REFERENCES tag (id),
    PRIMARY KEY (article_id, tag_id)
);

CREATE TABLE IF NOT EXISTS source_default_tags
(
    source_id BIGINT NOT NULL REFERENCES source (id),
    tag_id    BIGINT NOT NULL REFERENCES tag (id),
    PRIMARY KEY (source_id, tag_id)
);

INSERT INTO tag (name)
VALUES ('ai'),
       ('it'),
       ('coding'),
       ('programming'),
       ('technology')
ON CONFLICT (name) DO NOTHING;

INSERT INTO source (name, url)
VALUES ('Habr News', 'https://habr.com/ru/news/')
ON CONFLICT (name) DO NOTHING;

INSERT INTO source_default_tags (source_id, tag_id)
SELECT s.id, t.id
FROM source s
         JOIN tag t ON t.name IN ('ai', 'it', 'coding', 'programming', 'technology')
WHERE s.name = 'Habr News'
ON CONFLICT DO NOTHING;

INSERT INTO source (name, url)
VALUES ('3DNews', 'https://3dnews.ru/news/')
ON CONFLICT (name) DO NOTHING;

INSERT INTO source_default_tags (source_id, tag_id)
SELECT s.id, t.id
FROM source s
         JOIN tag t ON t.name IN ('ai', 'it', 'coding', 'programming', 'technology')
WHERE s.name = '3DNews'
ON CONFLICT DO NOTHING;



CREATE TABLE channel
(
    id          BIGSERIAL PRIMARY KEY,
    platform    VARCHAR(32)  NOT NULL, -- TELEGRAM, VK, X, THREADS, FACEBOOK
    name        VARCHAR(255) NOT NULL, -- "Новости", "Технологии", "Кино"
    credentials JSONB        NOT NULL, -- токены, chatId, groupId, pageId
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE post
(
    id           BIGSERIAL PRIMARY KEY,
    text         TEXT        NOT NULL,
    scheduled_at TIMESTAMP,            -- null = публиковать сразу
    status       VARCHAR(32) NOT NULL, -- PENDING, PUBLISHED, FAILED, PARTIAL
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE post_media
(
    id       BIGSERIAL PRIMARY KEY,
    post_id  BIGINT      NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    type     VARCHAR(32) NOT NULL,          -- IMAGE, VIDEO
    url      TEXT        NOT NULL,
    position INT         NOT NULL DEFAULT 0 -- порядок медиа
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
    external_id   VARCHAR(255), -- id поста в Telegram/VK/X/Threads
    error_message TEXT,
    published_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

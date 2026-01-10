-- PARSING ARTICLES — TABLES

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

CREATE INDEX IF NOT EXISTS idx_tag_name_hash
    ON tag USING hash (name);


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

CREATE INDEX IF NOT EXISTS idx_source_name_hash
    ON source USING hash (name);


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

CREATE INDEX IF NOT EXISTS idx_article_url_hash
    ON article USING hash (url);


CREATE TABLE IF NOT EXISTS article_tag
(
    article_id BIGINT NOT NULL REFERENCES article (id) ON DELETE CASCADE,
    tag_id     BIGINT NOT NULL REFERENCES tag (id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

CREATE TABLE IF NOT EXISTS source_default_tags
(
    source_id BIGINT NOT NULL REFERENCES source (id) ON DELETE CASCADE,
    tag_id    BIGINT NOT NULL REFERENCES tag (id) ON DELETE CASCADE,
    PRIMARY KEY (source_id, tag_id)
);

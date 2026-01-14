CREATE TABLE category
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version    BIGINT       NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_category_name_hash
    ON category USING hash (name);

CREATE TABLE IF NOT EXISTS processed_article
(
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    content          TEXT         NOT NULL,
    short_content    TEXT         NOT NULL,
    url              VARCHAR(255) NOT NULL UNIQUE,
    image_url        VARCHAR(255),
    source_id        BIGINT REFERENCES source (id),
    publication_date TIMESTAMP,
    llm_parsed       BOOLEAN      NOT NULL DEFAULT false,
    rate             INT          NOT NULL DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP,
    created_by       UUID,
    updated_by       UUID,
    version          BIGINT       NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_processed_article_url_hash
    ON processed_article USING hash (url);

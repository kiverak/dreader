CREATE TABLE IF NOT EXISTS tag
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_tag_name_hash ON tag USING hash (name);

CREATE TABLE IF NOT EXISTS source
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    url  VARCHAR(255) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_source_name_hash ON source USING hash (name);

CREATE TABLE IF NOT EXISTS source_default_tags
(
    source_id BIGINT NOT NULL REFERENCES source (id),
    tag_id    BIGINT NOT NULL REFERENCES tag (id),
    PRIMARY KEY (source_id, tag_id)
);

INSERT INTO tag (name)
VALUES ('ai'), ('it'), ('coding'), ('programming'), ('technology')
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
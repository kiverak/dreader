-- INIT TAGS

INSERT INTO tag (name)
VALUES ('ai'),
       ('it'),
       ('coding'),
       ('programming'),
       ('technology')
ON CONFLICT (name) DO NOTHING;


-- INIT SOURCES

INSERT INTO source (name, url)
VALUES ('Habr News', 'https://habr.com/ru/news/')
ON CONFLICT (name) DO NOTHING;

INSERT INTO source (name, url)
VALUES ('3DNews', 'https://3dnews.ru/news/')
ON CONFLICT (name) DO NOTHING;


-- INIT DEFAULT TAGS FOR SOURCES

INSERT INTO source_default_tags (source_id, tag_id)
SELECT s.id, t.id
FROM source s
         JOIN tag t ON t.name IN ('ai', 'it', 'coding', 'programming', 'technology')
WHERE s.name = 'Habr News'
ON CONFLICT DO NOTHING;

INSERT INTO source_default_tags (source_id, tag_id)
SELECT s.id, t.id
FROM source s
         JOIN tag t ON t.name IN ('ai', 'it', 'coding', 'programming', 'technology')
WHERE s.name = '3DNews'
ON CONFLICT DO NOTHING;

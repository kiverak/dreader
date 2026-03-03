INSERT INTO channel (id, platform, name, credentials, created_at, updated_at, created_by, updated_by, version)
VALUES (5, 'THREADS', 'threads-test', '{"clientId": "@dailyreadertest"}',
        DEFAULT, DEFAULT, null, null, DEFAULT);

INSERT INTO channel_category (category_id, channel_id) VALUES (1, 5);
INSERT INTO channel_category (category_id, channel_id) VALUES (3, 5);
CREATE TABLE channel_category
(
    category_id BIGINT NOT NULL REFERENCES category (id) ON DELETE CASCADE,
    channel_id  BIGINT NOT NULL REFERENCES channel (id) ON DELETE CASCADE,
    CONSTRAINT pk_channel_category PRIMARY KEY (category_id, channel_id)
);

CREATE TABLE post_category
(
    category_id BIGINT NOT NULL REFERENCES category (id) ON DELETE CASCADE,
    post_id     BIGINT NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    CONSTRAINT pk_post_category PRIMARY KEY (category_id, post_id)
);

CREATE TABLE processed_article_category
(
    category_id     BIGINT NOT NULL REFERENCES category (id) ON DELETE CASCADE,
    processed_article_id BIGINT NOT NULL REFERENCES processed_article (id) ON DELETE CASCADE,
    CONSTRAINT pk_processed_article_category PRIMARY KEY (category_id, processed_article_id)
);


-- INSERT
INSERT INTO channel_category (category_id, channel_id)
VALUES (1, 1),
       (3, 1),
       (4, 2)
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

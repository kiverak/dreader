-- Таблица тегов (категории, регионы)
CREATE TABLE tags (
                      id BIGSERIAL PRIMARY KEY,
                      version BIGINT DEFAULT 0 NOT NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      name VARCHAR(255) NOT NULL UNIQUE,
                      type VARCHAR(50) NOT NULL
);

-- Таблица новостей
CREATE TABLE news (
                      id BIGSERIAL PRIMARY KEY,
                      version BIGINT DEFAULT 0 NOT NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      created_by VARCHAR(255),
                      updated_by VARCHAR(255),
                      title VARCHAR(500) NOT NULL,
                      content TEXT,
                      published_at TIMESTAMP
);

-- Связь многие-ко-многим: новости ↔ теги
CREATE TABLE news_tags (
                           news_id BIGINT NOT NULL,
                           tag_id BIGINT NOT NULL,
                           PRIMARY KEY (news_id, tag_id),
                           CONSTRAINT fk_news FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE,
                           CONSTRAINT fk_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Таблица изображений
CREATE TABLE images (
                        id BIGSERIAL PRIMARY KEY,
                        version BIGINT DEFAULT 0 NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        news_id BIGINT NOT NULL,
                        url VARCHAR(1000) NOT NULL,
                        checksum VARCHAR(255),
                        CONSTRAINT fk_news_image FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE
);

-- Таблица комментариев
CREATE TABLE comments (
                          id BIGSERIAL PRIMARY KEY,
                          version BIGINT DEFAULT 0 NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          created_by VARCHAR(255),
                          updated_by VARCHAR(255),
                          news_id BIGINT NOT NULL,
                          parent_id BIGINT,
                          content TEXT NOT NULL,
                          CONSTRAINT fk_news_comment FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE,
                          CONSTRAINT fk_parent_comment FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);

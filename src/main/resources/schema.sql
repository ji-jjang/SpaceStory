CREATE TABLE spaces
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    content     TEXT         NOT NULL,
    view_count  INT DEFAULT 0,
    is_pinned   BOOLEAN,
    created_by  VARCHAR(255) NOT NULL,
    created_at  DATETIME,
    category_id BIGINT,
    user_id     BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (category_id) REFERENCES announcement_categories (id)
);

CREATE TABLE free_categories
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

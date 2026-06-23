-- review-service schema (review_db)
CREATE TABLE reviews (
    id            UUID         PRIMARY KEY,
    order_id      UUID         NOT NULL UNIQUE,
    user_id       UUID         NOT NULL,
    restaurant_id UUID         NOT NULL,
    driver_id     UUID,
    rating        INTEGER      NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment       VARCHAR(2000),
    created_at    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_reviews_restaurant_id ON reviews (restaurant_id);
CREATE INDEX idx_reviews_driver_id     ON reviews (driver_id);
CREATE INDEX idx_reviews_user_id       ON reviews (user_id);

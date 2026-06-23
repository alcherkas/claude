-- menu-service initial schema (database: menu_db)

CREATE TABLE menu_items (
    id            UUID         PRIMARY KEY,
    restaurant_id UUID         NOT NULL,
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    price_cents   BIGINT       NOT NULL CHECK (price_cents >= 0),
    currency      CHAR(3)      NOT NULL,
    category      VARCHAR(100) NOT NULL,
    available     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_menu_items_restaurant_id ON menu_items (restaurant_id);
CREATE INDEX idx_menu_items_restaurant_category ON menu_items (restaurant_id, category);

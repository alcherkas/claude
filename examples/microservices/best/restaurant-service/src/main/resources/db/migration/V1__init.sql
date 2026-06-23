CREATE TABLE restaurants (
    id            UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    name          VARCHAR(255) NOT NULL,
    cuisine       VARCHAR(128) NOT NULL,
    address_line  VARCHAR(512) NOT NULL,
    city          VARCHAR(128) NOT NULL,
    lat           DOUBLE PRECISION NOT NULL,
    lng           DOUBLE PRECISION NOT NULL,
    status        VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    opening_hours VARCHAR(512),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_restaurant_status CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED'))
);

CREATE INDEX idx_restaurants_owner_user_id ON restaurants (owner_user_id);
CREATE INDEX idx_restaurants_cuisine ON restaurants (LOWER(cuisine));
CREATE INDEX idx_restaurants_city ON restaurants (LOWER(city));

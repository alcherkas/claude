-- delivery-service initial schema (database: delivery_db)

CREATE TABLE deliveries (
    id          UUID         PRIMARY KEY,
    order_id    UUID         NOT NULL UNIQUE,
    user_id     UUID,
    driver_id   UUID,
    status      VARCHAR(24)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_deliveries_status CHECK (status IN
        ('PENDING','ASSIGNED','EN_ROUTE_TO_PICKUP','PICKED_UP',
         'EN_ROUTE_TO_CUSTOMER','DELIVERED','FAILED'))
);

CREATE INDEX idx_deliveries_order_id ON deliveries (order_id);
CREATE INDEX idx_deliveries_driver_id ON deliveries (driver_id);

CREATE TABLE tracking_points (
    id           UUID         PRIMARY KEY,
    delivery_id  UUID         NOT NULL REFERENCES deliveries (id) ON DELETE CASCADE,
    lat          DOUBLE PRECISION NOT NULL,
    lng          DOUBLE PRECISION NOT NULL,
    at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_tracking_points_delivery_id ON tracking_points (delivery_id);

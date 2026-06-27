-- order-service initial schema (database: order_db)

CREATE TABLE orders (
    id                UUID            PRIMARY KEY,
    user_id           UUID            NOT NULL,
    restaurant_id     UUID            NOT NULL,
    status            VARCHAR(20)     NOT NULL,
    subtotal_cents    BIGINT          NOT NULL,
    delivery_fee_cents BIGINT         NOT NULL,
    service_fee_cents BIGINT          NOT NULL,
    tax_cents         BIGINT          NOT NULL,
    discount_cents    BIGINT          NOT NULL,
    tip_cents         BIGINT          NOT NULL DEFAULT 0,
    total_cents       BIGINT          NOT NULL,
    currency          VARCHAR(3)      NOT NULL,
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT chk_orders_status CHECK (status IN
        ('CREATED','CONFIRMED','PREPARING','READY','PICKED_UP','DELIVERED','CANCELLED'))
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_restaurant_id ON orders (restaurant_id);

CREATE TABLE order_items (
    id              UUID        PRIMARY KEY,
    order_id        UUID        NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    menu_item_id    UUID        NOT NULL,
    name            VARCHAR(255) NOT NULL,
    qty             INTEGER     NOT NULL,
    unit_price_cents BIGINT     NOT NULL,
    item_position   INTEGER     NOT NULL,
    CONSTRAINT chk_order_items_qty CHECK (qty > 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);

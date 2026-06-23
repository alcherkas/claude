-- payment-service schema (payment_db)
-- Mirrors com.quickbite.payment.domain.Payment

CREATE TABLE payments (
    id           UUID         PRIMARY KEY,
    order_id     UUID         NOT NULL,
    user_id      UUID         NOT NULL,
    amount_cents BIGINT       NOT NULL,
    currency     VARCHAR(3)   NOT NULL,
    method       VARCHAR(16)  NOT NULL,
    status       VARCHAR(16)  NOT NULL,
    provider     VARCHAR(64)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    CONSTRAINT chk_payments_method CHECK (method IN ('CARD', 'WALLET')),
    CONSTRAINT chk_payments_status CHECK (status IN ('AUTHORIZED', 'CAPTURED', 'REFUNDED', 'FAILED')),
    CONSTRAINT chk_payments_amount CHECK (amount_cents >= 0)
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_user_id ON payments (user_id);

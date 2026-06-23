-- promotion-service schema (promotion_db)

CREATE TABLE promotions (
    id                 BIGINT       GENERATED ALWAYS AS IDENTITY,
    code               VARCHAR(64)  NOT NULL,
    type               VARCHAR(16)  NOT NULL,
    value              BIGINT       NOT NULL,
    min_subtotal_cents BIGINT       NOT NULL DEFAULT 0,
    valid_from         TIMESTAMPTZ  NOT NULL,
    valid_to           TIMESTAMPTZ  NOT NULL,
    max_redemptions    INTEGER,
    per_user_limit     INTEGER,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_promotions PRIMARY KEY (id),
    CONSTRAINT uq_promotions_code UNIQUE (code),
    CONSTRAINT ck_promotions_type CHECK (type IN ('PERCENT', 'FIXED', 'FREE_DELIVERY'))
);

CREATE TABLE redemptions (
    id           BIGINT       GENERATED ALWAYS AS IDENTITY,
    promotion_id BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    order_id     BIGINT       NOT NULL,
    redeemed_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_redemptions PRIMARY KEY (id),
    CONSTRAINT fk_redemptions_promotion FOREIGN KEY (promotion_id) REFERENCES promotions (id),
    CONSTRAINT uq_redemptions_promotion_order UNIQUE (promotion_id, order_id)
);

CREATE INDEX idx_redemptions_promotion_user ON redemptions (promotion_id, user_id);

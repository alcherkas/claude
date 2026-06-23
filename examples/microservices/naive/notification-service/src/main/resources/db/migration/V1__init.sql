-- notification-service initial schema.
-- One row per notification rendered from an inbound order/payment/delivery event.

CREATE TABLE notification (
    id           UUID         NOT NULL,
    user_id      UUID         NOT NULL,
    channel      VARCHAR(16)  NOT NULL,
    template     VARCHAR(64)  NOT NULL,
    payload_json TEXT         NOT NULL,
    status       VARCHAR(16)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at      TIMESTAMPTZ,
    CONSTRAINT pk_notification PRIMARY KEY (id),
    CONSTRAINT chk_notification_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
    CONSTRAINT chk_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX idx_notification_user ON notification (user_id);
CREATE INDEX idx_notification_user_created ON notification (user_id, created_at DESC);
CREATE INDEX idx_notification_status ON notification (status);

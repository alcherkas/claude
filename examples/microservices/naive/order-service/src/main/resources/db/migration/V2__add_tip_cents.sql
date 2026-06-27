-- Optional courier tip captured at checkout, added on top of the order total.
-- Existing orders predate tipping, so default to 0.
ALTER TABLE orders ADD COLUMN tip_cents BIGINT NOT NULL DEFAULT 0;

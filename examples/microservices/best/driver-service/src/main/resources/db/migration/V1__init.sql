-- driver-service schema (driver_db). Matches com.quickbite.driver.domain.Driver.
CREATE TABLE drivers (
    id          UUID            PRIMARY KEY,
    user_id     UUID            NOT NULL UNIQUE,
    name        VARCHAR(255)    NOT NULL,
    vehicle     VARCHAR(16)     NOT NULL,
    status      VARCHAR(16)     NOT NULL DEFAULT 'OFFLINE',
    lat         DOUBLE PRECISION,
    lng         DOUBLE PRECISION,
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT chk_drivers_vehicle CHECK (vehicle IN ('BIKE', 'CAR', 'SCOOTER')),
    CONSTRAINT chk_drivers_status  CHECK (status IN ('OFFLINE', 'AVAILABLE', 'ON_DELIVERY'))
);

-- Dispatch reads filter by status and order by distance from a pickup point.
CREATE INDEX idx_drivers_status      ON drivers (status);
CREATE INDEX idx_drivers_status_geo  ON drivers (status, lat, lng);

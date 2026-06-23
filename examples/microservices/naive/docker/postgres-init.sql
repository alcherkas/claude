-- QuickBite local Postgres bootstrap.
-- Creates one database per stateful service (14 *_db databases).
-- pricing-service and api-gateway are stateless and have no database.
-- Run automatically by the postgres container on first boot
-- (mounted at /docker-entrypoint-initdb.d). Owned by the `quickbite` role.

CREATE DATABASE identity_db      OWNER quickbite;
CREATE DATABASE restaurant_db    OWNER quickbite;
CREATE DATABASE menu_db          OWNER quickbite;
CREATE DATABASE search_db        OWNER quickbite;
CREATE DATABASE cart_db          OWNER quickbite;
CREATE DATABASE promotion_db     OWNER quickbite;
CREATE DATABASE order_db         OWNER quickbite;
CREATE DATABASE wallet_db        OWNER quickbite;
CREATE DATABASE payment_db       OWNER quickbite;
CREATE DATABASE driver_db        OWNER quickbite;
CREATE DATABASE delivery_db      OWNER quickbite;
CREATE DATABASE notification_db  OWNER quickbite;
CREATE DATABASE review_db        OWNER quickbite;

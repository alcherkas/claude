-- search-service initial schema.
-- Denormalized read model populated from restaurant.events + menu.events.
-- The example uses Postgres ILIKE / trigram matching; production would use OpenSearch.

CREATE TABLE search_doc (
    id            UUID         NOT NULL,
    type          VARCHAR(16)  NOT NULL,
    ref_id        UUID         NOT NULL,
    restaurant_id UUID         NOT NULL,
    name          VARCHAR(256) NOT NULL,
    cuisine       VARCHAR(128),
    price_cents   BIGINT,
    lat           DOUBLE PRECISION,
    lng           DOUBLE PRECISION,
    available     BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_search_doc PRIMARY KEY (id),
    CONSTRAINT uk_search_doc_type_ref UNIQUE (type, ref_id),
    CONSTRAINT chk_search_doc_type CHECK (type IN ('RESTAURANT', 'MENU_ITEM'))
);

CREATE INDEX idx_search_doc_type ON search_doc (type);
CREATE INDEX idx_search_doc_restaurant ON search_doc (restaurant_id);
CREATE INDEX idx_search_doc_cuisine ON search_doc (cuisine);

-- Case-insensitive substring search acceleration for the local ILIKE queries.
-- (pg_trgm ships with the standard Postgres image used in docker-compose.)
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_search_doc_name_trgm ON search_doc USING gin (name gin_trgm_ops);
CREATE INDEX idx_search_doc_cuisine_trgm ON search_doc USING gin (cuisine gin_trgm_ops);

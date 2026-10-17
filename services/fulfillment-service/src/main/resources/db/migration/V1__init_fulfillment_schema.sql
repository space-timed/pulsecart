-- PulseCart Fulfillment Service Initial Schema
CREATE SCHEMA IF NOT EXISTS pulsecart_fulfillment;

CREATE TABLE IF NOT EXISTS pulsecart_fulfillment.fulfillments (
    id VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL UNIQUE,
    customer_id VARCHAR(100) NOT NULL,
    warehouse_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL, -- PENDING, ALLOCATED, PICKED, PACKED, FULFILLED, CANCELLED
    shipping_address TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pulsecart_fulfillment.fulfillment_items (
    id BIGSERIAL PRIMARY KEY,
    fulfillment_id VARCHAR(100) NOT NULL REFERENCES pulsecart_fulfillment.fulfillments(id),
    product_id VARCHAR(100) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL
);

CREATE TABLE IF NOT EXISTS pulsecart_fulfillment.outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_fulfillment_order ON pulsecart_fulfillment.fulfillments(order_id);
CREATE INDEX IF NOT EXISTS idx_fulfillment_warehouse ON pulsecart_fulfillment.fulfillments(warehouse_id);

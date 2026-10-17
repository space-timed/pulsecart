-- PulseCart Shipping Service Initial Schema
CREATE SCHEMA IF NOT EXISTS pulsecart_shipping;

CREATE TABLE IF NOT EXISTS pulsecart_shipping.shipments (
    id VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL UNIQUE,
    customer_id VARCHAR(100) NOT NULL,
    carrier VARCHAR(30) NOT NULL, -- FEDEX, UPS, DHL
    tracking_number VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL, -- PENDING, DISPATCHED, IN_TRANSIT, DELIVERED, FAILED
    destination_address TEXT,
    estimated_delivery_at TIMESTAMPTZ NOT NULL,
    dispatched_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pulsecart_shipping.outbox_events (
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

CREATE INDEX IF NOT EXISTS idx_shipment_order ON pulsecart_shipping.shipments(order_id);
CREATE INDEX IF NOT EXISTS idx_shipment_tracking ON pulsecart_shipping.shipments(tracking_number);

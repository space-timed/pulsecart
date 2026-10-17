-- PulseCart Order Service Initial Schema
CREATE SCHEMA IF NOT EXISTS pulsecart_order;

CREATE TABLE IF NOT EXISTS pulsecart_order.orders (
    id VARCHAR(100) PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL,
    status VARCHAR(40) NOT NULL, -- PENDING, INVENTORY_RESERVED, CONFIRMED, COMPENSATING_INVENTORY, CANCELLED
    subtotal NUMERIC(12, 2) NOT NULL,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    reservation_id VARCHAR(100),
    payment_id VARCHAR(100),
    cancellation_reason VARCHAR(255),
    shipping_address TEXT,
    idempotency_key VARCHAR(128) UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pulsecart_order.order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL REFERENCES pulsecart_order.orders(id),
    product_id VARCHAR(100) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    quantity INT NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS pulsecart_order.saga_instances (
    id VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL REFERENCES pulsecart_order.orders(id),
    current_step VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL, -- IN_PROGRESS, SUCCEEDED, COMPENSATING, FAILED
    compensation_reason VARCHAR(255),
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS pulsecart_order.saga_step_logs (
    id BIGSERIAL PRIMARY KEY,
    saga_id VARCHAR(100) NOT NULL REFERENCES pulsecart_order.saga_instances(id),
    step_name VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL, -- PENDING, SUCCESS, FAILED, COMPENSATED
    payload TEXT,
    error_message VARCHAR(255),
    executed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pulsecart_order.outbox_events (
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

CREATE INDEX IF NOT EXISTS idx_order_customer ON pulsecart_order.orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_idempotency ON pulsecart_order.orders(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_order_outbox_unpub ON pulsecart_order.outbox_events(published, created_at);

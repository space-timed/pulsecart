CREATE SCHEMA IF NOT EXISTS pulsecart_returns;

CREATE TABLE IF NOT EXISTS pulsecart_returns.returns (
    id VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    refund_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_returns_order_id ON pulsecart_returns.returns(order_id);
CREATE INDEX IF NOT EXISTS idx_returns_customer_id ON pulsecart_returns.returns(customer_id);

CREATE TABLE IF NOT EXISTS pulsecart_returns.return_items (
    id BIGSERIAL PRIMARY KEY,
    return_id VARCHAR(100) NOT NULL REFERENCES pulsecart_returns.returns(id) ON DELETE CASCADE,
    product_id VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    condition VARCHAR(50) DEFAULT 'UNOPENED',
    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS pulsecart_returns.returns_outbox_events (
    id VARCHAR(100) PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_returns_outbox_published ON pulsecart_returns.returns_outbox_events(published);

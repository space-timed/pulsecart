-- PulseCart Payment Service Initial Schema
CREATE SCHEMA IF NOT EXISTS pulsecart_payment;

CREATE TABLE IF NOT EXISTS pulsecart_payment.payments (
    id VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    status VARCHAR(30) NOT NULL, -- PENDING, AUTHORIZED, CAPTURED, FAILED, REFUNDED
    provider VARCHAR(30) NOT NULL, -- STRIPE, PAYPAL, CHAOS_MOCK
    authorization_code VARCHAR(100),
    failure_reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pulsecart_payment.payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(100) NOT NULL REFERENCES pulsecart_payment.payments(id),
    type VARCHAR(30) NOT NULL, -- AUTHORIZE, CAPTURE, REFUND
    amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL, -- SUCCESS, FAILED
    gateway_reference VARCHAR(100),
    error_message VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pulsecart_payment.payment_idempotency_keys (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    payment_id VARCHAR(100),
    request_hash VARCHAR(64) NOT NULL,
    response_body TEXT,
    status VARCHAR(30) NOT NULL, -- PROCESSING, COMPLETED, FAILED
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payment_order_id ON pulsecart_payment.payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_customer_id ON pulsecart_payment.payments(customer_id);

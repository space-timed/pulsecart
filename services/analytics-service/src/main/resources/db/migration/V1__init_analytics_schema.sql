CREATE SCHEMA IF NOT EXISTS pulsecart_analytics;

CREATE TABLE IF NOT EXISTS pulsecart_analytics.hourly_sales_aggregates (
    hour_bucket VARCHAR(30) PRIMARY KEY,
    total_orders INT NOT NULL DEFAULT 0,
    total_revenue NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_items_sold INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS pulsecart_analytics.product_sales_velocity (
    product_id VARCHAR(100) PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    units_sold INT NOT NULL DEFAULT 0,
    total_revenue NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    velocity_score NUMERIC(8, 2) NOT NULL DEFAULT 0.00,
    last_order_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_velocity_units ON pulsecart_analytics.product_sales_velocity(units_sold);

CREATE SCHEMA IF NOT EXISTS pulsecart_search;

CREATE TABLE IF NOT EXISTS pulsecart_search.product_search_index (
    product_id VARCHAR(100) PRIMARY KEY,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id VARCHAR(100),
    category_name VARCHAR(100),
    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    in_stock BOOLEAN NOT NULL DEFAULT TRUE,
    stock_quantity INT NOT NULL DEFAULT 0,
    tags VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_search_category ON pulsecart_search.product_search_index(category_name);
CREATE INDEX IF NOT EXISTS idx_search_price ON pulsecart_search.product_search_index(price);
CREATE INDEX IF NOT EXISTS idx_search_in_stock ON pulsecart_search.product_search_index(in_stock);
CREATE INDEX IF NOT EXISTS idx_search_name ON pulsecart_search.product_search_index(name);

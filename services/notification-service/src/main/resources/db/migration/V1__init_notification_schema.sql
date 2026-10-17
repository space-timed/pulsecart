-- PulseCart Notification Service Initial Schema
CREATE SCHEMA IF NOT EXISTS pulsecart_notification;

CREATE TABLE IF NOT EXISTS pulsecart_notification.notifications (
    id VARCHAR(100) PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    channel VARCHAR(20) NOT NULL, -- EMAIL, SMS
    template_name VARCHAR(100) NOT NULL,
    subject VARCHAR(255),
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL, -- SENT, FAILED, DLQ
    order_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pulsecart_notification.dlq_messages (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(100) NOT NULL,
    partition_id INT NOT NULL DEFAULT 0,
    offset_val BIGINT NOT NULL DEFAULT 0,
    payload TEXT NOT NULL,
    exception_message TEXT,
    replayed BOOLEAN NOT NULL DEFAULT FALSE,
    failed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_recipient ON pulsecart_notification.notifications(recipient);
CREATE INDEX IF NOT EXISTS idx_notification_order ON pulsecart_notification.notifications(order_id);
CREATE INDEX IF NOT EXISTS idx_dlq_topic ON pulsecart_notification.dlq_messages(topic);

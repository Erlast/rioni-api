CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    executed_at TIMESTAMP,
    operation VARCHAR(255) NOT NULL,
    cb VARCHAR(50),
    quantity DECIMAL(18, 8),
    amount DECIMAL(18, 2) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1
);

CREATE INDEX idx_orders_account_id ON orders(account_id);

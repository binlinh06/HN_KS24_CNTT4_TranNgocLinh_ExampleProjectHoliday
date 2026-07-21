-- V7__enhance_addresses_orders_and_payments.sql
-- Step 1: Alter order_items table for product_id nullability and snapshot support
ALTER TABLE order_items MODIFY COLUMN product_id CHAR(36) NULL;
ALTER TABLE order_items DROP FOREIGN KEY fk_order_items_product;

ALTER TABLE order_items
ADD COLUMN product_name_snapshot VARCHAR(255) NULL,
ADD COLUMN base_price_snapshot DECIMAL(15,2) NULL,
ADD COLUMN options_price_snapshot DECIMAL(15,2) NULL,
ADD COLUMN unit_price_snapshot DECIMAL(15,2) NULL,
ADD COLUMN line_total DECIMAL(15,2) NULL,
ADD COLUMN special_note VARCHAR(150) NULL;

-- Backfill order_items snapshots from products
UPDATE order_items oi
JOIN products p ON oi.product_id = p.id
SET oi.product_name_snapshot = p.product_name,
    oi.base_price_snapshot = p.base_price,
    oi.options_price_snapshot = 0.00,
    oi.unit_price_snapshot = p.base_price,
    oi.line_total = p.base_price * oi.quantity;

-- For items whose products might not exist anymore, backfill defaults
UPDATE order_items
SET product_name_snapshot = 'Món ăn đã bị xóa',
    base_price_snapshot = price_at_order,
    options_price_snapshot = 0.00,
    unit_price_snapshot = price_at_order,
    line_total = price_at_order * quantity
WHERE product_name_snapshot IS NULL;

-- Modify snapshot columns to be NOT NULL
ALTER TABLE order_items
MODIFY COLUMN product_name_snapshot VARCHAR(255) NOT NULL,
MODIFY COLUMN base_price_snapshot DECIMAL(15,2) NOT NULL,
MODIFY COLUMN options_price_snapshot DECIMAL(15,2) NOT NULL,
MODIFY COLUMN unit_price_snapshot DECIMAL(15,2) NOT NULL,
MODIFY COLUMN line_total DECIMAL(15,2) NOT NULL;

-- Recreate foreign key with ON DELETE SET NULL
ALTER TABLE order_items
ADD CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE SET NULL;


-- Step 2: Alter order_item_options table to preserve data and support snapshots
ALTER TABLE order_item_options ADD COLUMN id CHAR(36) NULL;

-- Backfill UUIDs for existing order_item_options rows.
-- In standard MySQL, UPDATE with UUID() assigns a different UUID to each row.
UPDATE order_item_options SET id = UUID() WHERE id IS NULL;

ALTER TABLE order_item_options DROP FOREIGN KEY fk_oio_item;
ALTER TABLE order_item_options DROP FOREIGN KEY fk_oio_option;
ALTER TABLE order_item_options DROP PRIMARY KEY;

ALTER TABLE order_item_options MODIFY COLUMN id CHAR(36) NOT NULL;
ALTER TABLE order_item_options ADD PRIMARY KEY (id);

ALTER TABLE order_item_options MODIFY COLUMN option_id CHAR(36) NULL;

ALTER TABLE order_item_options
ADD COLUMN option_group_name_snapshot VARCHAR(100) NULL,
ADD COLUMN option_name_snapshot VARCHAR(100) NULL,
ADD COLUMN incremental_price_snapshot DECIMAL(15,2) NOT NULL DEFAULT 0.00;

-- Backfill snapshots from product_options and product_option_groups
UPDATE order_item_options oio
JOIN product_options po ON oio.option_id = po.id
JOIN option_groups og ON po.group_id = og.id
SET oio.option_group_name_snapshot = og.group_name,
    oio.option_name_snapshot = po.option_name,
    oio.incremental_price_snapshot = po.incremental_price;

-- Set defaults for option records that couldn't be matched
UPDATE order_item_options
SET option_group_name_snapshot = 'Nhóm tùy chọn',
    option_name_snapshot = 'Tùy chọn đã xóa',
    incremental_price_snapshot = 0.00
WHERE option_group_name_snapshot IS NULL;

ALTER TABLE order_item_options
MODIFY COLUMN option_group_name_snapshot VARCHAR(100) NOT NULL,
MODIFY COLUMN option_name_snapshot VARCHAR(100) NOT NULL;

-- Recreate foreign keys
ALTER TABLE order_item_options
ADD CONSTRAINT fk_oio_item FOREIGN KEY (order_item_id) REFERENCES order_items (id) ON DELETE CASCADE,
ADD CONSTRAINT fk_oio_option FOREIGN KEY (option_id) REFERENCES product_options (id) ON DELETE SET NULL;


-- Step 3: Alter orders table for snapshot, order_code, payment_method, shipping_fee
ALTER TABLE orders
ADD COLUMN order_code VARCHAR(50) NULL,
ADD COLUMN payment_method VARCHAR(50) NULL,
ADD COLUMN shipping_address_snapshot TEXT NULL,
ADD COLUMN receiver_name_snapshot VARCHAR(100) NULL,
ADD COLUMN receiver_phone_snapshot VARCHAR(20) NULL,
ADD COLUMN voucher_code_snapshot VARCHAR(50) NULL,
ADD COLUMN shipping_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00,
ADD COLUMN canceled_at DATETIME NULL;

-- Backfill order codes with unique values (e.g. PB-<first 8 characters of UUID>)
UPDATE orders SET order_code = CONCAT('PB-', SUBSTRING(UUID(), 1, 8)) WHERE order_code IS NULL;
UPDATE orders SET payment_method = 'COD' WHERE payment_method IS NULL;
UPDATE orders SET shipping_address_snapshot = shipping_address WHERE shipping_address_snapshot IS NULL;

-- Modify to NOT NULL
ALTER TABLE orders MODIFY COLUMN order_code VARCHAR(50) NOT NULL;
ALTER TABLE orders MODIFY COLUMN payment_method VARCHAR(50) NOT NULL;

ALTER TABLE orders ADD CONSTRAINT uk_orders_code UNIQUE (order_code);


-- Step 4: Alter payments table for provider, provider_transaction_id, failure details
ALTER TABLE payments
ADD COLUMN provider VARCHAR(50) NULL,
ADD COLUMN provider_transaction_id VARCHAR(255) NULL,
ADD COLUMN idempotency_key CHAR(36) NULL,
ADD COLUMN failure_code VARCHAR(50) NULL,
ADD COLUMN failure_message VARCHAR(255) NULL;

-- Backfill payments
UPDATE payments SET provider = 'MOCK' WHERE provider IS NULL;
UPDATE payments SET provider_transaction_id = transaction_id WHERE provider_transaction_id IS NULL;

-- Modify provider to NOT NULL, drop transaction_id column, add unique constraint
ALTER TABLE payments MODIFY COLUMN provider VARCHAR(50) NOT NULL;
ALTER TABLE payments DROP COLUMN transaction_id;
ALTER TABLE payments ADD CONSTRAINT uk_payments_provider_tx UNIQUE (provider_transaction_id);


-- Step 5: Index creation
CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_orders_created_at ON orders (created_at);

CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_payment_status ON payments (payment_status);

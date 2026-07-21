-- V8__add_order_tracking_and_reviews.sql

-- 1. Create review_migration_conflicts table (all cloned columns are nullable)
CREATE TABLE review_migration_conflicts (
    id CHAR(36) NOT NULL PRIMARY KEY,
    original_review_id CHAR(36) NULL,
    order_id CHAR(36) NULL,
    customer_id CHAR(36) NULL,
    rating INT NULL,
    comment TEXT NULL,
    moderation_status VARCHAR(50) NULL,
    created_at DATETIME NULL,
    conflict_reason VARCHAR(255) NOT NULL,
    archived_at DATETIME NOT NULL,
    CONSTRAINT uq_review_migration_conflict UNIQUE (original_review_id, conflict_reason)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Create order_status_history table
CREATE TABLE order_status_history (
    id CHAR(36) NOT NULL PRIMARY KEY,
    order_id CHAR(36) NOT NULL,
    previous_status VARCHAR(50) NULL,
    new_status VARCHAR(50) NOT NULL,
    changed_by_user_id CHAR(36) NULL,
    changed_by_role VARCHAR(50) NULL,
    change_source VARCHAR(50) NOT NULL,
    reason VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_osh_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_osh_user FOREIGN KEY (changed_by_user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Add status_updated_at and version to orders table
ALTER TABLE orders ADD COLUMN status_updated_at DATETIME NULL;
ALTER TABLE orders ADD COLUMN version BIGINT NOT NULL DEFAULT 0;


-- 4. Transactional DML data cleanup procedure
DROP PROCEDURE IF EXISTS migrate_reviews_and_orders;

DELIMITER //

CREATE PROCEDURE migrate_reviews_and_orders()
BEGIN
    DECLARE expected_duplicates INT DEFAULT 0;
    DECLARE archived_duplicates INT DEFAULT 0;

    -- Start DML transaction
    START TRANSACTION;

    -- A. Archive invalid rating
    INSERT INTO review_migration_conflicts (id, original_review_id, order_id, customer_id, rating, comment, moderation_status, created_at, conflict_reason, archived_at)
    SELECT UUID(), id, order_id, customer_id, rating, comment, moderation_status, created_at, 'INVALID_RATING', NOW()
    FROM reviews
    WHERE rating < 1 OR rating > 5 OR rating IS NULL;

    -- B. Archive invalid moderation_status
    INSERT INTO review_migration_conflicts (id, original_review_id, order_id, customer_id, rating, comment, moderation_status, created_at, conflict_reason, archived_at)
    SELECT UUID(), id, order_id, customer_id, rating, comment, moderation_status, created_at, 'INVALID_MODERATION_STATUS', NOW()
    FROM reviews
    WHERE moderation_status IS NULL OR moderation_status NOT IN ('PENDING', 'APPROVED', 'REJECTED');

    -- C. Archive null order_id
    INSERT INTO review_migration_conflicts (id, original_review_id, order_id, customer_id, rating, comment, moderation_status, created_at, conflict_reason, archived_at)
    SELECT UUID(), id, order_id, customer_id, rating, comment, moderation_status, created_at, 'NULL_ORDER_ID', NOW()
    FROM reviews
    WHERE order_id IS NULL;

    -- D. Identify duplicates using a TEMPORARY TABLE
    DROP TEMPORARY TABLE IF EXISTS temp_duplicate_reviews;
    CREATE TEMPORARY TABLE temp_duplicate_reviews (
        id CHAR(36) NOT NULL,
        order_id CHAR(36) NULL,
        customer_id CHAR(36) NULL,
        rating INT NULL,
        comment TEXT NULL,
        moderation_status VARCHAR(50) NULL,
        created_at DATETIME NULL
    );

    INSERT INTO temp_duplicate_reviews (id, order_id, customer_id, rating, comment, moderation_status, created_at)
    SELECT id, order_id, customer_id, rating, comment, moderation_status, created_at
    FROM (
        SELECT id, order_id, customer_id, rating, comment, moderation_status, created_at,
               ROW_NUMBER() OVER (PARTITION BY order_id ORDER BY created_at DESC, id DESC) as rn
        FROM reviews
        WHERE order_id IS NOT NULL
    ) t
    WHERE t.rn > 1;

    -- Get expected duplicates count
    SELECT COUNT(*) INTO expected_duplicates FROM temp_duplicate_reviews;

    -- E. Archive duplicate order_id
    INSERT INTO review_migration_conflicts (id, original_review_id, order_id, customer_id, rating, comment, moderation_status, created_at, conflict_reason, archived_at)
    SELECT UUID(), id, order_id, customer_id, rating, comment, moderation_status, created_at, 'DUPLICATE_ORDER_ID', NOW()
    FROM temp_duplicate_reviews;

    -- Get archived duplicates count
    SELECT COUNT(*) INTO archived_duplicates
    FROM review_migration_conflicts
    WHERE conflict_reason = 'DUPLICATE_ORDER_ID';

    -- F. Check count matches
    IF expected_duplicates <> archived_duplicates THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Migration failed: Archival count does not match duplicate count.';
    END IF;

    -- G. Delete duplicates from reviews table
    DELETE FROM reviews
    WHERE id IN (SELECT id FROM temp_duplicate_reviews);

    -- H. Delete null order_id from reviews table
    DELETE FROM reviews
    WHERE order_id IS NULL;

    -- I. Standardize rating and moderation_status
    UPDATE reviews
    SET rating = CASE
        WHEN rating IS NULL OR rating < 1 THEN 1
        WHEN rating > 5 THEN 5
        ELSE rating
    END,
    moderation_status = CASE
        WHEN moderation_status IS NULL OR moderation_status NOT IN ('PENDING', 'APPROVED', 'REJECTED') THEN 'PENDING'
        ELSE moderation_status
    END
    WHERE rating < 1 OR rating > 5 OR rating IS NULL OR moderation_status IS NULL OR moderation_status NOT IN ('PENDING', 'APPROVED', 'REJECTED');

    -- J. Backfill status_updated_at for orders
    UPDATE orders
    SET status_updated_at = COALESCE(updated_at, created_at, NOW())
    WHERE status_updated_at IS NULL;

    -- K. Backfill order status history
    INSERT INTO order_status_history (id, order_id, previous_status, new_status, change_source, created_at)
    SELECT UUID(), id, NULL, status, 'MIGRATION', COALESCE(updated_at, created_at, NOW())
    FROM orders o
    WHERE NOT EXISTS (
        SELECT 1 FROM order_status_history h WHERE h.order_id = o.id
    );

    -- Commit DML changes
    COMMIT;

    -- Clean up temp table
    DROP TEMPORARY TABLE IF EXISTS temp_duplicate_reviews;
END //

DELIMITER ;

CALL migrate_reviews_and_orders();
DROP PROCEDURE IF EXISTS migrate_reviews_and_orders;

-- 5. Add constraints to reviews table
ALTER TABLE reviews ADD CONSTRAINT uk_reviews_order UNIQUE (order_id);
ALTER TABLE reviews ADD CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5);

-- 6. Add indexes
CREATE INDEX idx_orders_customer_created ON orders (customer_id, created_at DESC);
CREATE INDEX idx_orders_customer_status_created ON orders (customer_id, status, created_at DESC);
CREATE INDEX idx_reviews_customer_created ON reviews (customer_id, created_at DESC);
CREATE INDEX idx_osh_order_created ON order_status_history (order_id, created_at);
CREATE INDEX idx_osh_created ON order_status_history (created_at);

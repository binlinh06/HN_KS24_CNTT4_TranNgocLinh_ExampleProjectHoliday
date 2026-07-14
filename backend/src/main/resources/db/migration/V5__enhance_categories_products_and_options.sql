-- V5__enhance_categories_products_and_options.sql
-- Enhance tables to support display order, slugs, featured flags, and indexes on MySQL 8

-- Categories Table Updates
ALTER TABLE categories ADD COLUMN display_order INT NOT NULL DEFAULT 0;
CREATE INDEX idx_categories_name ON categories(category_name);
CREATE INDEX idx_categories_display_order ON categories(display_order);

-- Products Table Updates
ALTER TABLE products ADD COLUMN slug VARCHAR(255) NULL;
ALTER TABLE products ADD COLUMN is_featured TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE products ADD COLUMN preparation_time_minutes INT NOT NULL DEFAULT 15;

-- Update existing products with a temporary slug based on name
UPDATE products SET slug = LOWER(REPLACE(REPLACE(REPLACE(product_name, ' ', '-'), 'đ', 'd'), 'đ', 'd'));
-- Make it NOT NULL after population
ALTER TABLE products MODIFY COLUMN slug VARCHAR(255) NOT NULL;

-- Create indexes for products
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_is_available ON products(is_available);
CREATE INDEX idx_products_is_featured ON products(is_featured);
-- Unique index for active products' slugs (uniqueness only applies to non-deleted items)
CREATE UNIQUE INDEX idx_products_slug_active ON products(slug, (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END));

-- Option Groups Updates
ALTER TABLE option_groups ADD COLUMN min_selectable INT NOT NULL DEFAULT 0;
ALTER TABLE option_groups ADD COLUMN display_order INT NOT NULL DEFAULT 0;
ALTER TABLE option_groups ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1;

-- Product Options Updates
ALTER TABLE product_options ADD COLUMN display_order INT NOT NULL DEFAULT 0;

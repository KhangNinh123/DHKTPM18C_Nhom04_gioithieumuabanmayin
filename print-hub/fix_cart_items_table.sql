orders-- Fix cart_items and carts table structure
USE printshop;

-- Drop the existing tables if they exist with incorrect structure
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;

-- Hibernate will recreate the tables with the correct structure on next application start
-- The new cart_items structure will have:
-- - id (INT AUTO_INCREMENT PRIMARY KEY)
-- - cart_id (foreign key to carts)
-- - product_id (foreign key to products)
-- - quantity (INT NOT NULL)
-- - price_at_add (DECIMAL(16,2) NOT NULL)

-- The new carts structure will have:
-- - id (INT AUTO_INCREMENT PRIMARY KEY)
-- - user_id (INT NOT NULL UNIQUE, foreign key to users)
-- - total (DECIMAL(16,2) NOT NULL DEFAULT 0.00)


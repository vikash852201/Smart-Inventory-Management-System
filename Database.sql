-- ========= Full SQL script for inventory_db (final) =========
-- Drop & recreate DB
DROP DATABASE IF EXISTS `inventory_new`;
CREATE DATABASE `inventory_new` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `inventory_new`;

-- ========= users table (used by LoginForm / cart / orders) =========
CREATE TABLE `users` (
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `role` ENUM('admin','manager','customer') NOT NULL DEFAULT 'customer',
  `full_name` VARCHAR(100) DEFAULT NULL,
  `email` VARCHAR(120) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB;

-- ========= products table (used throughout) =========
CREATE TABLE `products` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NOT NULL,
  `category` VARCHAR(80) DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `quantity` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_products_name` (`name`)
) ENGINE=InnoDB;

-- ========= cart table (CustomerDashboard / Customer actions) =========
CREATE TABLE `cart` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `product_id` INT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `added_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_cart_user_product` (`username`, `product_id`),
  INDEX (`username`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ========= normalized orders schema (CartView checkout, MyOrders, RecentOrdersView, OrderDetails) =========
CREATE TABLE `orders` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) DEFAULT NULL,       -- may be null for guest orders
  `order_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `status` VARCHAR(30) NOT NULL DEFAULT 'Confirmed',
  `shipping_address` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_orders_username` (`username`),
  INDEX `idx_orders_date` (`order_date`),
  CONSTRAINT `fk_orders_user` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE `order_items` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `order_id` INT NOT NULL,
  `product_id` INT NOT NULL,
  `quantity` INT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL, -- unit price at time of order
  PRIMARY KEY (`id`),
  INDEX (`order_id`),
  INDEX (`product_id`),
  CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ========= POS-style table (SellItem inserts here) =========
-- SellItem writes per-line rows with customer_name & product_name etc.
CREATE TABLE `pos_orders` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `customer_name` VARCHAR(120) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `product_name` VARCHAR(150) NOT NULL,
  `quantity` INT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `total` DECIMAL(12,2) NOT NULL,
  `date_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX (`date_time`)
) ENGINE=InnoDB;

-- ========= optional settings & audit (small helpers) =========
CREATE TABLE IF NOT EXISTS `settings` (
  `k` VARCHAR(64) NOT NULL,
  `v` TEXT,
  PRIMARY KEY (`k`)
) ENGINE=InnoDB;

-- ========= Sample data =========
-- Users (demo). Passwords are plaintext for demo only — hash in production.
INSERT INTO `users` (`username`, `password`, `role`, `full_name`, `email`, `phone`) VALUES
  ('admin', 'admin123', 'admin', 'Administrator', 'admin@example.com', '9999999999'),
  ('manager', 'manager123', 'manager', 'Store Manager', 'manager@example.com', '9999999998'),
  ('prem', 'prem123', 'customer', 'Prem Kumar', 'prem@example.com', '9876543210'),
  ('vikash', 'Vikash@2005', 'customer', 'Vikash Jaiswal', 'vikash@example.com', '9123456780');

-- Products
INSERT INTO `products` (`name`, `category`, `price`, `quantity`) VALUES
  ('Plain T-Shirt', 'Clothing', 249.00, 50),
  ('Wireless Mouse', 'Electronics', 599.00, 30),
  ('USB-C Cable', 'Electronics', 199.00, 100),
  ('LED Bulb (9W)', 'Home Appliances', 129.00, 80),
  ('Siren 220V', 'Home Appliances', 349.00, 10);

-- Cart entries (example)
INSERT INTO `cart` (`username`, `product_id`, `quantity`) VALUES
  ('prem', 2, 1),
  ('prem', 3, 2);

-- A sample normalized order (orders + order_items)
INSERT INTO `orders` (`username`, `order_date`, `total_amount`, `status`, `shipping_address`)
VALUES
  ('prem', '2025-10-01 11:30:00', 1397.00, 'Delivered', '123, Example Street, City');

-- Suppose the above order gets id = 1 (fresh DB) -> create order_items
INSERT INTO `order_items` (`order_id`, `product_id`, `quantity`, `price`) VALUES
  (1, 2, 1, 599.00),
  (1, 4, 1, 129.00),
  (1, 3, 2, 199.00);

-- Decrement product stock to reflect sample order
UPDATE `products` SET `quantity` = `quantity` - 1 WHERE `id` = 2;
UPDATE `products` SET `quantity` = `quantity` - 1 WHERE `id` = 4;
UPDATE `products` SET `quantity` = `quantity` - 2 WHERE `id` = 3;

-- A couple of POS-style orders (SellItem) — each row is one billed line
INSERT INTO `pos_orders` (`customer_name`, `phone`, `product_name`, `quantity`, `price`, `total`, `date_time`) VALUES
  ('Ravi Kumar','9876500000','Plain T-Shirt',2,249.00,498.00,'2025-11-08 14:15:00'),
  ('Ravi Kumar','9876500000','USB-C Cable',1,199.00,199.00,'2025-11-08 14:15:00');

-- Settings
INSERT INTO `settings` (`k`,`v`) VALUES
  ('currency_symbol','₹'),
  ('low_stock_threshold','5');

-- ========= Indexes for performance =========
CREATE INDEX `idx_products_category` ON `products` (`category`);
CREATE INDEX `idx_cart_username` ON `cart` (`username`);
CREATE INDEX `idx_orders_recent` ON `orders` (`order_date`);

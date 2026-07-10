CREATE DATABASE IF NOT EXISTS smartcart_db;
USE smartcart_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS tracking_updates;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS delivery_addresses;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS wishlist_items;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS product_images;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS coupons;
DROP TABLE IF EXISTS brands;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  mobile VARCHAR(20),
  role VARCHAR(30) DEFAULT 'CUSTOMER',
  auth_token VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255)
);

CREATE TABLE brands (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  logo_url VARCHAR(700),
  description VARCHAR(255),
  trending BOOLEAN DEFAULT TRUE
);

CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  price DECIMAL(10,2),
  mrp DECIMAL(10,2),
  discount_percent INT,
  stock INT,
  image_url VARCHAR(700),
  brand VARCHAR(100),
  size_chart VARCHAR(255),
  color VARCHAR(80),
  rating DOUBLE,
  review_count INT,
  style_tag VARCHAR(100),
  demand_tag VARCHAR(100),
  season_tag VARCHAR(100),
  product_type VARCHAR(100),
  available_pincodes VARCHAR(500),
  delivery_charge DECIMAL(10,2),
  estimated_delivery_days INT,
  trending BOOLEAN DEFAULT FALSE,
  featured BOOLEAN DEFAULT FALSE,
  category_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE product_images (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  image_url VARCHAR(700) NOT NULL,
  sort_order INT DEFAULT 1,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  user_name VARCHAR(120),
  rating INT,
  comment TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  product_id BIGINT,
  quantity INT,
  selected_size VARCHAR(50),
  saved_for_later BOOLEAN DEFAULT FALSE,
  added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE wishlist_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  product_id BIGINT,
  added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE coupons (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL UNIQUE,
  title VARCHAR(150),
  description VARCHAR(500),
  discount_percent INT,
  min_order_amount DECIMAL(10,2),
  max_discount_amount DECIMAL(10,2),
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  title VARCHAR(150),
  message VARCHAR(600),
  type VARCHAR(50),
  read_status BOOLEAN DEFAULT FALSE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE delivery_addresses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  receiver_name VARCHAR(150),
  mobile VARCHAR(20),
  email VARCHAR(150),
  address TEXT,
  city VARCHAR(100),
  state_name VARCHAR(100),
  pincode VARCHAR(20),
  landmark VARCHAR(200),
  delivery_instructions VARCHAR(500),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  total_amount DECIMAL(10,2),
  discount_amount DECIMAL(10,2),
  delivery_charge DECIMAL(10,2),
  payable_amount DECIMAL(10,2),
  coupon_code VARCHAR(50),
  status VARCHAR(50),
  receiver_name VARCHAR(150),
  delivery_mobile VARCHAR(20),
  email VARCHAR(150),
  address TEXT,
  city VARCHAR(100),
  state_name VARCHAR(100),
  pincode VARCHAR(20),
  landmark VARCHAR(200),
  delivery_instructions VARCHAR(500),
  payment_method VARCHAR(100),
  payment_status VARCHAR(50),
  transaction_id VARCHAR(120),
  estimated_delivery_date DATETIME,
  order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT,
  product_id BIGINT,
  product_name VARCHAR(150),
  image_url VARCHAR(700),
  price DECIMAL(10,2),
  quantity INT,
  selected_size VARCHAR(50),
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT,
  payment_method VARCHAR(100),
  payment_status VARCHAR(50),
  transaction_id VARCHAR(120),
  amount DECIMAL(10,2),
  paid_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE tracking_updates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT,
  status VARCHAR(100),
  location VARCHAR(150),
  message VARCHAR(600),
  step_order INT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

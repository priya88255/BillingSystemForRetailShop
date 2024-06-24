
-- Create the database if it doesn't exist
CREATE DATABASE retail_shop;
USE retail_shop;

-- Create customers table
CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    address VARCHAR(255)
);

-- Create products table
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    stock INT NOT NULL CHECK (stock >= 0),
    rate DECIMAL(10, 2) NOT NULL DEFAULT 0,
    CHECK (rate <= price)
);

-- Create bills table
CREATE TABLE bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    tot_quantity INT NOT NULL DEFAULT 0,
    bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10, 2) NOT NULL CHECK (total > 0),
    payment_method ENUM('cash', 'credit_card', 'upi') NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Create bill_items table
CREATE TABLE bill_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
-- Create feedback table
CREATE TABLE feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    feedback_date DATE,
    rating INT,
    comments TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Insert duplicate data for customers
INSERT INTO customers (name, email, phone, address) VALUES
('Ravi Kumar', 'ravi@example.com', '9876543210', '1st gandhi street'),
('Sita Sharma', 'sita@example.com', '8765432109', '123, nehru street'),
('Arun Vijay', 'arun@example.com', '7654321098', '2nd cross avenue'),
('Priya Iyer', 'priya@example.com', '6543210987', '123, nehru street'),
('Rajesh Singh', 'rajesh@example.com', '5432109876', '56st rajpa road'),
('Anita Reddy', 'anita@example.com', '4321098765', '1st gandhi street'),
('Vikram Chandra', 'vikram@example.com', '3210987654', '2nd cross avenue'),
('Lakshmi Nair', 'lakshmi@example.com', '2109876543', '123, nehru street'),
('Karthik Raju', 'karthik@example.com', '1098765432', '1st gandhi street'),
('Meena Menon', 'meena@example.com', '9988776655', '2nd cross avenue');

INSERT INTO products (name, price, stock, rate) VALUES
('Rice Bag 25kg', 1200.00, 100, 1100.00),
('Sunflower Oil 1L', 150.00, 200, 145.00),
('Turmeric Powder 500g', 100.00, 50, 96.00),
('Chilli Powder 500g', 120.00, 75, 120.00),
('Toor Dal 1kg', 140.00, 150, 135.00),
('Urad Dal 1kg', 130.00, 100, 125.00),
('Sugar 1kg', 40.00, 300, 40.00),
('Salt 1kg', 20.00, 500, 20.00),
('Wheat Flour 5kg', 250.00, 80, 250.00),
('Besan Flour 1kg', 90.00, 60, 90.00),
('Coconut Oil 500ml', 180.00, 200, 180.00),
('Groundnut Oil 1L', 200.00, 150, 200.00),
('Mustard Seeds 100g', 30.00, 300, 29.00),
('Cumin Seeds 100g', 40.00, 250, 40.00),
('Black Pepper 100g', 60.00, 150, 60.00),
('Garlic 250g', 50.00, 200, 50.00),
('Ginger 250g', 40.00, 180, 40.00),
('Green Tea 100g', 120.00, 100, 120.00),
('Coffee Powder 200g', 200.00, 150, 199.00),
('Tea Powder 500g', 150.00, 200, 150.00),
('Soap Bar 100g', 20.00, 500, 20.00),
('Shampoo 200ml', 100.00, 300, 99.00),
('Toothpaste 200g', 80.00, 400, 80.00),
('Toothbrush', 20.00, 500, 20.00),
('Dishwash Bar 200g', 25.00, 350, 25.00),
('Detergent Powder 1kg', 50.00, 300, 50.00),
('Hand Wash 500ml', 70.00, 200, 70.00),
('Floor Cleaner 1L', 90.00, 150, 90.00),
('Glass Cleaner 500ml', 60.00, 180, 60.00),
('Sanitary Napkins Pack of 10', 50.00, 300, 49.00);

-- Insert sample data for bills
INSERT INTO bills (customer_id, tot_quantity, bill_date, total, payment_method) VALUES
(1, 6, '2023-06-01 10:00:00', 3000.00, 'cash'),
(2, 7, '2023-06-02 11:00:00', 940.00, 'credit_card'),
(3, 3, '2023-06-03 12:00:00', 390.00, 'upi'),
(4, 7, '2023-06-04 13:00:00', 180.00, 'cash'),
(5, 7, '2023-06-05 14:00:00', 790.00, 'credit_card');

-- Insert sample data for bill_items
INSERT INTO bill_items (bill_id, product_id, quantity, price) VALUES
(1, 1, 2, 1200.00), -- Bill 1: 2x Rice Bag 25kg
(1, 2, 4, 150.00),  -- Bill 1: 4x Sunflower Oil 1L
(2, 3, 1, 100.00),  -- Bill 2: 1x Turmeric Powder 500g
(2, 4, 2, 120.00),  -- Bill 2: 2x Chilli Powder 500g
(2, 5, 4, 140.00),  -- Bill 2: 4x Toor Dal 1kg
(3, 6, 3, 130.00),  -- Bill 3: 3x Urad Dal 1kg
(4, 7, 2, 40.00),   -- Bill 4: 2x Sugar 1kg
(4, 8, 5, 20.00),   -- Bill 4: 5x Salt 1kg
(5, 9, 1, 250.00),  -- Bill 5: 1x Wheat Flour 5kg
(5, 10, 6, 90.00);  -- Bill 5: 6x Besan Flour 1kg

-- Insert sample data into the feedback table
INSERT INTO feedback (customer_id, feedback_date, rating, comments) VALUES
(1, '2024-01-01', 5, 'Excellent service!'),
(2, '2024-01-02', 4, 'Good products but a bit pricey.'),
(3, '2024-01-03', 3, 'Average experience.'),
(4, '2024-01-04', 2, 'Not satisfied with the product quality.'),
(5, '2024-01-05', 1, 'Very poor service.'),
(6, '2024-01-06', 5, 'Loved the shopping experience.'),
(7, '2024-01-07', 4, 'Great variety of products.'),
(8, '2024-01-08', 3, 'Okay, but could be better.'),
(9, '2024-01-09', 2, 'Not happy with the customer support.'),
(10, '2024-01-10', 1, 'Terrible experience, will not come again.');


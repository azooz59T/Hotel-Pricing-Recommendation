CREATE TABLE buildings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE products (
    id VARCHAR(50) PRIMARY KEY,  -- PROD_0001, PROD_0002, etc.
    room_name VARCHAR(255),      -- "Room A", "Room B", etc.
    arrival_date DATE,
    beds INTEGER,                -- "No. of Beds" from CSV
    room_type VARCHAR(100),      -- "Single", "Double Room", etc.
    grade INTEGER,               -- Star rating 1-7
    private_pool BOOLEAN,        -- "Yes"/"No" from CSV
    building_name VARCHAR(255)   -- From buildings.csv
);

CREATE TABLE bookings (
    id VARCHAR(50) PRIMARY KEY,  -- BOOK_000001, etc.
    product_id VARCHAR(50),      -- References products.id
    creation_date DATE,
    confirmation_status VARCHAR(50), -- "Confirmed", "Pending", "Cancelled"
    arrival_date DATE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE prices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50),      -- References products.id
    price DECIMAL(10,2),         -- Current price from CSV
    currency VARCHAR(10),        -- "USD", "EUR", etc.
    recommended_price DECIMAL(10,2), -- Calculated by our algorithm
    FOREIGN KEY (product_id) REFERENCES products(id)
);
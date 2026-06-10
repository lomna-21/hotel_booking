-- Room Status lookup table
CREATE TABLE room_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Room Type lookup table
CREATE TABLE room_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Booking Status lookup table
CREATE TABLE booking_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Customer Profiles
CREATE TABLE customer_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(20) NOT NULL UNIQUE,
    user_id BIGINT UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255)    NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Rooms
CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(20) NOT NULL UNIQUE,
    hotel_id BIGINT NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    room_status VARCHAR(50) NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    max_occupancy INT NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_room_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    CONSTRAINT uq_room_number_hotel UNIQUE (hotel_id, room_number)
);

-- Bookings
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(20) NOT NULL UNIQUE,
    hotel_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    booked_by BIGINT,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    booking_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_booking_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customer_profiles(id),
    CONSTRAINT fk_booking_booked_by FOREIGN KEY (booked_by) REFERENCES users(id)
);

-- Reviews
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(20) NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    rating DECIMAL(3, 1) NOT NULL,
    comment TEXT,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES customer_profiles(id),
    CONSTRAINT fk_review_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    CONSTRAINT chk_rating CHECK (rating BETWEEN 0.0 AND 5.0)
);

-- Seed room status
INSERT INTO room_status (public_id, name) VALUES
    ('RST-00000001', 'AVAILABLE'),
    ('RST-00000002', 'BOOKED'),
    ('RST-00000003', 'OCCUPIED'),
    ('RST-00000004', 'MAINTENANCE');

-- Seed room types
INSERT INTO room_types (public_id, name) VALUES
    ('RMT-00000001', 'SINGLE'),
    ('RMT-00000002', 'DOUBLE'),
    ('RMT-00000003', 'SUITE'),
    ('RMT-00000004', 'DELUXE'),
    ('RMT-00000005', 'PENTHOUSE');

-- Seed booking status
INSERT INTO booking_status (public_id, name) VALUES
    ('BST-00000001', 'PENDING'),
    ('BST-00000002', 'CONFIRMED'),
    ('BST-00000003', 'CANCELLED'),
    ('BST-00000004', 'COMPLETED');
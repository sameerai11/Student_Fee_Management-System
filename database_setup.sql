-- --------------------------------------------------------
-- DATABASE SETUP SCRIPT for Student Fee Management System
-- --------------------------------------------------------

-- 1. Create and Select Database
DROP DATABASE IF EXISTS student_fees_db;
CREATE DATABASE student_fees_db;
USE student_fees_db;

-- 2. Create Students Table
CREATE TABLE Students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    department VARCHAR(50),
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Create Fee Templates Table (fees)
CREATE TABLE fees (
    fee_template_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    default_amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255)
);

-- 4. Create Users Table
-- NOTE: Password hash placeholder used. Replace [ADMIN_PASS_HASH] and [TEST_PASS_HASH] with real hashes.
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'student') NOT NULL,
    student_id INT,
    FOREIGN KEY (student_id) REFERENCES Students(student_id)
);

-- 5. Create Student Fees Table (Assigned Fees)
CREATE TABLE studentfees (
    student_fee_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    fee_id INT NOT NULL, -- Links to the fee template
    fee_amount DECIMAL(10, 2) NOT NULL,
    due_date DATE NOT NULL,
    status ENUM('OUTSTANDING', 'PARTIAL', 'PAID') NOT NULL DEFAULT 'OUTSTANDING',
    description VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES Students(student_id),
    FOREIGN KEY (fee_id) REFERENCES fees(fee_template_id)
);

-- 6. Create Transactions Table (Fee Payments)
-- CRITICAL: Uses the schema confirmed from DESCRIBE command
CREATE TABLE transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    student_fee_id INT NOT NULL,
    payment_amount DECIMAL(10, 2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) NOT NULL,
    FOREIGN KEY (student_fee_id) REFERENCES studentfees(student_fee_id)
);


-- --------------------------------------------------------
-- 7. Initial Data Inserts
-- --------------------------------------------------------

-- Student Data
INSERT INTO Students (first_name, last_name, department, email)
VALUES ('Alice', 'Jonson', 'Computer Science', 'alice.j@school.edu');

-- Fee Templates
INSERT INTO fees (name, default_amount, description)
VALUES
    ('Tuition Fee', 50000.00, 'Standard semester tuition charge'),
    ('Annual Fee', 5000.00, 'Non-refundable annual maintenance fee');


-- User Data (Linking to Student and Admin)
INSERT INTO users (username, password, role, student_id)
VALUES
    -- Admin User
    ('admin', '[ADMIN_PASS_HASH]', 'admin', NULL),
    -- Student User (ID 1)
    ('alice', '[TEST_PASS_HASH]', 'student', 1);

-- Initial Assigned Fees for Alice (Student ID 1)
INSERT INTO studentfees (student_id, fee_id, fee_amount, due_date, status, description)
VALUES
    (1, 1, 50000.00, '2025-12-02', 'PAID', 'Fall 2025 Tuition Fee'),
    (1, 2, 5000.00, '2025-12-07', 'PAID', 'Annual Maintenance Fee');

-- Initial Transaction Data (To reflect the 'PAID' status of the fees above)
-- Payment for StudentFee ID 3 (Tuition)
INSERT INTO transactions (student_fee_id, payment_amount, payment_method)
VALUES (3, 50000.00, 'Cash');

-- Payment for StudentFee ID 4 (Annual Fee)
INSERT INTO transactions (student_fee_id, payment_amount, payment_method)
VALUES (4, 5000.00, 'Cash');
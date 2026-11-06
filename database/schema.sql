-- =====================================================================
-- CineFlow - Movie Ticket Reservation System
-- Database Schema Definition (MySQL 8.x, InnoDB)
-- Database Name: cineflow_db
-- =====================================================================

CREATE DATABASE IF NOT EXISTS cineflow_db;
USE cineflow_db;

-- Drop tables in reverse foreign-key dependency order
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS seat_holds;
DROP TABLE IF EXISTS booking_seats;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS showtimes;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS screens;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS users;

-- ---------------------------------------------------------------------
-- Table 1: users
-- Stores system users: ADMIN and CUSTOMER accounts
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('ADMIN', 'CUSTOMER') NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table 2: movies
-- Stores movie catalog information
-- ---------------------------------------------------------------------
CREATE TABLE movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    genre VARCHAR(50) NOT NULL,
    language VARCHAR(50) NOT NULL,
    duration_minutes INT NOT NULL,
    rating DECIMAL(3, 1) NOT NULL DEFAULT 0.0,
    description TEXT,
    release_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_movies_title (title),
    INDEX idx_movies_active (active),
    INDEX idx_movies_genre (genre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table 3: screens
-- Multiplex auditoriums/screens
-- ---------------------------------------------------------------------
CREATE TABLE screens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    total_rows INT NOT NULL,
    seats_per_row INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table 4: seats
-- Physical seat layout per screen
-- ---------------------------------------------------------------------
CREATE TABLE seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    screen_id INT NOT NULL,
    row_label VARCHAR(5) NOT NULL,
    seat_number INT NOT NULL,
    seat_type ENUM('REGULAR', 'PREMIUM') NOT NULL DEFAULT 'REGULAR',
    CONSTRAINT fk_seats_screen FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE CASCADE,
    CONSTRAINT uk_screen_seat UNIQUE (screen_id, row_label, seat_number),
    INDEX idx_seats_screen (screen_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table 5: showtimes
-- Scheduled screenings linking movies, screens, and ticket pricing
-- ---------------------------------------------------------------------
CREATE TABLE showtimes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    screen_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    price_regular DECIMAL(10, 2) NOT NULL,
    price_premium DECIMAL(10, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_showtimes_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE RESTRICT,
    CONSTRAINT fk_showtimes_screen FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE RESTRICT,
    INDEX idx_showtimes_start_time (start_time),
    INDEX idx_showtimes_movie (movie_id),
    INDEX idx_showtimes_screen (screen_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table 6: bookings
-- Ticket reservations made by registered customers
-- ---------------------------------------------------------------------
CREATE TABLE bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    showtime_id INT NOT NULL,
    booking_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(id) ON DELETE RESTRICT,
    INDEX idx_bookings_booking_time (booking_time),
    INDEX idx_bookings_user (user_id),
    INDEX idx_bookings_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table 7: booking_seats
-- Line items connecting booked seats to a booking and showtime.
-- The UNIQUE (showtime_id, seat_id) constraint prevents double bookings!
-- ---------------------------------------------------------------------
CREATE TABLE booking_seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    showtime_id INT NOT NULL,
    seat_id INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_bseats_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_bseats_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bseats_seat FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE RESTRICT,
    CONSTRAINT uk_booking_showtime_seat UNIQUE (showtime_id, seat_id),
    INDEX idx_bseats_booking (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table 8: seat_holds
-- Temporary 5-minute reservations during payment checkout.
-- The UNIQUE (showtime_id, seat_id) constraint prevents duplicate active holds.
-- ---------------------------------------------------------------------
CREATE TABLE seat_holds (
    id INT AUTO_INCREMENT PRIMARY KEY,
    showtime_id INT NOT NULL,
    seat_id INT NOT NULL,
    user_id INT NOT NULL,
    held_until DATETIME NOT NULL,
    CONSTRAINT fk_holds_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(id) ON DELETE CASCADE,
    CONSTRAINT fk_holds_seat FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE,
    CONSTRAINT fk_holds_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_hold_showtime_seat UNIQUE (showtime_id, seat_id),
    INDEX idx_holds_held_until (held_until),
    INDEX idx_holds_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table 9: payments
-- Simulated payment records for completed bookings
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    method ENUM('CASH', 'UPI', 'CARD') NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status ENUM('PAID', 'REFUNDED') NOT NULL DEFAULT 'PAID',
    paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE RESTRICT,
    INDEX idx_payments_status (status),
    INDEX idx_payments_paid_at (paid_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


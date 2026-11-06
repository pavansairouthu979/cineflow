package com.cineflow.dao;

import com.cineflow.model.*;
import com.cineflow.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for 'bookings', 'booking_seats', and 'payments' tables.
 * Implements strict ACID JDBC transactions with SELECT ... FOR UPDATE.
 */
public class BookingDAO {

    /**
     * Executes the comprehensive JDBC booking transaction as specified in Step 9:
     * 1. Obtain connection.
     * 2. Disable auto-commit.
     * 3. Lock relevant seat records using SELECT ... FOR UPDATE.
     * 4. Check whether seats are already booked.
     * 5. Check whether another customer owns active holds.
     * 6. Verify current customer's holds have not expired.
     * 7. Insert booking.
     * 8. Insert all booking_seats records.
     * 9. Insert payment.
     * 10. Delete current customer's holds.
     * 11. Commit transaction.
     */
    public boolean executeBookingTransaction(Booking booking, List<Integer> seatIds,
                                             List<BigDecimal> seatPrices, Payment payment) throws SQLException {
        if (seatIds == null || seatIds.isEmpty() || seatIds.size() != seatPrices.size()) {
            throw new IllegalArgumentException("Invalid seat selection for booking transaction.");
        }

        Connection conn = null;
        try {
            // 1. Obtain database connection
            conn = DBConnection.getConnection();

            // 2. Disable auto-commit to begin JDBC transaction
            conn.setAutoCommit(false);

            // 3. Lock the relevant seat records using SELECT ... FOR UPDATE
            String inPlaceholders = String.join(",", Collections.nCopies(seatIds.size(), "?"));
            String lockSql = "SELECT id FROM seats WHERE id IN (" + inPlaceholders + ") FOR UPDATE";

            try (PreparedStatement psLock = conn.prepareStatement(lockSql)) {
                for (int i = 0; i < seatIds.size(); i++) {
                    psLock.setInt(i + 1, seatIds.get(i));
                }
                psLock.executeQuery(); // Locks matching rows in InnoDB
            }

            // 4. Check whether any seat is already booked for this showtime
            String checkBookedSql = "SELECT bs.seat_id, s.row_label, s.seat_number " +
                                    "FROM booking_seats bs " +
                                    "JOIN seats s ON bs.seat_id = s.id " +
                                    "JOIN bookings b ON bs.booking_id = b.id " +
                                    "WHERE bs.showtime_id = ? AND b.status = 'CONFIRMED' AND bs.seat_id IN (" + inPlaceholders + ")";

            try (PreparedStatement psCheckBooked = conn.prepareStatement(checkBookedSql)) {
                psCheckBooked.setInt(1, booking.getShowtimeId());
                for (int i = 0; i < seatIds.size(); i++) {
                    psCheckBooked.setInt(i + 2, seatIds.get(i));
                }
                try (ResultSet rs = psCheckBooked.executeQuery()) {
                    if (rs.next()) {
                        String seatCode = rs.getString("row_label") + rs.getInt("seat_number");
                        throw new SQLException("Seat " + seatCode + " has already been booked by another customer.");
                    }
                }
            }

            // 5. Check whether another customer owns active holds on these seats
            String checkHoldsSql = "SELECT sh.seat_id, s.row_label, s.seat_number " +
                                   "FROM seat_holds sh " +
                                   "JOIN seats s ON sh.seat_id = s.id " +
                                   "WHERE sh.showtime_id = ? AND sh.user_id <> ? AND sh.held_until > NOW() " +
                                   "AND sh.seat_id IN (" + inPlaceholders + ")";

            try (PreparedStatement psCheckHolds = conn.prepareStatement(checkHoldsSql)) {
                psCheckHolds.setInt(1, booking.getShowtimeId());
                psCheckHolds.setInt(2, booking.getUserId());
                for (int i = 0; i < seatIds.size(); i++) {
                    psCheckHolds.setInt(i + 3, seatIds.get(i));
                }
                try (ResultSet rs = psCheckHolds.executeQuery()) {
                    if (rs.next()) {
                        String seatCode = rs.getString("row_label") + rs.getInt("seat_number");
                        throw new SQLException("Seat " + seatCode + " is currently held by another customer.");
                    }
                }
            }

            // 6. Verify current customer's holds have not expired
            String verifyHoldSql = "SELECT COUNT(*) FROM seat_holds " +
                                   "WHERE showtime_id = ? AND user_id = ? AND held_until >= NOW() " +
                                   "AND seat_id IN (" + inPlaceholders + ")";

            try (PreparedStatement psVerify = conn.prepareStatement(verifyHoldSql)) {
                psVerify.setInt(1, booking.getShowtimeId());
                psVerify.setInt(2, booking.getUserId());
                for (int i = 0; i < seatIds.size(); i++) {
                    psVerify.setInt(i + 3, seatIds.get(i));
                }
                try (ResultSet rs = psVerify.executeQuery()) {
                    if (rs.next() && rs.getInt(1) < seatIds.size()) {
                        throw new SQLException("Your temporary seat hold has expired. Please select seats again.");
                    }
                }
            }

            // 7. Insert booking into 'bookings' table
            String insertBookingSql = "INSERT INTO bookings (user_id, showtime_id, booking_time, total_amount, status) " +
                                      "VALUES (?, ?, NOW(), ?, 'CONFIRMED')";

            int bookingId;
            try (PreparedStatement psBooking = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                psBooking.setInt(1, booking.getUserId());
                psBooking.setInt(2, booking.getShowtimeId());
                psBooking.setBigDecimal(3, booking.getTotalAmount());
                psBooking.executeUpdate();

                try (ResultSet keys = psBooking.getGeneratedKeys()) {
                    if (keys.next()) {
                        bookingId = keys.getInt(1);
                        booking.setId(bookingId);
                    } else {
                        throw new SQLException("Failed to retrieve generated booking ID.");
                    }
                }
            }

            // 8. Insert all booking_seats records
            String insertSeatsSql = "INSERT INTO booking_seats (booking_id, showtime_id, seat_id, price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psSeats = conn.prepareStatement(insertSeatsSql)) {
                for (int i = 0; i < seatIds.size(); i++) {
                    psSeats.setInt(1, bookingId);
                    psSeats.setInt(2, booking.getShowtimeId());
                    psSeats.setInt(3, seatIds.get(i));
                    psSeats.setBigDecimal(4, seatPrices.get(i));
                    psSeats.addBatch();
                }
                psSeats.executeBatch();
            }

            // 9. Insert payment record into 'payments' table
            String insertPaymentSql = "INSERT INTO payments (booking_id, method, amount, status, paid_at) VALUES (?, ?, ?, 'PAID', NOW())";
            try (PreparedStatement psPayment = conn.prepareStatement(insertPaymentSql, Statement.RETURN_GENERATED_KEYS)) {
                psPayment.setInt(1, bookingId);
                psPayment.setString(2, payment.getMethod().name());
                psPayment.setBigDecimal(3, payment.getAmount());
                psPayment.executeUpdate();

                try (ResultSet keys = psPayment.getGeneratedKeys()) {
                    if (keys.next()) {
                        payment.setId(keys.getInt(1));
                    }
                }
            }

            // 10. Delete current customer's holds
            String deleteHoldsSql = "DELETE FROM seat_holds WHERE showtime_id = ? AND user_id = ?";
            try (PreparedStatement psDel = conn.prepareStatement(deleteHoldsSql)) {
                psDel.setInt(1, booking.getShowtimeId());
                psDel.setInt(2, booking.getUserId());
                psDel.executeUpdate();
            }

            // 11. Commit transaction
            conn.commit();
            return true;

        } catch (SQLException ex) {
            // Roll back changes on any failure
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw ex;
        } finally {
            // Restore auto-commit before closing/returning connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Failed to restore auto-commit: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Cancels a booking and refunds the payment within an ACID transaction.
     */
    public boolean cancelBookingTransaction(int bookingId, int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Verify eligibility: booking exists, belongs to user, and showtime hasn't started
            String verifySql = "SELECT b.id, s.start_time, b.status " +
                               "FROM bookings b " +
                               "JOIN showtimes s ON b.showtime_id = s.id " +
                               "WHERE b.id = ? AND b.user_id = ? FOR UPDATE";

            try (PreparedStatement psVerify = conn.prepareStatement(verifySql)) {
                psVerify.setInt(1, bookingId);
                psVerify.setInt(2, userId);
                try (ResultSet rs = psVerify.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Booking not found or access denied.");
                    }
                    if ("CANCELLED".equals(rs.getString("status"))) {
                        throw new SQLException("Booking is already cancelled.");
                    }
                    Timestamp start = rs.getTimestamp("start_time");
                    if (start.before(Timestamp.valueOf(LocalDateTime.now()))) {
                        throw new SQLException("Cancellation is not permitted after the showtime has started.");
                    }
                }
            }

            // Update booking status
            String updateBooking = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?";
            try (PreparedStatement psB = conn.prepareStatement(updateBooking)) {
                psB.setInt(1, bookingId);
                psB.executeUpdate();
            }

            // Update payment status
            String updatePayment = "UPDATE payments SET status = 'REFUNDED' WHERE booking_id = ?";
            try (PreparedStatement psP = conn.prepareStatement(updatePayment)) {
                psP.setInt(1, bookingId);
                psP.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public List<Integer> getBookedSeatIdsForShowtime(int showtimeId) throws SQLException {
        String sql = "SELECT bs.seat_id FROM booking_seats bs " +
                     "JOIN bookings b ON bs.booking_id = b.id " +
                     "WHERE bs.showtime_id = ? AND b.status = 'CONFIRMED'";
        List<Integer> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("seat_id"));
                }
            }
        }
        return list;
    }

    public List<Booking> findByUserId(int userId) throws SQLException {
        String sql = "SELECT b.id, b.user_id, b.showtime_id, b.booking_time, b.total_amount, b.status, " +
                     "u.username, u.full_name as customer_name, m.title as movie_title, " +
                     "sc.name as screen_name, s.start_time as showtime_start, " +
                     "GROUP_CONCAT(CONCAT(se.row_label, se.seat_number) ORDER BY se.row_label, se.seat_number SEPARATOR ', ') as seat_numbers, " +
                     "p.method as payment_method " +
                     "FROM bookings b " +
                     "JOIN users u ON b.user_id = u.id " +
                     "JOIN showtimes s ON b.showtime_id = s.id " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN screens sc ON s.screen_id = sc.id " +
                     "JOIN booking_seats bs ON b.id = bs.booking_id " +
                     "JOIN seats se ON bs.seat_id = se.id " +
                     "LEFT JOIN payments p ON b.id = p.booking_id " +
                     "WHERE b.user_id = ? " +
                     "GROUP BY b.id, b.user_id, b.showtime_id, b.booking_time, b.total_amount, b.status, " +
                     "u.username, u.full_name, m.title, sc.name, s.start_time, p.method " +
                     "ORDER BY b.booking_time DESC";

        List<Booking> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBooking(rs));
                }
            }
        }
        return list;
    }

    public List<Booking> findAllJoined(LocalDate startDate, LocalDate endDate,
                                       Integer movieId, BookingStatus status, String username) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT b.id, b.user_id, b.showtime_id, b.booking_time, b.total_amount, b.status, " +
                "u.username, u.full_name as customer_name, m.title as movie_title, " +
                "sc.name as screen_name, s.start_time as showtime_start, " +
                "GROUP_CONCAT(CONCAT(se.row_label, se.seat_number) ORDER BY se.row_label, se.seat_number SEPARATOR ', ') as seat_numbers, " +
                "p.method as payment_method " +
                "FROM bookings b " +
                "JOIN users u ON b.user_id = u.id " +
                "JOIN showtimes s ON b.showtime_id = s.id " +
                "JOIN movies m ON s.movie_id = m.id " +
                "JOIN screens sc ON s.screen_id = sc.id " +
                "JOIN booking_seats bs ON b.id = bs.booking_id " +
                "JOIN seats se ON bs.seat_id = se.id " +
                "LEFT JOIN payments p ON b.id = p.booking_id " +
                "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (startDate != null) {
            sql.append(" AND DATE(b.booking_time) >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND DATE(b.booking_time) <= ?");
            params.add(Date.valueOf(endDate));
        }
        if (movieId != null && movieId > 0) {
            sql.append(" AND s.movie_id = ?");
            params.add(movieId);
        }
        if (status != null) {
            sql.append(" AND b.status = ?");
            params.add(status.name());
        }
        if (username != null && !username.trim().isEmpty()) {
            sql.append(" AND u.username LIKE ?");
            params.add("%" + username.trim() + "%");
        }

        sql.append(" GROUP BY b.id, b.user_id, b.showtime_id, b.booking_time, b.total_amount, b.status, ")
           .append("u.username, u.full_name, m.title, sc.name, s.start_time, p.method ")
           .append("ORDER BY b.booking_time DESC");

        List<Booking> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBooking(rs));
                }
            }
        }
        return list;
    }

    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("showtime_id"),
                rs.getTimestamp("booking_time").toLocalDateTime(),
                rs.getBigDecimal("total_amount"),
                BookingStatus.fromString(rs.getString("status"))
        );
        b.setUsername(rs.getString("username"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setMovieTitle(rs.getString("movie_title"));
        b.setScreenName(rs.getString("screen_name"));
        b.setShowtimeStart(rs.getTimestamp("showtime_start").toLocalDateTime());
        b.setSeatNumbers(rs.getString("seat_numbers"));
        b.setPaymentMethod(PaymentMethod.fromString(rs.getString("payment_method")));
        return b;
    }
}


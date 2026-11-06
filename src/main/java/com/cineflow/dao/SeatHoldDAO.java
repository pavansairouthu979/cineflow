package com.cineflow.dao;

import com.cineflow.model.SeatHold;
import com.cineflow.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'seat_holds' table.
 * Handles 5-minute checkout holds and hold expiry checks.
 */
public class SeatHoldDAO {

    public boolean createHold(int showtimeId, int seatId, int userId, LocalDateTime heldUntil) throws SQLException {
        // Clean up expired hold for this specific seat first if one exists
        deleteExpiredHolds();

        String sql = "INSERT INTO seat_holds (showtime_id, seat_id, user_id, held_until) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "user_id = IF(held_until < NOW() OR user_id = VALUES(user_id), VALUES(user_id), user_id), " +
                     "held_until = IF(held_until < NOW() OR user_id = VALUES(user_id), VALUES(held_until), held_until)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ps.setInt(2, seatId);
            ps.setInt(3, userId);
            ps.setTimestamp(4, Timestamp.valueOf(heldUntil));

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteHold(int showtimeId, int seatId, int userId) throws SQLException {
        String sql = "DELETE FROM seat_holds WHERE showtime_id = ? AND seat_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ps.setInt(2, seatId);
            ps.setInt(3, userId);

            return ps.executeUpdate() > 0;
        }
    }

    public void deleteHoldsForUser(int showtimeId, int userId) throws SQLException {
        String sql = "DELETE FROM seat_holds WHERE showtime_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public int deleteExpiredHolds() throws SQLException {
        String sql = "DELETE FROM seat_holds WHERE held_until < NOW()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return ps.executeUpdate();
        }
    }

    /**
     * Retrieves all seat IDs currently with active (unexpired) holds for a showtime.
     */
    public List<Integer> getActiveHeldSeatIds(int showtimeId, int excludeUserId) throws SQLException {
        String sql = "SELECT seat_id FROM seat_holds " +
                     "WHERE showtime_id = ? AND held_until > NOW() AND user_id <> ?";
        List<Integer> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ps.setInt(2, excludeUserId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("seat_id"));
                }
            }
        }
        return list;
    }

    /**
     * Checks if a seat is currently held by someone other than the current user.
     */
    public boolean isSeatHeldByOther(int showtimeId, int seatId, int currentUserId) throws SQLException {
        String sql = "SELECT 1 FROM seat_holds " +
                     "WHERE showtime_id = ? AND seat_id = ? AND user_id <> ? AND held_until > NOW() LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ps.setInt(2, seatId);
            ps.setInt(3, currentUserId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}


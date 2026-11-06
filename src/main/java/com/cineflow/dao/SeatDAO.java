package com.cineflow.dao;

import com.cineflow.model.Seat;
import com.cineflow.model.SeatType;
import com.cineflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for 'seats' table.
 */
public class SeatDAO {

    public boolean createSeatsBatch(List<Seat> seats) throws SQLException {
        String sql = "INSERT INTO seats (screen_id, row_label, seat_number, seat_type) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            try {
                for (Seat seat : seats) {
                    ps.setInt(1, seat.getScreenId());
                    ps.setString(2, seat.getRowLabel());
                    ps.setInt(3, seat.getSeatNumber());
                    ps.setString(4, seat.getSeatType().name());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Seat> findByScreenId(int screenId) throws SQLException {
        String sql = "SELECT id, screen_id, row_label, seat_number, seat_type " +
                     "FROM seats WHERE screen_id = ? ORDER BY row_label ASC, seat_number ASC";
        List<Seat> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, screenId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Seat(
                            rs.getInt("id"),
                            rs.getInt("screen_id"),
                            rs.getString("row_label"),
                            rs.getInt("seat_number"),
                            SeatType.fromString(rs.getString("seat_type"))
                    ));
                }
            }
        }
        return list;
    }

    public Optional<Seat> findById(int id) throws SQLException {
        String sql = "SELECT id, screen_id, row_label, seat_number, seat_type FROM seats WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Seat(
                            rs.getInt("id"),
                            rs.getInt("screen_id"),
                            rs.getString("row_label"),
                            rs.getInt("seat_number"),
                            SeatType.fromString(rs.getString("seat_type"))
                    ));
                }
            }
        }
        return Optional.empty();
    }
}

package com.cineflow.dao;

import com.cineflow.model.Showtime;
import com.cineflow.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for 'showtimes' table with joined movie & screen details
 * and conflict/overlap detection.
 */
public class ShowtimeDAO {

    public boolean create(Showtime showtime) throws SQLException {
        String sql = "INSERT INTO showtimes (movie_id, screen_id, start_time, price_regular, price_premium) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, showtime.getMovieId());
            ps.setInt(2, showtime.getScreenId());
            ps.setTimestamp(3, Timestamp.valueOf(showtime.getStartTime()));
            ps.setBigDecimal(4, showtime.getPriceRegular());
            ps.setBigDecimal(5, showtime.getPricePremium());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        showtime.setId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean update(Showtime showtime) throws SQLException {
        String sql = "UPDATE showtimes SET movie_id = ?, screen_id = ?, start_time = ?, " +
                     "price_regular = ?, price_premium = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtime.getMovieId());
            ps.setInt(2, showtime.getScreenId());
            ps.setTimestamp(3, Timestamp.valueOf(showtime.getStartTime()));
            ps.setBigDecimal(4, showtime.getPriceRegular());
            ps.setBigDecimal(5, showtime.getPricePremium());
            ps.setInt(6, showtime.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM showtimes WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Optional<Showtime> findById(int id) throws SQLException {
        String sql = "SELECT s.id, s.movie_id, s.screen_id, s.start_time, s.price_regular, s.price_premium, " +
                     "m.title as movie_title, m.duration_minutes, sc.name as screen_name " +
                     "FROM showtimes s " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN screens sc ON s.screen_id = sc.id " +
                     "WHERE s.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToShowtime(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Showtime> findAll() throws SQLException {
        String sql = "SELECT s.id, s.movie_id, s.screen_id, s.start_time, s.price_regular, s.price_premium, " +
                     "m.title as movie_title, m.duration_minutes, sc.name as screen_name " +
                     "FROM showtimes s " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN screens sc ON s.screen_id = sc.id " +
                     "ORDER BY s.start_time DESC";
        List<Showtime> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToShowtime(rs));
            }
        }
        return list;
    }

    public List<Showtime> findUpcoming() throws SQLException {
        String sql = "SELECT s.id, s.movie_id, s.screen_id, s.start_time, s.price_regular, s.price_premium, " +
                     "m.title as movie_title, m.duration_minutes, sc.name as screen_name " +
                     "FROM showtimes s " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN screens sc ON s.screen_id = sc.id " +
                     "WHERE s.start_time >= NOW() AND m.active = TRUE " +
                     "ORDER BY s.start_time ASC";
        List<Showtime> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToShowtime(rs));
            }
        }
        return list;
    }

    public List<Showtime> findByMovieId(int movieId) throws SQLException {
        String sql = "SELECT s.id, s.movie_id, s.screen_id, s.start_time, s.price_regular, s.price_premium, " +
                     "m.title as movie_title, m.duration_minutes, sc.name as screen_name " +
                     "FROM showtimes s " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN screens sc ON s.screen_id = sc.id " +
                     "WHERE s.movie_id = ? AND s.start_time >= NOW() AND m.active = TRUE " +
                     "ORDER BY s.start_time ASC";
        List<Showtime> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToShowtime(rs));
                }
            }
        }
        return list;
    }

    /**
     * Checks if a showtime overlaps with an existing screening on the same screen.
     * Overlap condition: existingStart < newEnd AND existingEnd > newStart.
     * Back-to-back shows (existingStart == newEnd or existingEnd == newStart) are allowed!
     */
    public boolean hasOverlap(int screenId, LocalDateTime newStart, LocalDateTime newEnd, Integer excludeShowtimeId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT 1 FROM showtimes s " +
                "JOIN movies m ON s.movie_id = m.id " +
                "WHERE s.screen_id = ? " +
                "AND s.start_time < ? " +
                "AND DATE_ADD(s.start_time, INTERVAL m.duration_minutes MINUTE) > ?"
        );

        if (excludeShowtimeId != null) {
            sql.append(" AND s.id <> ?");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setInt(1, screenId);
            ps.setTimestamp(2, Timestamp.valueOf(newEnd));
            ps.setTimestamp(3, Timestamp.valueOf(newStart));

            if (excludeShowtimeId != null) {
                ps.setInt(4, excludeShowtimeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Showtime mapResultSetToShowtime(ResultSet rs) throws SQLException {
        Showtime s = new Showtime(
                rs.getInt("id"),
                rs.getInt("movie_id"),
                rs.getInt("screen_id"),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getBigDecimal("price_regular"),
                rs.getBigDecimal("price_premium")
        );
        s.setMovieTitle(rs.getString("movie_title"));
        s.setScreenName(rs.getString("screen_name"));
        s.setDurationMinutes(rs.getInt("duration_minutes"));
        return s;
    }
}

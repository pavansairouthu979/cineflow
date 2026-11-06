package com.cineflow.dao;

import com.cineflow.model.OccupancyReportItem;
import com.cineflow.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Data Access Object for analytical reports and data visualizations.
 * All revenue calculations strictly include PAID payments and exclude REFUNDED.
 */
public class ReportDAO {

    public BigDecimal getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'PAID'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    public int getTodayBookingsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE DATE(booking_time) = CURRENT_DATE() AND status = 'CONFIRMED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Retrieves daily revenue for the past 7 days for the JavaFX BarChart.
     */
    public Map<LocalDate, BigDecimal> getSevenDayRevenue() throws SQLException {
        String sql = "SELECT DATE(paid_at) as pay_date, SUM(amount) as daily_rev " +
                     "FROM payments " +
                     "WHERE status = 'PAID' AND paid_at >= DATE_SUB(CURRENT_DATE(), INTERVAL 6 DAY) " +
                     "GROUP BY DATE(paid_at) " +
                     "ORDER BY pay_date ASC";

        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();

        // Initialize all last 7 days with zero so chart is complete
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            map.put(today.minusDays(i), BigDecimal.ZERO);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LocalDate date = rs.getDate("pay_date").toLocalDate();
                BigDecimal amount = rs.getBigDecimal("daily_rev");
                map.put(date, amount);
            }
        }
        return map;
    }

    /**
     * Retrieves top movies by ticket volume for the JavaFX PieChart.
     */
    public Map<String, Integer> getTopMoviesByTicketSales() throws SQLException {
        String sql = "SELECT m.title, COUNT(bs.id) as ticket_count " +
                     "FROM booking_seats bs " +
                     "JOIN bookings b ON bs.booking_id = b.id " +
                     "JOIN showtimes s ON bs.showtime_id = s.id " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "WHERE b.status = 'CONFIRMED' " +
                     "GROUP BY m.id, m.title " +
                     "ORDER BY ticket_count DESC " +
                     "LIMIT 5";

        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getString("title"), rs.getInt("ticket_count"));
            }
        }
        return map;
    }

    /**
     * Calculates occupancy percentage per showtime.
     * Formula: (booked_seats / (total_rows * seats_per_row)) * 100.
     */
    public List<OccupancyReportItem> getShowtimeOccupancy() throws SQLException {
        String sql = "SELECT s.id as showtime_id, m.title as movie_title, sc.name as screen_name, " +
                     "s.start_time, (sc.total_rows * sc.seats_per_row) as total_seats, " +
                     "COUNT(bs.id) as booked_seats " +
                     "FROM showtimes s " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN screens sc ON s.screen_id = sc.id " +
                     "LEFT JOIN bookings b ON s.id = b.showtime_id AND b.status = 'CONFIRMED' " +
                     "LEFT JOIN booking_seats bs ON b.id = bs.booking_id AND bs.showtime_id = s.id " +
                     "GROUP BY s.id, m.title, sc.name, s.start_time, sc.total_rows, sc.seats_per_row " +
                     "ORDER BY s.start_time DESC";

        List<OccupancyReportItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new OccupancyReportItem(
                        rs.getInt("showtime_id"),
                        rs.getString("movie_title"),
                        rs.getString("screen_name"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getInt("booked_seats"),
                        rs.getInt("total_seats")
                ));
            }
        }
        return list;
    }
}

package com.cineflow.dao;

import com.cineflow.model.Screen;
import com.cineflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for 'screens' table.
 */
public class ScreenDAO {

    public boolean create(Screen screen) throws SQLException {
        String sql = "INSERT INTO screens (name, total_rows, seats_per_row) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, screen.getName());
            ps.setInt(2, screen.getTotalRows());
            ps.setInt(3, screen.getSeatsPerRow());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        screen.setId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public Optional<Screen> findById(int id) throws SQLException {
        String sql = "SELECT id, name, total_rows, seats_per_row FROM screens WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Screen(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("total_rows"),
                            rs.getInt("seats_per_row")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public List<Screen> findAll() throws SQLException {
        String sql = "SELECT id, name, total_rows, seats_per_row FROM screens ORDER BY id ASC";
        List<Screen> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Screen(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("total_rows"),
                        rs.getInt("seats_per_row")
                ));
            }
        }
        return list;
    }

    public boolean existsByName(String name) throws SQLException {
        String sql = "SELECT 1 FROM screens WHERE name = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}


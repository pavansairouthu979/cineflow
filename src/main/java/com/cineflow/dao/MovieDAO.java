package com.cineflow.dao;

import com.cineflow.model.Movie;
import com.cineflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for 'movies' table.
 */
public class MovieDAO {

    public boolean create(Movie movie) throws SQLException {
        String sql = "INSERT INTO movies (title, genre, language, duration_minutes, rating, description, release_date, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, movie.getTitle());
            ps.setString(2, movie.getGenre());
            ps.setString(3, movie.getLanguage());
            ps.setInt(4, movie.getDurationMinutes());
            ps.setDouble(5, movie.getRating());
            ps.setString(6, movie.getDescription());
            ps.setDate(7, Date.valueOf(movie.getReleaseDate()));
            ps.setBoolean(8, movie.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        movie.setId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean update(Movie movie) throws SQLException {
        String sql = "UPDATE movies SET title = ?, genre = ?, language = ?, duration_minutes = ?, " +
                     "rating = ?, description = ?, release_date = ?, active = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, movie.getTitle());
            ps.setString(2, movie.getGenre());
            ps.setString(3, movie.getLanguage());
            ps.setInt(4, movie.getDurationMinutes());
            ps.setDouble(5, movie.getRating());
            ps.setString(6, movie.getDescription());
            ps.setDate(7, Date.valueOf(movie.getReleaseDate()));
            ps.setBoolean(8, movie.isActive());
            ps.setInt(9, movie.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM movies WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            // Foreign key restraint prevents hard delete: soft deactivate instead
            return setActive(id, false);
        }
    }

    public boolean setActive(int id, boolean active) throws SQLException {
        String sql = "UPDATE movies SET active = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Optional<Movie> findById(int id) throws SQLException {
        String sql = "SELECT id, title, genre, language, duration_minutes, rating, description, release_date, active " +
                     "FROM movies WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMovie(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Movie> findAll() throws SQLException {
        String sql = "SELECT id, title, genre, language, duration_minutes, rating, description, release_date, active " +
                     "FROM movies ORDER BY id DESC";
        List<Movie> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToMovie(rs));
            }
        }
        return list;
    }

    public List<Movie> findActive() throws SQLException {
        String sql = "SELECT id, title, genre, language, duration_minutes, rating, description, release_date, active " +
                     "FROM movies WHERE active = TRUE ORDER BY title ASC";
        List<Movie> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToMovie(rs));
            }
        }
        return list;
    }

    public List<Movie> search(String titleQuery, String genre, String language) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, title, genre, language, duration_minutes, rating, description, release_date, active " +
                "FROM movies WHERE active = TRUE"
        );
        List<Object> params = new ArrayList<>();

        if (titleQuery != null && !titleQuery.trim().isEmpty()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + titleQuery.trim() + "%");
        }
        if (genre != null && !genre.equalsIgnoreCase("ALL") && !genre.trim().isEmpty()) {
            sql.append(" AND genre = ?");
            params.add(genre.trim());
        }
        if (language != null && !language.equalsIgnoreCase("ALL") && !language.trim().isEmpty()) {
            sql.append(" AND language = ?");
            params.add(language.trim());
        }

        sql.append(" ORDER BY title ASC");

        List<Movie> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMovie(rs));
                }
            }
        }
        return list;
    }

    private Movie mapResultSetToMovie(ResultSet rs) throws SQLException {
        return new Movie(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("genre"),
                rs.getString("language"),
                rs.getInt("duration_minutes"),
                rs.getDouble("rating"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getBoolean("active")
        );
    }
}


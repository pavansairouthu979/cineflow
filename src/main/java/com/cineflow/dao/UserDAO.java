package com.cineflow.dao;

import com.cineflow.model.Role;
import com.cineflow.model.User;
import com.cineflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for 'users' table.
 * All SQL interactions use PreparedStatement with try-with-resources.
 */
public class UserDAO {

    /**
     * Finds an active or inactive user by username.
     *
     * @param username user login handle
     * @return Optional containing User if found, empty Optional otherwise
     * @throws SQLException if a database error occurs
     */
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, salt, full_name, email, role, active " +
                     "FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a user by primary key ID.
     *
     * @param id user primary key
     * @return Optional containing User if found, empty Optional otherwise
     * @throws SQLException if a database error occurs
     */
    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT id, username, password_hash, salt, full_name, email, role, active " +
                     "FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a user by email address.
     *
     * @param email user email
     * @return Optional containing User if found, empty Optional otherwise
     * @throws SQLException if a database error occurs
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, username, password_hash, salt, full_name, email, role, active " +
                     "FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if a username is already taken.
     *
     * @param username username to check
     * @return true if username exists, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Checks if an email is already registered.
     *
     * @param email email to check
     * @return true if email exists, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Inserts a new user record into the database.
     * Populates the generated primary key back into the User model instance.
     *
     * @param user User object to persist
     * @return true if insert succeeded, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, email, role, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getRole().name());
            ps.setBoolean(7, user.isActive());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Updates an existing user's details.
     *
     * @param user User object containing updated fields
     * @return true if update affected 1 row, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ?, role = ?, active = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Soft-deactivates a user account by setting active = FALSE.
     *
     * @param id user primary key
     * @return true if updated, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean deactivateUser(int id) throws SQLException {
        String sql = "UPDATE users SET active = FALSE WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all users registered in the system.
     *
     * @return list of User objects
     * @throws SQLException if a database error occurs
     */
    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, username, password_hash, salt, full_name, email, role, active " +
                     "FROM users ORDER BY id ASC";
        List<User> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * Maps the current row of a ResultSet to a User domain object.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt"),
                rs.getString("full_name"),
                rs.getString("email"),
                Role.fromString(rs.getString("role")),
                rs.getBoolean("active")
        );
    }
}


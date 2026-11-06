package com.cineflow.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1 Unit and Integration tests verifying JDBC connectivity,
 * configuration loading, and database schema integrity.
 */
class DBConnectionTest {

    @Test
    @DisplayName("Should successfully connect to MySQL using db.properties")
    void testGetConnection() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open and active");
            assertTrue(conn.isValid(2), "Connection should respond to validation probe within 2 seconds");
        }
    }

    @Test
    @DisplayName("testConnection() should return success with 'Database connected' message")
    void testConnectionDiagnostics() {
        DBConnection.ConnectionTestResult result = DBConnection.testConnection();
        assertTrue(result.isSuccess(), "Diagnostic connection test must succeed");
        assertEquals("Database connected", result.getMessage(), "Status message must match specification");
        assertNotNull(result.getDatabaseVersion(), "DBMS version should be populated");
        assertEquals("cineflow_db", result.getDatabaseName(), "Catalog should be 'cineflow_db'");
    }

    @Test
    @DisplayName("Should verify all 9 required tables exist in cineflow_db")
    void testAllNineTablesExist() throws SQLException {
        Set<String> expectedTables = Set.of(
                "users",
                "movies",
                "screens",
                "seats",
                "showtimes",
                "bookings",
                "booking_seats",
                "seat_holds",
                "payments"
        );

        Set<String> actualTables = new HashSet<>();

        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables("cineflow_db", null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    actualTables.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
        }

        for (String expected : expectedTables) {
            assertTrue(actualTables.contains(expected),
                    "Database is missing required table: " + expected);
        }
    }

    @Test
    @DisplayName("Should verify seed data was inserted across catalog, screens, and showtimes")
    void testSeedDataCounts() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Verify at least 1 Admin and 2 Customers = 3 Users
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 3, "Expected at least 3 users");
            }

            // Verify 8 Movies
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM movies")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 8, "Expected at least 8 movies");
            }

            // Verify Screens (at least 2 from seed)
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM screens")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 2, "Expected at least 2 screens");
            }

            // Verify Seats generated (at least 64 from seed)
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM seats")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 64, "Expected at least 64 seats");
            }

            // Verify Showtimes
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM showtimes")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 12, "Expected at least 12 showtimes");
            }

            // Verify Bookings (Historical for reports)
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM bookings")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 25, "Expected at least 25 historical bookings");
            }
        }
    }
}


package com.cineflow.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class responsible for managing JDBC database connections.
 * Loads credentials from 'db.properties' to ensure sensitive credentials
 * are never hard-coded in the source code.
 */
public class DBConnection {

    private static final String DEFAULT_PROPERTIES_FILE = "db.properties";
    private static Properties properties = new Properties();
    private static boolean initialized = false;

    // Static initializer block loads properties once at class loading
    static {
        loadProperties();
    }

    /**
     * Loads the database configuration properties from file or classpath.
     */
    public static synchronized void loadProperties() {
        properties.clear();
        File externalFile = new File(DEFAULT_PROPERTIES_FILE);

        if (externalFile.exists()) {
            try (FileInputStream fis = new FileInputStream(externalFile)) {
                properties.load(fis);
                initialized = true;
                return;
            } catch (IOException e) {
                System.err.println("[DBConnection] Failed to load external db.properties: " + e.getMessage());
            }
        }

        // Fallback to classpath resource
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
            if (is != null) {
                properties.load(is);
                initialized = true;
            } else {
                System.err.println("[DBConnection] db.properties not found on file system or classpath.");
            }
        } catch (IOException e) {
            System.err.println("[DBConnection] Failed to load db.properties from classpath: " + e.getMessage());
        }
    }

    /**
     * Obtains a new JDBC Connection from DriverManager.
     * Callers must close this connection or manage it using try-with-resources.
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs or credentials are invalid
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized || properties.isEmpty()) {
            loadProperties();
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        if (url == null || user == null) {
            throw new SQLException("Database connection configuration missing. " +
                    "Ensure 'db.properties' exists with 'db.url', 'db.user', and 'db.password'.");
        }

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests whether a connection can be established to the database.
     *
     * @return ConnectionTestResult with success status, database metadata or error details
     */
    public static ConnectionTestResult testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                DatabaseMetaData meta = conn.getMetaData();
                String productName = meta.getDatabaseProductName();
                String productVersion = meta.getDatabaseProductVersion();
                String catalog = conn.getCatalog();
                return new ConnectionTestResult(true, "Database connected",
                        productName + " " + productVersion, catalog, null);
            }
            return new ConnectionTestResult(false, "Connection is closed or null", "", "", null);
        } catch (SQLException e) {
            return new ConnectionTestResult(false, "Connection failed: " + e.getMessage(),
                    "", "", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getDbUrl() {
        return properties.getProperty("db.url", "Unknown");
    }

    public static String getDbUser() {
        return properties.getProperty("db.user", "Unknown");
    }

    /**
     * Value object encapsulating connection diagnostics.
     */
    public static class ConnectionTestResult {
        private final boolean success;
        private final String message;
        private final String databaseVersion;
        private final String databaseName;
        private final Exception exception;

        public ConnectionTestResult(boolean success, String message, String databaseVersion,
                                    String databaseName, Exception exception) {
            this.success = success;
            this.message = message;
            this.databaseVersion = databaseVersion;
            this.databaseName = databaseName;
            this.exception = exception;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getDatabaseVersion() {
            return databaseVersion;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public Exception getException() {
            return exception;
        }
    }
}


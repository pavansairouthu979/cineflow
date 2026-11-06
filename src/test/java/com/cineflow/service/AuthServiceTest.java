package com.cineflow.service;

import com.cineflow.dao.UserDAO;
import com.cineflow.model.Role;
import com.cineflow.model.User;
import com.cineflow.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit & Integration tests verifying Phase 2 Authentication,
 * Registration validation, salted hashing, and role checks.
 */
class AuthServiceTest {

    private AuthService authService;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        authService = new AuthService(userDAO);
        AuthService.logout();
    }

    @Test
    @DisplayName("Should successfully authenticate ADMIN with valid credentials")
    void testAdminLoginSuccess() throws AuthenticationException {
        User admin = authService.login("admin", "Admin@123");
        assertNotNull(admin, "Authenticated admin user must not be null");
        assertEquals("admin", admin.getUsername());
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.isActive());
        assertTrue(admin.isAdmin());
        assertEquals(admin, AuthService.getCurrentUser(), "Session user must match logged in admin");
    }

    @Test
    @DisplayName("Should successfully authenticate CUSTOMER with valid credentials")
    void testCustomerLoginSuccess() throws AuthenticationException {
        User customer = authService.login("john_doe", "Customer@123");
        assertNotNull(customer, "Authenticated customer user must not be null");
        assertEquals("john_doe", customer.getUsername());
        assertEquals(Role.CUSTOMER, customer.getRole());
        assertTrue(customer.isActive());
        assertFalse(customer.isAdmin());
        assertEquals(customer, AuthService.getCurrentUser());
    }

    @Test
    @DisplayName("Should reject authentication with an incorrect password")
    void testLoginWrongPassword() {
        AuthenticationException ex = assertThrows(
                AuthenticationException.class,
                () -> authService.login("admin", "WrongPassword999!")
        );
        assertTrue(ex.getMessage().contains("Invalid username or password"));
        assertNull(AuthService.getCurrentUser());
    }

    @Test
    @DisplayName("Should reject authentication for a non-existent username")
    void testLoginNonExistentUser() {
        AuthenticationException ex = assertThrows(
                AuthenticationException.class,
                () -> authService.login("unknown_user_99", "AnyPassword123")
        );
        assertTrue(ex.getMessage().contains("Invalid username or password"));
        assertNull(AuthService.getCurrentUser());
    }

    @Test
    @DisplayName("Should reject login when username or password is empty")
    void testLoginEmptyFields() {
        assertThrows(AuthenticationException.class, () -> authService.login("", "pass"));
        assertThrows(AuthenticationException.class, () -> authService.login("admin", "   "));
        assertThrows(AuthenticationException.class, () -> authService.login(null, "pass"));
    }

    @Test
    @DisplayName("Should reject authentication for inactive/deactivated account")
    void testLoginInactiveAccount() throws SQLException {
        String testInactiveUsername = "inact_" + (System.currentTimeMillis() % 10000000);
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword("InactivePass@123", salt);

        User inactiveUser = new User(
                testInactiveUsername, hash, salt,
                "Inactive Test", testInactiveUsername + "@example.com",
                Role.CUSTOMER, false // inactive
        );
        userDAO.createUser(inactiveUser);

        AuthenticationException ex = assertThrows(
                AuthenticationException.class,
                () -> authService.login(testInactiveUsername, "InactivePass@123")
        );
        assertTrue(ex.getMessage().contains("inactive"), "Expected inactive message, got: " + ex.getMessage());
    }

    @Test
    @DisplayName("Should register a new customer with salted hash and allow subsequent login")
    void testRegistrationSuccess() throws ValidationException, AuthenticationException {
        long suffix = System.currentTimeMillis() % 10000000;
        String regUsername = "user_" + suffix;
        String regEmail = "reg_" + suffix + "@example.com";
        String regPass = "SecretPass#123";

        User registered = authService.register(
                "Registered Customer",
                regUsername,
                regEmail,
                regPass,
                regPass
        );

        assertNotNull(registered);
        assertTrue(registered.getId() > 0, "Registered user must receive auto-generated primary key");
        assertEquals(regUsername, registered.getUsername());
        assertEquals(Role.CUSTOMER, registered.getRole());
        assertTrue(registered.isActive());

        // Verify salt and hash are stored
        assertNotNull(registered.getSalt());
        assertEquals(32, registered.getSalt().length(), "Salt should be 32 hex chars");
        assertNotNull(registered.getPasswordHash());
        assertEquals(64, registered.getPasswordHash().length(), "SHA-256 hash should be 64 hex chars");

        // Verify the newly registered user can authenticate immediately
        User loggedIn = authService.login(regUsername, regPass);
        assertEquals(registered.getId(), loggedIn.getId());
    }

    @Test
    @DisplayName("Should reject registration when username is already taken")
    void testRegistrationDuplicateUsername() {
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> authService.register(
                        "Duplicate User", "admin", "newemail@example.com",
                        "ValidPass123", "ValidPass123"
                )
        );
        assertTrue(ex.getMessage().contains("already taken"));
    }

    @Test
    @DisplayName("Should reject registration when email is already registered")
    void testRegistrationDuplicateEmail() {
        String uniqueUsername = "u_" + (System.currentTimeMillis() % 10000000);
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> authService.register(
                        "Duplicate Email User", uniqueUsername,
                        "admin@cineflow.com", "ValidPass123", "ValidPass123"
                )
        );
        assertTrue(ex.getMessage().contains("already registered"));
    }

    @Test
    @DisplayName("Should reject registration when passwords do not match")
    void testRegistrationPasswordMismatch() {
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> authService.register(
                        "Mismatch User", "mismatch_user", "mismatch@example.com",
                        "PasswordOne123", "PasswordTwo456"
                )
        );
        assertTrue(ex.getMessage().contains("Passwords do not match"));
    }

    @Test
    @DisplayName("Should reject registration when fields violate validation rules")
    void testRegistrationValidationRules() {
        // Full name too short
        assertThrows(ValidationException.class, () ->
                authService.register("A", "validuser", "user@test.com", "pass123", "pass123"));

        // Invalid username characters (spaces or special symbols)
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "user name!", "user@test.com", "pass123", "pass123"));

        // Invalid email format
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "validuser", "notanemail", "pass123", "pass123"));

        // Password too short (< 6 chars)
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "validuser", "user@test.com", "12345", "12345"));
    }

    @Test
    @DisplayName("Should verify PasswordUtil hashing and salt generation")
    void testPasswordUtil() {
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();
        assertNotEquals(salt1, salt2, "Subsequent salts must be random and unique");

        String hash1 = PasswordUtil.hashPassword("MyPassword123", salt1);
        String hash2 = PasswordUtil.hashPassword("MyPassword123", salt1);
        assertEquals(hash1, hash2, "Hashing same password and salt must be deterministic");

        assertTrue(PasswordUtil.verifyPassword("MyPassword123", salt1, hash1));
        assertFalse(PasswordUtil.verifyPassword("WrongPassword", salt1, hash1));
    }
}

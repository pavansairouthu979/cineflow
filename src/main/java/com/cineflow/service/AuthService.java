package com.cineflow.service;

import com.cineflow.dao.UserDAO;
import com.cineflow.model.Role;
import com.cineflow.model.User;
import com.cineflow.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service handling user authentication, registration, session management,
 * and credential validation business logic.
 */
public class AuthService {

    private final UserDAO userDAO;
    private static User currentUser = null;

    // Email format validation regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    // Username format validation regex pattern (letters, numbers, underscores, 3-20 characters)
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user given username and plain-text password.
     *
     * @param username user handle
     * @param password raw plain-text password
     * @return authenticated User entity
     * @throws AuthenticationException if credentials are invalid or account is disabled
     */
    public User login(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Password cannot be empty.");
        }

        String cleanUsername = username.trim();

        try {
            Optional<User> userOpt = userDAO.findByUsername(cleanUsername);

            if (userOpt.isEmpty()) {
                throw new AuthenticationException("Invalid username or password.");
            }

            User user = userOpt.get();

            // Check if the user account has been disabled
            if (!user.isActive()) {
                throw new AuthenticationException("This account is inactive. Please contact system administration.");
            }

            // Verify salted password hash
            boolean matches = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());
            if (!matches) {
                throw new AuthenticationException("Invalid username or password.");
            }

            // Set current session user
            currentUser = user;
            return user;

        } catch (SQLException e) {
            throw new AuthenticationException("Database error during authentication: " + e.getMessage(), e);
        }
    }

    /**
     * Registers a new customer in the system with comprehensive validation
     * and salted password hashing.
     *
     * @param fullName        customer's full name
     * @param username        unique username handle
     * @param email           unique email address
     * @param password        raw password
     * @param confirmPassword password confirmation
     * @return newly created User entity
     * @throws ValidationException if inputs fail validation or username/email is taken
     */
    public User register(String fullName, String username, String email,
                         String password, String confirmPassword) throws ValidationException {

        // 1. Validate Full Name
        if (fullName == null || fullName.trim().length() < 2) {
            throw new ValidationException("Full name must be at least 2 characters long.");
        }

        // 2. Validate Username
        if (username == null || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new ValidationException("Username must be 3-20 alphanumeric characters or underscores.");
        }

        // 3. Validate Email
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Please enter a valid email address (e.g., user@example.com).");
        }

        // 4. Validate Password Length
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }

        // 5. Validate Password Confirmation Match
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        String cleanUsername = username.trim();
        String cleanEmail = email.trim().toLowerCase();
        String cleanFullName = fullName.trim();

        try {
            // 6. Check for unique username
            if (userDAO.existsByUsername(cleanUsername)) {
                throw new ValidationException("Username '" + cleanUsername + "' is already taken.");
            }

            // 7. Check for unique email
            if (userDAO.existsByEmail(cleanEmail)) {
                throw new ValidationException("Email '" + cleanEmail + "' is already registered.");
            }

            // 8. Generate cryptographically secure 16-byte random salt
            String salt = PasswordUtil.generateSalt();

            // 9. Hash password via SHA-256(salt + password)
            String passwordHash = PasswordUtil.hashPassword(password, salt);

            // 10. Construct new Customer model
            User newUser = new User(cleanUsername, passwordHash, salt, cleanFullName,
                    cleanEmail, Role.CUSTOMER, true);

            // 11. Persist to database via PreparedStatement
            boolean created = userDAO.createUser(newUser);
            if (!created) {
                throw new ValidationException("Failed to create user account. Please try again.");
            }

            return newUser;

        } catch (SQLException e) {
            throw new ValidationException("Database error during registration: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the currently authenticated user in this session.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the active user for testing or session restoration.
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Checks if a user is currently logged in.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Checks if the currently logged-in user holds the ADMIN role.
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Clears the current user session (logout).
     */
    public static void logout() {
        currentUser = null;
    }
}

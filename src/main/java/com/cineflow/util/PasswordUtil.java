package com.cineflow.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility for cryptographically secure password hashing and verification.
 * Uses JDK SecureRandom for 16-byte salt generation and JDK MessageDigest
 * with SHA-256 for one-way salted password hashing.
 */
public class PasswordUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Generates a cryptographically strong 16-byte random salt,
     * encoded as a 32-character hexadecimal string.
     *
     * @return 32-character hex salt string
     */
    public static String generateSalt() {
        byte[] saltBytes = new byte[16];
        SECURE_RANDOM.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    /**
     * Computes the SHA-256 hash of a password concatenated with a salt.
     * Formula: SHA-256(salt + password)
     *
     * @param password raw plain-text password
     * @param salt     hexadecimal salt string
     * @return 64-character lowercase hexadecimal hash
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt cannot be null.");
        }
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            // Prepend the salt to defend against dictionary and rainbow table attacks
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JDK runtime.", e);
        }
    }

    /**
     * Verifies whether a candidate plain-text password matches the stored salted hash.
     *
     * @param plainPassword raw candidate password entered by user
     * @param salt          hexadecimal salt stored in the database
     * @param expectedHash  hexadecimal hash stored in the database
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String salt, String expectedHash) {
        if (plainPassword == null || salt == null || expectedHash == null) {
            return false;
        }
        String computedHash = hashPassword(plainPassword, salt);
        return computedHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * Converts a byte array to a lowercase hexadecimal string.
     *
     * @param bytes byte array to convert
     * @return hexadecimal representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}


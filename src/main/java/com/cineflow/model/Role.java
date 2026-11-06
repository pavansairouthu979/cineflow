package com.cineflow.model;

/**
 * Enumeration representing supported user roles within CineFlow.
 */
public enum Role {
    ADMIN,
    CUSTOMER;

    /**
     * Converts a database role string to a Role enum safely.
     *
     * @param roleString string value from database
     * @return corresponding Role enum, defaults to CUSTOMER
     */
    public static Role fromString(String roleString) {
        if (roleString == null) {
            return CUSTOMER;
        }
        try {
            return Role.valueOf(roleString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CUSTOMER;
        }
    }
}


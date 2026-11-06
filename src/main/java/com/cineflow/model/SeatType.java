package com.cineflow.model;

/**
 * Enumeration of seat tiers available in auditoriums.
 */
public enum SeatType {
    REGULAR,
    PREMIUM;

    public static SeatType fromString(String type) {
        if (type == null) return REGULAR;
        try {
            return SeatType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return REGULAR;
        }
    }
}


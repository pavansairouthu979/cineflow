package com.cineflow.model;

/**
 * Enumeration representing booking lifecycle states.
 */
public enum BookingStatus {
    CONFIRMED,
    CANCELLED;

    public static BookingStatus fromString(String status) {
        if (status == null) return CONFIRMED;
        try {
            return BookingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CONFIRMED;
        }
    }
}

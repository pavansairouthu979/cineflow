package com.cineflow.model;

/**
 * Enumeration representing simulated payment methods.
 */
public enum PaymentMethod {
    CASH,
    UPI,
    CARD;

    public static PaymentMethod fromString(String method) {
        if (method == null) return CARD;
        try {
            return PaymentMethod.valueOf(method.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CARD;
        }
    }
}

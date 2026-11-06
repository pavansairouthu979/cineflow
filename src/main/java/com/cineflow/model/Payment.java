package com.cineflow.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model representing a payment transaction record.
 */
public class Payment {
    private int id;
    private int bookingId;
    private PaymentMethod method;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paidAt;

    public Payment() {
        this.status = PaymentStatus.PAID;
    }

    public Payment(int bookingId, PaymentMethod method, BigDecimal amount, PaymentStatus status) {
        this.bookingId = bookingId;
        this.method = method;
        this.amount = amount;
        this.status = status != null ? status : PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public Payment(int id, int bookingId, PaymentMethod method, BigDecimal amount,
                   PaymentStatus status, LocalDateTime paidAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.method = method;
        this.amount = amount;
        this.status = status != null ? status : PaymentStatus.PAID;
        this.paidAt = paidAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}


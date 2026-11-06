package com.cineflow.model;

import java.math.BigDecimal;

/**
 * Line item model connecting a seat to a booking.
 */
public class BookingSeat {
    private int id;
    private int bookingId;
    private int showtimeId;
    private int seatId;
    private BigDecimal price;

    public BookingSeat() {}

    public BookingSeat(int bookingId, int showtimeId, int seatId, BigDecimal price) {
        this.bookingId = bookingId;
        this.showtimeId = showtimeId;
        this.seatId = seatId;
        this.price = price;
    }

    public BookingSeat(int id, int bookingId, int showtimeId, int seatId, BigDecimal price) {
        this.id = id;
        this.bookingId = bookingId;
        this.showtimeId = showtimeId;
        this.seatId = seatId;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getShowtimeId() { return showtimeId; }
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}


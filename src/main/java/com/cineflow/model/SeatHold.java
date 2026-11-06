package com.cineflow.model;

import java.time.LocalDateTime;

/**
 * Model representing a temporary 5-minute seat hold during customer checkout.
 */
public class SeatHold {
    private int id;
    private int showtimeId;
    private int seatId;
    private int userId;
    private LocalDateTime heldUntil;

    public SeatHold() {}

    public SeatHold(int showtimeId, int seatId, int userId, LocalDateTime heldUntil) {
        this.showtimeId = showtimeId;
        this.seatId = seatId;
        this.userId = userId;
        this.heldUntil = heldUntil;
    }

    public SeatHold(int id, int showtimeId, int seatId, int userId, LocalDateTime heldUntil) {
        this.id = id;
        this.showtimeId = showtimeId;
        this.seatId = seatId;
        this.userId = userId;
        this.heldUntil = heldUntil;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShowtimeId() { return showtimeId; }
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getHeldUntil() { return heldUntil; }
    public void setHeldUntil(LocalDateTime heldUntil) { this.heldUntil = heldUntil; }

    public boolean isExpired() {
        return heldUntil != null && LocalDateTime.now().isAfter(heldUntil);
    }
}

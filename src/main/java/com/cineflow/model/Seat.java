package com.cineflow.model;

/**
 * Model representing a physical seat inside an auditorium.
 */
public class Seat {
    private int id;
    private int screenId;
    private String rowLabel;
    private int seatNumber;
    private SeatType seatType;

    public Seat() {
        this.seatType = SeatType.REGULAR;
    }

    public Seat(int screenId, String rowLabel, int seatNumber, SeatType seatType) {
        this.screenId = screenId;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.seatType = seatType != null ? seatType : SeatType.REGULAR;
    }

    public Seat(int id, int screenId, String rowLabel, int seatNumber, SeatType seatType) {
        this.id = id;
        this.screenId = screenId;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.seatType = seatType != null ? seatType : SeatType.REGULAR;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getScreenId() { return screenId; }
    public void setScreenId(int screenId) { this.screenId = screenId; }

    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }

    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }

    public String getSeatCode() {
        return rowLabel + seatNumber;
    }

    @Override
    public String toString() {
        return getSeatCode() + " (" + seatType + ")";
    }
}

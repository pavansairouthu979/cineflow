package com.cineflow.model;

/**
 * Model class representing an auditorium/screen in the multiplex.
 */
public class Screen {
    private int id;
    private String name;
    private int totalRows;
    private int seatsPerRow;

    public Screen() {}

    public Screen(String name, int totalRows, int seatsPerRow) {
        this.name = name;
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
    }

    public Screen(int id, String name, int totalRows, int seatsPerRow) {
        this.id = id;
        this.name = name;
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(int seatsPerRow) { this.seatsPerRow = seatsPerRow; }

    public int getTotalSeats() { return totalRows * seatsPerRow; }

    @Override
    public String toString() {
        return name + " (" + getTotalSeats() + " seats)";
    }
}


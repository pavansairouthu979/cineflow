package com.cineflow.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO representing showtime occupancy metrics for reports.
 */
public class OccupancyReportItem {
    private int showtimeId;
    private String movieTitle;
    private String screenName;
    private LocalDateTime startTime;
    private int bookedSeats;
    private int totalSeats;
    private double occupancyPercentage;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public OccupancyReportItem(int showtimeId, String movieTitle, String screenName,
                               LocalDateTime startTime, int bookedSeats, int totalSeats) {
        this.showtimeId = showtimeId;
        this.movieTitle = movieTitle;
        this.screenName = screenName;
        this.startTime = startTime;
        this.bookedSeats = bookedSeats;
        this.totalSeats = totalSeats;
        this.occupancyPercentage = totalSeats > 0 ? ((double) bookedSeats / totalSeats) * 100.0 : 0.0;
    }

    public int getShowtimeId() { return showtimeId; }
    public String getMovieTitle() { return movieTitle; }
    public String getScreenName() { return screenName; }
    public LocalDateTime getStartTime() { return startTime; }
    public int getBookedSeats() { return bookedSeats; }
    public int getTotalSeats() { return totalSeats; }
    public double getOccupancyPercentage() { return occupancyPercentage; }

    public String getFormattedStartTime() {
        return startTime != null ? startTime.format(FORMATTER) : "";
    }

    public String getFormattedOccupancy() {
        return String.format("%.1f%% (%d/%d)", occupancyPercentage, bookedSeats, totalSeats);
    }
}


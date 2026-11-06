package com.cineflow.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a scheduled Movie Screening in a Screen.
 */
public class Showtime {
    private int id;
    private int movieId;
    private int screenId;
    private LocalDateTime startTime;
    private BigDecimal priceRegular;
    private BigDecimal pricePremium;

    // Joined fields for display
    private String movieTitle;
    private String screenName;
    private int durationMinutes;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Showtime() {}

    public Showtime(int movieId, int screenId, LocalDateTime startTime,
                    BigDecimal priceRegular, BigDecimal pricePremium) {
        this.movieId = movieId;
        this.screenId = screenId;
        this.startTime = startTime;
        this.priceRegular = priceRegular;
        this.pricePremium = pricePremium;
    }

    public Showtime(int id, int movieId, int screenId, LocalDateTime startTime,
                    BigDecimal priceRegular, BigDecimal pricePremium) {
        this.id = id;
        this.movieId = movieId;
        this.screenId = screenId;
        this.startTime = startTime;
        this.priceRegular = priceRegular;
        this.pricePremium = pricePremium;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public int getScreenId() { return screenId; }
    public void setScreenId(int screenId) { this.screenId = screenId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public BigDecimal getPriceRegular() { return priceRegular; }
    public void setPriceRegular(BigDecimal priceRegular) { this.priceRegular = priceRegular; }

    public BigDecimal getPricePremium() { return pricePremium; }
    public void setPricePremium(BigDecimal pricePremium) { this.pricePremium = pricePremium; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public LocalDateTime getEndTime() {
        if (startTime == null) return null;
        return startTime.plusMinutes(durationMinutes > 0 ? durationMinutes : 120);
    }

    public String getFormattedStartTime() {
        return startTime != null ? startTime.format(FORMATTER) : "";
    }

    @Override
    public String toString() {
        return (movieTitle != null ? movieTitle : "Movie #" + movieId) + " @ " +
                (screenName != null ? screenName : "Screen #" + screenId) + " - " +
                getFormattedStartTime();
    }
}

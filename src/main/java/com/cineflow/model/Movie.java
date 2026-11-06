package com.cineflow.model;

import java.time.LocalDate;

/**
 * Model class representing a Movie in the catalog.
 */
public class Movie {
    private int id;
    private String title;
    private String genre;
    private String language;
    private int durationMinutes;
    private double rating;
    private String description;
    private LocalDate releaseDate;
    private boolean active;

    public Movie() {
        this.active = true;
    }

    public Movie(String title, String genre, String language, int durationMinutes,
                 double rating, String description, LocalDate releaseDate, boolean active) {
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
        this.description = description;
        this.releaseDate = releaseDate;
        this.active = active;
    }

    public Movie(int id, String title, String genre, String language, int durationMinutes,
                 double rating, String description, LocalDate releaseDate, boolean active) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
        this.description = description;
        this.releaseDate = releaseDate;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return title + " (" + language + " • " + durationMinutes + "m)";
    }
}


package com.cineflow.service;

import com.cineflow.dao.MovieDAO;
import com.cineflow.dao.ShowtimeDAO;
import com.cineflow.model.Movie;
import com.cineflow.model.Showtime;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service managing screening schedules and conflict/overlap validation.
 */
public class ShowtimeService {

    private final ShowtimeDAO showtimeDAO;
    private final MovieDAO movieDAO;

    public ShowtimeService() {
        this.showtimeDAO = new ShowtimeDAO();
        this.movieDAO = new MovieDAO();
    }

    public ShowtimeService(ShowtimeDAO showtimeDAO, MovieDAO movieDAO) {
        this.showtimeDAO = showtimeDAO;
        this.movieDAO = movieDAO;
    }

    public void scheduleShowtime(Showtime showtime) throws ValidationException, SQLException {
        validateShowtime(showtime);
        showtimeDAO.create(showtime);
    }

    public void updateShowtime(Showtime showtime) throws ValidationException, SQLException {
        validateShowtime(showtime);
        showtimeDAO.update(showtime);
    }

    public void deleteShowtime(int id) throws SQLException {
        showtimeDAO.delete(id);
    }

    public List<Showtime> getAllShowtimes() throws SQLException {
        return showtimeDAO.findAll();
    }

    public List<Showtime> getUpcomingShowtimes() throws SQLException {
        return showtimeDAO.findUpcoming();
    }

    public List<Showtime> getShowtimesForMovie(int movieId) throws SQLException {
        return showtimeDAO.findByMovieId(movieId);
    }

    public Optional<Showtime> getShowtimeById(int id) throws SQLException {
        return showtimeDAO.findById(id);
    }

    private void validateShowtime(Showtime showtime) throws ValidationException, SQLException {
        if (showtime == null) {
            throw new ValidationException("Showtime cannot be null.");
        }
        if (showtime.getMovieId() <= 0) {
            throw new ValidationException("Please select a valid movie.");
        }
        if (showtime.getScreenId() <= 0) {
            throw new ValidationException("Please select a valid auditorium/screen.");
        }
        if (showtime.getStartTime() == null) {
            throw new ValidationException("Showtime start time is required.");
        }
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Cannot schedule showtimes in the past.");
        }
        if (showtime.getPriceRegular() == null || showtime.getPriceRegular().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Regular seat price must be greater than zero.");
        }
        if (showtime.getPricePremium() == null || showtime.getPricePremium().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Premium seat price must be greater than zero.");
        }
        if (showtime.getPricePremium().compareTo(showtime.getPriceRegular()) < 0) {
            throw new ValidationException("Premium price cannot be less than regular price.");
        }

        // Fetch movie to determine duration for overlap calculation
        Optional<Movie> movieOpt = movieDAO.findById(showtime.getMovieId());
        if (movieOpt.isEmpty() || !movieOpt.get().isActive()) {
            throw new ValidationException("Selected movie does not exist or is inactive.");
        }

        int duration = movieOpt.get().getDurationMinutes();
        LocalDateTime endTime = showtime.getStartTime().plusMinutes(duration);

        // Check for conflicting overlaps on the same screen
        Integer excludeId = showtime.getId() > 0 ? showtime.getId() : null;
        boolean overlap = showtimeDAO.hasOverlap(showtime.getScreenId(), showtime.getStartTime(), endTime, excludeId);
        if (overlap) {
            throw new ValidationException("Showtime conflict: Another screening is already scheduled on this screen during this time window.");
        }
    }
}

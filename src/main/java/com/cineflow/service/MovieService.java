package com.cineflow.service;

import com.cineflow.dao.MovieDAO;
import com.cineflow.model.Movie;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service managing movie catalog business logic and validation.
 */
public class MovieService {

    private final MovieDAO movieDAO;

    public MovieService() {
        this.movieDAO = new MovieDAO();
    }

    public MovieService(MovieDAO movieDAO) {
        this.movieDAO = movieDAO;
    }

    public void saveMovie(Movie movie) throws ValidationException, SQLException {
        validateMovie(movie);
        if (movie.getId() > 0) {
            movieDAO.update(movie);
        } else {
            movieDAO.create(movie);
        }
    }

    public void deleteMovie(int id) throws SQLException {
        movieDAO.delete(id);
    }

    public void toggleActive(int id, boolean active) throws SQLException {
        movieDAO.setActive(id, active);
    }

    public Optional<Movie> getMovieById(int id) throws SQLException {
        return movieDAO.findById(id);
    }

    public List<Movie> getAllMovies() throws SQLException {
        return movieDAO.findAll();
    }

    public List<Movie> getActiveMovies() throws SQLException {
        return movieDAO.findActive();
    }

    public List<Movie> searchMovies(String query, String genre, String language) throws SQLException {
        return movieDAO.search(query, genre, language);
    }

    private void validateMovie(Movie movie) throws ValidationException {
        if (movie == null) {
            throw new ValidationException("Movie object cannot be null.");
        }
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            throw new ValidationException("Movie title is required.");
        }
        if (movie.getGenre() == null || movie.getGenre().trim().isEmpty()) {
            throw new ValidationException("Genre is required.");
        }
        if (movie.getLanguage() == null || movie.getLanguage().trim().isEmpty()) {
            throw new ValidationException("Language is required.");
        }
        if (movie.getDurationMinutes() <= 0) {
            throw new ValidationException("Duration must be greater than 0 minutes.");
        }
        if (movie.getRating() < 0.0 || movie.getRating() > 10.0) {
            throw new ValidationException("Rating must be between 0.0 and 10.0.");
        }
        if (movie.getReleaseDate() == null) {
            throw new ValidationException("Release date is required.");
        }
    }
}

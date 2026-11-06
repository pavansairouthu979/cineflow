package com.cineflow.service;

import com.cineflow.dao.ScreenDAO;
import com.cineflow.dao.SeatDAO;
import com.cineflow.model.Screen;
import com.cineflow.model.Seat;
import com.cineflow.model.SeatType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing auditorium screens and automatic seat layout generation.
 */
public class ScreenService {

    private final ScreenDAO screenDAO;
    private final SeatDAO seatDAO;

    public ScreenService() {
        this.screenDAO = new ScreenDAO();
        this.seatDAO = new SeatDAO();
    }

    public ScreenService(ScreenDAO screenDAO, SeatDAO seatDAO) {
        this.screenDAO = screenDAO;
        this.seatDAO = seatDAO;
    }

    /**
     * Adds a new screen and automatically generates the complete seat matrix.
     */
    public Screen createScreenWithSeats(String name, int totalRows, int seatsPerRow) throws ValidationException, SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Screen name cannot be empty.");
        }
        if (totalRows < 1 || totalRows > 26) {
            throw new ValidationException("Rows must be between 1 and 26.");
        }
        if (seatsPerRow < 1 || seatsPerRow > 30) {
            throw new ValidationException("Seats per row must be between 1 and 30.");
        }
        if (screenDAO.existsByName(name.trim())) {
            throw new ValidationException("A screen with name '" + name.trim() + "' already exists.");
        }

        Screen screen = new Screen(name.trim(), totalRows, seatsPerRow);
        boolean created = screenDAO.create(screen);
        if (!created || screen.getId() <= 0) {
            throw new SQLException("Failed to create screen record.");
        }

        // Generate physical seats (e.g. Rows A to Z)
        // First 60% of rows are REGULAR, back 40% are PREMIUM
        int regularRowLimit = Math.max(1, (int) Math.ceil(totalRows * 0.6));
        List<Seat> seatsToInsert = new ArrayList<>();

        for (int r = 0; r < totalRows; r++) {
            char rowChar = (char) ('A' + r);
            String rowLabel = String.valueOf(rowChar);
            SeatType type = (r < regularRowLimit) ? SeatType.REGULAR : SeatType.PREMIUM;

            for (int s = 1; s <= seatsPerRow; s++) {
                seatsToInsert.add(new Seat(screen.getId(), rowLabel, s, type));
            }
        }

        seatDAO.createSeatsBatch(seatsToInsert);
        return screen;
    }

    public List<Screen> getAllScreens() throws SQLException {
        return screenDAO.findAll();
    }

    public List<Seat> getSeatsForScreen(int screenId) throws SQLException {
        return seatDAO.findByScreenId(screenId);
    }
}


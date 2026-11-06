package com.cineflow.service;

import com.cineflow.dao.BookingDAO;
import com.cineflow.dao.SeatHoldDAO;
import com.cineflow.model.Booking;
import com.cineflow.model.BookingStatus;
import com.cineflow.model.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service managing temporary seat holds, ACID booking transactions,
 * and customer ticket cancellations.
 */
public class BookingService {

    private final BookingDAO bookingDAO;
    private final SeatHoldDAO seatHoldDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.seatHoldDAO = new SeatHoldDAO();
    }

    public BookingService(BookingDAO bookingDAO, SeatHoldDAO seatHoldDAO) {
        this.bookingDAO = bookingDAO;
        this.seatHoldDAO = seatHoldDAO;
    }

    /**
     * Reserves temporary 5-minute holds for selected seats.
     */
    public void holdSeats(int showtimeId, List<Integer> seatIds, int userId) throws ValidationException, SQLException {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new ValidationException("Please select at least one seat.");
        }

        LocalDateTime heldUntil = LocalDateTime.now().plusMinutes(5);

        // Check if any seat is already booked or held by another user
        List<Integer> bookedSeatIds = bookingDAO.getBookedSeatIdsForShowtime(showtimeId);
        for (int seatId : seatIds) {
            if (bookedSeatIds.contains(seatId)) {
                throw new ValidationException("One or more selected seats have already been booked.");
            }
            if (seatHoldDAO.isSeatHeldByOther(showtimeId, seatId, userId)) {
                throw new ValidationException("One or more selected seats are currently on hold by another customer.");
            }
        }

        // Apply holds
        for (int seatId : seatIds) {
            seatHoldDAO.createHold(showtimeId, seatId, userId, heldUntil);
        }
    }

    /**
     * Releases active holds for a specific user and showtime (e.g. upon checkout cancellation).
     */
    public void releaseUserHolds(int showtimeId, int userId) {
        try {
            seatHoldDAO.deleteHoldsForUser(showtimeId, userId);
        } catch (SQLException e) {
            System.err.println("Could not release user holds: " + e.getMessage());
        }
    }

    /**
     * Executes the comprehensive JDBC booking transaction with SELECT ... FOR UPDATE.
     */
    public boolean confirmBooking(Booking booking, List<Integer> seatIds,
                                  List<BigDecimal> seatPrices, Payment payment) throws SQLException {
        return bookingDAO.executeBookingTransaction(booking, seatIds, seatPrices, payment);
    }

    /**
     * Cancels a booking if the showtime has not yet started.
     */
    public boolean cancelBooking(int bookingId, int userId) throws SQLException {
        return bookingDAO.cancelBookingTransaction(bookingId, userId);
    }

    public List<Integer> getBookedSeats(int showtimeId) throws SQLException {
        return bookingDAO.getBookedSeatIdsForShowtime(showtimeId);
    }

    public List<Integer> getHeldSeatsByOthers(int showtimeId, int currentUserId) throws SQLException {
        return seatHoldDAO.getActiveHeldSeatIds(showtimeId, currentUserId);
    }

    public List<Booking> getCustomerBookings(int userId) throws SQLException {
        return bookingDAO.findByUserId(userId);
    }

    public List<Booking> getFilteredAdminBookings(LocalDate start, LocalDate end,
                                                 Integer movieId, BookingStatus status, String username) throws SQLException {
        return bookingDAO.findAllJoined(start, end, movieId, status, username);
    }
}


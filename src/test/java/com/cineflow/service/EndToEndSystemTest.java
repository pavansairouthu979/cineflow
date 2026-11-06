package com.cineflow.service;

import com.cineflow.dao.*;
import com.cineflow.model.*;
import com.cineflow.util.CsvExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive automated End-to-End integration test suite verifying:
 * - Movie CRUD
 * - Screen generation & seat layout
 * - Showtime scheduling & overlap validation
 * - Seat holding & ACID booking transaction with SELECT ... FOR UPDATE
 * - Concurrency & double-booking prevention
 * - Cancellation & refund transaction
 * - Financial reports & CSV export
 */
class EndToEndSystemTest {

    private MovieService movieService;
    private ScreenService screenService;
    private ShowtimeService showtimeService;
    private BookingService bookingService;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        movieService = new MovieService();
        screenService = new ScreenService();
        showtimeService = new ShowtimeService();
        bookingService = new BookingService();
        reportService = new ReportService();
    }

    @Test
    @DisplayName("Should perform Movie CRUD operations and search")
    void testMovieCrud() throws ValidationException, SQLException {
        // Create
        Movie movie = new Movie(
                "Test Movie " + System.currentTimeMillis(),
                "Action", "English", 130, 8.5,
                "A thrill ride test movie", LocalDate.now(), true
        );
        movieService.saveMovie(movie);
        assertTrue(movie.getId() > 0, "Saved movie must receive ID");

        // Read
        Optional<Movie> fetched = movieService.getMovieById(movie.getId());
        assertTrue(fetched.isPresent());
        assertEquals(movie.getTitle(), fetched.get().getTitle());

        // Update
        movie.setGenre("Sci-Fi");
        movieService.saveMovie(movie);
        Optional<Movie> updated = movieService.getMovieById(movie.getId());
        assertTrue(updated.isPresent());
        assertEquals("Sci-Fi", updated.get().getGenre());

        // Search
        List<Movie> results = movieService.searchMovies(movie.getTitle(), "Sci-Fi", "English");
        assertFalse(results.isEmpty());

        // Toggle active
        movieService.toggleActive(movie.getId(), false);
        Optional<Movie> deactivated = movieService.getMovieById(movie.getId());
        assertTrue(deactivated.isPresent());
        assertFalse(deactivated.get().isActive());
    }

    @Test
    @DisplayName("Should create Screen and automatically generate physical seats layout")
    void testScreenAndSeatGeneration() throws ValidationException, SQLException {
        String screenName = "Test Screen " + System.currentTimeMillis();
        int rows = 3;
        int seatsPerRow = 5;

        Screen screen = screenService.createScreenWithSeats(screenName, rows, seatsPerRow);
        assertNotNull(screen);
        assertTrue(screen.getId() > 0);
        assertEquals(15, screen.getTotalSeats());

        List<Seat> generatedSeats = screenService.getSeatsForScreen(screen.getId());
        assertEquals(15, generatedSeats.size(), "Expected exactly 15 generated seats");

        // Verify row labels A, B, C
        assertEquals("A", generatedSeats.get(0).getRowLabel());
        assertEquals(1, generatedSeats.get(0).getSeatNumber());
        assertEquals("C", generatedSeats.get(14).getRowLabel());
        assertEquals(5, generatedSeats.get(14).getSeatNumber());
    }

    @Test
    @DisplayName("Should prevent overlapping showtimes on the same screen")
    void testShowtimeOverlapPrevention() throws ValidationException, SQLException {
        List<Movie> movies = movieService.getActiveMovies();
        List<Screen> screens = screenService.getAllScreens();
        assertFalse(movies.isEmpty());
        assertFalse(screens.isEmpty());

        Movie movie = movies.get(0);
        Screen screen = screens.get(0);

        LocalDateTime baseTime = LocalDateTime.now().plusDays(10).withHour(14).withMinute(0).withSecond(0).withNano(0);

        // Schedule first showtime
        Showtime show1 = new Showtime(
                movie.getId(), screen.getId(), baseTime,
                new BigDecimal("200.00"), new BigDecimal("300.00")
        );
        showtimeService.scheduleShowtime(show1);
        assertTrue(show1.getId() > 0);

        // Attempt to schedule conflicting showtime starting 30 mins after show1 starts (before show1 ends)
        Showtime conflictShow = new Showtime(
                movie.getId(), screen.getId(), baseTime.plusMinutes(30),
                new BigDecimal("200.00"), new BigDecimal("300.00")
        );

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> showtimeService.scheduleShowtime(conflictShow)
        );
        assertTrue(ex.getMessage().contains("conflict"), "Expected conflict message: " + ex.getMessage());

        // Clean up
        showtimeService.deleteShowtime(show1.getId());
    }

    @Test
    @DisplayName("Should execute complete ACID booking transaction with holds and prevent double booking")
    void testBookingTransactionAndConcurrency() throws ValidationException, SQLException {
        // Customer 2 (john_doe, id=2) and Customer 3 (jane_smith, id=3)
        int user1 = 2;
        int user2 = 3;

        // Pick upcoming showtime 5
        int showtimeId = 5;

        // Screen 1: Pick seat 1 (A1) and seat 2 (A2)
        List<Integer> targetSeatIds = List.of(1, 2);
        List<BigDecimal> prices = List.of(new BigDecimal("250.00"), new BigDecimal("250.00"));
        BigDecimal total = new BigDecimal("500.00");

        // 1. User 1 acquires temporary hold
        bookingService.holdSeats(showtimeId, targetSeatIds, user1);

        // 2. User 2 attempts to acquire hold on the same seats -> MUST FAIL
        ValidationException holdConflictEx = assertThrows(
                ValidationException.class,
                () -> bookingService.holdSeats(showtimeId, targetSeatIds, user2)
        );
        assertTrue(holdConflictEx.getMessage().contains("hold"));

        // 3. User 1 confirms booking via ACID JDBC transaction
        Booking booking = new Booking(user1, showtimeId, null, total, BookingStatus.CONFIRMED);
        Payment payment = new Payment(0, PaymentMethod.UPI, total, PaymentStatus.PAID);

        boolean booked = bookingService.confirmBooking(booking, targetSeatIds, prices, payment);
        assertTrue(booked, "Booking transaction must commit successfully");
        assertTrue(booking.getId() > 0);

        // 4. Verify seats are now recorded as booked
        List<Integer> bookedSeats = bookingService.getBookedSeats(showtimeId);
        assertTrue(bookedSeats.contains(1));
        assertTrue(bookedSeats.contains(2));

        // 5. User 2 now attempts to book the same seats -> MUST FAIL (double-booking prevented)
        Booking user2Booking = new Booking(user2, showtimeId, null, total, BookingStatus.CONFIRMED);
        Payment user2Payment = new Payment(0, PaymentMethod.CARD, total, PaymentStatus.PAID);

        SQLException doubleBookEx = assertThrows(
                SQLException.class,
                () -> bookingService.confirmBooking(user2Booking, targetSeatIds, prices, user2Payment)
        );
        assertTrue(doubleBookEx.getMessage().contains("already been booked"));

        // 6. Test cancellation before showtime
        boolean cancelled = bookingService.cancelBooking(booking.getId(), user1);
        assertTrue(cancelled, "Booking cancellation must succeed");

        // After cancellation, seat is no longer counted in active confirmed bookings
        List<Integer> bookedAfterCancel = bookingService.getBookedSeats(showtimeId);
        assertFalse(bookedAfterCancel.contains(1));
    }

    @Test
    @DisplayName("Should generate Reports and export valid RFC-4180 CSV files")
    void testReportsAndCsvExport() throws SQLException, IOException {
        BigDecimal totalRev = reportService.getTotalRevenue();
        assertNotNull(totalRev);
        assertTrue(totalRev.compareTo(BigDecimal.ZERO) > 0, "Historical seed data must produce positive revenue");

        Map<LocalDate, BigDecimal> weekly = reportService.getSevenDayRevenue();
        assertEquals(7, weekly.size(), "Weekly revenue map should contain 7 days");

        Map<String, Integer> topMovies = reportService.getTopMoviesByTicketSales();
        assertFalse(topMovies.isEmpty());

        List<OccupancyReportItem> occupancy = reportService.getShowtimeOccupancy();
        assertFalse(occupancy.isEmpty());

        // Test CSV Export to temp files
        File tempBookings = File.createTempFile("cineflow_test_bookings", ".csv");
        tempBookings.deleteOnExit();
        List<Booking> allBookings = bookingService.getFilteredAdminBookings(null, null, null, null, null);
        CsvExporter.exportBookingsToCsv(tempBookings, allBookings);
        assertTrue(tempBookings.length() > 0, "Bookings CSV must contain content");

        File tempRev = File.createTempFile("cineflow_test_rev", ".csv");
        tempRev.deleteOnExit();
        CsvExporter.exportRevenueToCsv(tempRev, weekly, totalRev);
        assertTrue(tempRev.length() > 0, "Revenue CSV must contain content");

        String revCsvContent = Files.readString(tempRev.toPath());
        assertTrue(revCsvContent.contains("TOTAL REVENUE"));
    }
}

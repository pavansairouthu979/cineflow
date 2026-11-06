package com.cineflow.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a ticket reservation booking.
 */
public class Booking {
    private int id;
    private int userId;
    private int showtimeId;
    private LocalDateTime bookingTime;
    private BigDecimal totalAmount;
    private BookingStatus status;

    // Joined fields for display in UI tables & tickets
    private String username;
    private String customerName;
    private String movieTitle;
    private String screenName;
    private LocalDateTime showtimeStart;
    private String seatNumbers;
    private PaymentMethod paymentMethod;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Booking() {
        this.status = BookingStatus.CONFIRMED;
    }

    public Booking(int userId, int showtimeId, LocalDateTime bookingTime,
                   BigDecimal totalAmount, BookingStatus status) {
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.bookingTime = bookingTime;
        this.totalAmount = totalAmount;
        this.status = status != null ? status : BookingStatus.CONFIRMED;
    }

    public Booking(int id, int userId, int showtimeId, LocalDateTime bookingTime,
                   BigDecimal totalAmount, BookingStatus status) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.bookingTime = bookingTime;
        this.totalAmount = totalAmount;
        this.status = status != null ? status : BookingStatus.CONFIRMED;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getShowtimeId() { return showtimeId; }
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }

    public LocalDateTime getShowtimeStart() { return showtimeStart; }
    public void setShowtimeStart(LocalDateTime showtimeStart) { this.showtimeStart = showtimeStart; }

    public String getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(String seatNumbers) { this.seatNumbers = seatNumbers; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getFormattedBookingTime() {
        return bookingTime != null ? bookingTime.format(FORMATTER) : "";
    }

    public String getFormattedShowtimeStart() {
        return showtimeStart != null ? showtimeStart.format(FORMATTER) : "";
    }

    public boolean canCancel() {
        return status == BookingStatus.CONFIRMED &&
               showtimeStart != null &&
               LocalDateTime.now().isBefore(showtimeStart);
    }
}

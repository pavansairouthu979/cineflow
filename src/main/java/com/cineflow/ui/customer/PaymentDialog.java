package com.cineflow.ui.customer;

import com.cineflow.model.*;
import com.cineflow.service.AuthService;
import com.cineflow.service.BookingService;
import com.cineflow.util.AlertHelper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Payment confirmation dialog with a 5-minute JavaFX Timeline countdown
 * and simulated payment processing through the ACID JDBC booking transaction.
 */
public class PaymentDialog {

    private final Showtime showtime;
    private final List<Seat> selectedSeats;
    private final BookingService bookingService;
    private final Consumer<Booking> onBookingSuccessful;

    private Stage dialogStage;
    private Timeline countdownTimeline;
    private int remainingSeconds = 300; // 5 minutes = 300 seconds
    private Label timerLabel;
    private ToggleGroup paymentGroup;

    public PaymentDialog(Showtime showtime, List<Seat> selectedSeats, Consumer<Booking> onBookingSuccessful) {
        this.showtime = showtime;
        this.selectedSeats = selectedSeats;
        this.bookingService = new BookingService();
        this.onBookingSuccessful = onBookingSuccessful;
    }

    public void show() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("CineFlow - Payment & Checkout");
        dialogStage.setResizable(false);

        VBox content = new VBox(16);
        content.getStyleClass().add("card");
        content.setPadding(new Insets(24));
        content.setPrefWidth(480);

        // Header
        Label title = new Label("Confirm Movie Reservation");
        title.getStyleClass().add("title-medium");

        // 5-Minute Countdown Timer Banner
        HBox timerBox = new HBox(10);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.setStyle("-fx-background-color: rgba(245, 158, 11, 0.15); -fx-padding: 10px; -fx-background-radius: 8px; -fx-border-color: #f59e0b; -fx-border-width: 1px; -fx-border-radius: 8px;");

        Label timerIcon = new Label("⏳");
        timerIcon.setStyle("-fx-font-size: 16px;");

        timerLabel = new Label("Seats reserved for: 05:00");
        timerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #fbbf24;");

        timerBox.getChildren().addAll(timerIcon, timerLabel);

        // Booking Summary Grid
        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(12);
        summaryGrid.setVgap(8);
        summaryGrid.setStyle("-fx-background-color: #0d1525; -fx-padding: 14px; -fx-background-radius: 8px; -fx-border-color: #1e293b; -fx-border-width: 1px; -fx-border-radius: 8px;");

        addSummaryRow(summaryGrid, 0, "Movie:", showtime.getMovieTitle());
        addSummaryRow(summaryGrid, 1, "Auditorium:", showtime.getScreenName());
        addSummaryRow(summaryGrid, 2, "Showtime:", showtime.getFormattedStartTime());

        StringBuilder seatCodes = new StringBuilder();
        BigDecimal total = BigDecimal.ZERO;
        List<BigDecimal> seatPrices = new ArrayList<>();

        for (int i = 0; i < selectedSeats.size(); i++) {
            Seat s = selectedSeats.get(i);
            BigDecimal p = s.getSeatType() == SeatType.PREMIUM ? showtime.getPricePremium() : showtime.getPriceRegular();
            total = total.add(p);
            seatPrices.add(p);
            if (i > 0) seatCodes.append(", ");
            seatCodes.append(s.getSeatCode()).append(" (").append(s.getSeatType()).append(")");
        }

        addSummaryRow(summaryGrid, 3, "Seats (" + selectedSeats.size() + "):", seatCodes.toString());
        addSummaryRow(summaryGrid, 4, "Total Payable:", "₹ " + total.toPlainString());

        final BigDecimal finalTotal = total;

        // Payment Method Selection
        VBox paymentMethodBox = new VBox(8);
        Label payTitle = new Label("Select Simulated Payment Method:");
        payTitle.getStyleClass().add("title-small");

        paymentGroup = new ToggleGroup();
        RadioButton upiRadio = new RadioButton("UPI (Google Pay / PhonePe / Paytm)");
        upiRadio.setToggleGroup(paymentGroup);
        upiRadio.setSelected(true);
        upiRadio.setStyle("-fx-text-fill: #f1f5f9;");

        RadioButton cardRadio = new RadioButton("Credit / Debit Card");
        cardRadio.setToggleGroup(paymentGroup);
        cardRadio.setStyle("-fx-text-fill: #f1f5f9;");

        RadioButton cashRadio = new RadioButton("Cash (Pay at Box Office)");
        cashRadio.setToggleGroup(paymentGroup);
        cashRadio.setStyle("-fx-text-fill: #f1f5f9;");

        paymentMethodBox.getChildren().addAll(payTitle, upiRadio, cardRadio, cashRadio);

        // Action Buttons
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button-secondary");
        // ActionEvent: Cancel payment and release holds
        cancelBtn.setOnAction(e -> handleCancelPayment());

        Button confirmBtn = new Button("Confirm & Pay ₹ " + finalTotal.toPlainString());
        confirmBtn.getStyleClass().add("button-primary");
        // ActionEvent: Confirm payment and trigger ACID JDBC transaction
        confirmBtn.setOnAction(e -> handleConfirmPayment(finalTotal, seatPrices));

        actionRow.getChildren().addAll(cancelBtn, confirmBtn);

        content.getChildren().addAll(title, timerBox, summaryGrid, paymentMethodBox, actionRow);

        Scene scene = new Scene(content);
        try {
            String css = getClass().getResource("/css/styles.css") != null
                    ? getClass().getResource("/css/styles.css").toExternalForm() : null;
            if (css != null) scene.getStylesheets().add(css);
        } catch (Exception ignored) {}

        dialogStage.setScene(scene);

        // WindowEvent: Releases holds if user closes window via 'X' button
        dialogStage.setOnCloseRequest(e -> handleCancelPayment());

        // Timeline: 5-minute countdown timer running every 1 second
        startCountdownTimeline();

        dialogStage.show();
    }

    private void addSummaryRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("text-subtitle");
        lbl.setStyle("-fx-font-weight: bold;");

        Label val = new Label(value);
        val.getStyleClass().add("text-body");
        val.setWrapText(true);

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    /**
     * Timeline: Starts the 5-minute countdown and automatically expires holds.
     */
    private void startCountdownTimeline() {
        remainingSeconds = 300; // 5 minutes

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;

            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            timerLabel.setText(String.format("Seats reserved for: %02d:%02d", minutes, seconds));

            if (remainingSeconds <= 0) {
                countdownTimeline.stop();
                handleHoldExpiry();
            }
        }));

        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void handleHoldExpiry() {
        User user = AuthService.getCurrentUser();
        if (user != null) {
            bookingService.releaseUserHolds(showtime.getId(), user.getId());
        }

        AlertHelper.showWarning(
                "Reservation Expired",
                "Checkout Timeout",
                "Your 5-minute seat hold has expired. The seats have been released back to availability."
        );
        dialogStage.close();
    }

    private void handleCancelPayment() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        User user = AuthService.getCurrentUser();
        if (user != null) {
            bookingService.releaseUserHolds(showtime.getId(), user.getId());
        }
        dialogStage.close();
    }

    private void handleConfirmPayment(BigDecimal totalAmount, List<BigDecimal> seatPrices) {
        User user = AuthService.getCurrentUser();
        if (user == null) {
            AlertHelper.showError("Auth Error", "Session Expired", "Please log in again.");
            return;
        }

        // Determine selected payment method
        RadioButton selectedRadio = (RadioButton) paymentGroup.getSelectedToggle();
        PaymentMethod method = PaymentMethod.CARD;
        if (selectedRadio.getText().startsWith("UPI")) {
            method = PaymentMethod.UPI;
        } else if (selectedRadio.getText().startsWith("Cash")) {
            method = PaymentMethod.CASH;
        }

        // Prepare Booking and Payment models
        Booking booking = new Booking(user.getId(), showtime.getId(), null, totalAmount, BookingStatus.CONFIRMED);
        Payment payment = new Payment(0, method, totalAmount, PaymentStatus.PAID);

        List<Integer> seatIds = new ArrayList<>();
        for (Seat s : selectedSeats) {
            seatIds.add(s.getId());
        }

        try {
            // Execute ACID JDBC Transaction (SELECT ... FOR UPDATE, inserts, hold cleanup)
            boolean success = bookingService.confirmBooking(booking, seatIds, seatPrices, payment);

            if (success) {
                if (countdownTimeline != null) {
                    countdownTimeline.stop();
                }

                // Populate display fields on booking for ticket summary
                booking.setUsername(user.getUsername());
                booking.setCustomerName(user.getFullName());
                booking.setMovieTitle(showtime.getMovieTitle());
                booking.setScreenName(showtime.getScreenName());
                booking.setShowtimeStart(showtime.getStartTime());
                booking.setPaymentMethod(method);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < selectedSeats.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(selectedSeats.get(i).getSeatCode());
                }
                booking.setSeatNumbers(sb.toString());

                dialogStage.close();

                if (onBookingSuccessful != null) {
                    onBookingSuccessful.accept(booking);
                }
            }

        } catch (SQLException e) {
            AlertHelper.showError("Booking Failed", "Transaction Rolled Back",
                    "Could not complete booking: " + e.getMessage());
        }
    }
}

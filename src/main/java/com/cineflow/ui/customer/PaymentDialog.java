package com.cineflow.ui.customer;

import com.cineflow.model.*;
import com.cineflow.service.AuthService;
import com.cineflow.service.BookingService;
import com.cineflow.util.AlertHelper;
import com.cineflow.util.PosterHelper;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Apple Liquid Glass Payment & Checkout Modal Dialog.
 * Features interactive Apple Card, UPI, and Cash simulation with live 5-minute hold timer
 * and ACID JDBC transaction execution.
 */
public class PaymentDialog {

    private final Showtime showtime;
    private final List<Seat> selectedSeats;
    private final BookingService bookingService;
    private final Consumer<Booking> onBookingSuccessful;

    private Stage dialogStage;
    private Timeline countdownTimeline;
    private int remainingSeconds = 300; // 5 minutes

    private Label timerLabel;
    private PaymentMethod selectedMethod = PaymentMethod.UPI;

    // Form inputs
    private TextField upiField;
    private TextField cardNumberField;
    private TextField cardExpiryField;
    private TextField cardCvvField;
    private Button confirmBtn;
    private ProgressIndicator progressIndicator;

    public PaymentDialog(Showtime showtime, List<Seat> selectedSeats, Consumer<Booking> onBookingSuccessful) {
        this.showtime = showtime;
        this.selectedSeats = selectedSeats;
        this.bookingService = new BookingService();
        this.onBookingSuccessful = onBookingSuccessful;
    }

    public void show() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("CineFlow • Secure Checkout & Payment");
        dialogStage.setResizable(false);

        VBox content = new VBox(16);
        content.getStyleClass().add("card");
        content.setPadding(new Insets(24));
        content.setPrefWidth(540);

        // Header with Brand & Close
        HBox headerRow = createHeaderRow();

        // 5-Minute Hold Countdown Banner
        HBox timerBox = createTimerBox();

        // Order Summary with Movie Poster Thumbnail
        HBox summaryBox = createSummaryBox();

        // Payment Method Selector (Apple Segmented Glass Control)
        VBox paymentMethodBox = createPaymentMethodSection();

        // Action Buttons Row (Cancel & Confirm & Pay)
        HBox actionRow = createActionRow();

        content.getChildren().addAll(headerRow, timerBox, summaryBox, paymentMethodBox, actionRow);

        Scene scene = new Scene(content);
        scene.setFill(Color.TRANSPARENT);
        try {
            String css = getClass().getResource("/css/styles.css") != null
                    ? getClass().getResource("/css/styles.css").toExternalForm() : null;
            if (css != null) scene.getStylesheets().add(css);
        } catch (Exception ignored) {}

        dialogStage.setScene(scene);
        dialogStage.setOnCloseRequest(e -> handleCancelPayment());

        startCountdownTimeline();
        dialogStage.show();
    }

    private HBox createHeaderRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label brand = new Label("CINEFLOW CHECKOUT");
        brand.getStyleClass().add("brand-text");
        brand.setStyle("-fx-font-size: 18px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sslBadge = new Label("🔒 256-BIT ENCRYPTION");
        sslBadge.getStyleClass().add("badge-info");
        sslBadge.setStyle("-fx-font-size: 11px;");

        row.getChildren().addAll(brand, spacer, sslBadge);
        return row;
    }

    private HBox createTimerBox() {
        HBox timerBox = new HBox(10);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.getStyleClass().add("badge-warning");
        timerBox.setStyle("-fx-background-color: rgba(255, 159, 10, 0.15); -fx-padding: 8px 16px; -fx-background-radius: 12px; -fx-border-color: rgba(255, 159, 10, 0.5); -fx-border-width: 1px; -fx-border-radius: 12px;");

        Label timerIcon = new Label("⏳");
        timerIcon.setStyle("-fx-font-size: 15px;");

        timerLabel = new Label("Seats reserved for: 05:00");
        timerLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #ffd60a;");

        timerBox.getChildren().addAll(timerIcon, timerLabel);
        return timerBox;
    }

    private HBox createSummaryBox() {
        HBox box = new HBox(14);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("card");
        box.setStyle("-fx-background-color: rgba(255, 255, 255, 0.04); -fx-border-color: rgba(255, 255, 255, 0.12); -fx-padding: 14px; -fx-background-radius: 14px; -fx-border-radius: 14px;");

        // Movie poster thumbnail (Apple rounded glass)
        ImageView posterThumb = PosterHelper.createPosterImageView(showtime.getMovieTitle(), 70, 95, 10);

        // Details column
        VBox details = new VBox(5);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label movieTitle = new Label(showtime.getMovieTitle());
        movieTitle.getStyleClass().add("title-small");
        movieTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label showMeta = new Label("🏛 " + showtime.getScreenName() + " • 🕒 " + showtime.getFormattedStartTime());
        showMeta.getStyleClass().add("text-subtitle");

        StringBuilder seatCodes = new StringBuilder();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < selectedSeats.size(); i++) {
            Seat s = selectedSeats.get(i);
            BigDecimal p = s.getSeatType() == SeatType.PREMIUM ? showtime.getPricePremium() : showtime.getPriceRegular();
            total = total.add(p);
            if (i > 0) seatCodes.append(", ");
            seatCodes.append(s.getSeatCode());
        }

        Label seatLabel = new Label("🎟 Seats: " + seatCodes + " (" + selectedSeats.size() + " total)");
        seatLabel.getStyleClass().add("text-body");

        Label totalLabel = new Label("Amount Payable: ₹ " + total.toPlainString());
        totalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #34c759;");

        details.getChildren().addAll(movieTitle, showMeta, seatLabel, totalLabel);
        box.getChildren().addAll(posterThumb, details);
        return box;
    }

    private VBox createPaymentMethodSection() {
        VBox section = new VBox(10);

        Label secTitle = new Label("Select Payment Method:");
        secTitle.getStyleClass().add("title-small");
        secTitle.setStyle("-fx-font-size: 13px;");

        // Apple Segmented Control Bar
        HBox segmentedBar = new HBox(8);
        segmentedBar.setAlignment(Pos.CENTER);
        segmentedBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35); -fx-padding: 4px; -fx-background-radius: 18px;");

        Button upiTab = createSegmentButton("📱 UPI / QR Code", true);
        Button cardTab = createSegmentButton("💳 Credit / Debit Card", false);
        Button cashTab = createSegmentButton("💵 Pay at Box Office", false);

        segmentedBar.getChildren().addAll(upiTab, cardTab, cashTab);

        // Container holding the dynamic payment form
        StackPane formContainer = new StackPane();
        formContainer.setStyle("-fx-min-height: 120px;");

        VBox upiView = createUpiForm();
        VBox cardView = createCardForm();
        VBox cashView = createCashForm();

        formContainer.getChildren().addAll(cardView, cashView, upiView);
        cardView.setVisible(false);
        cashView.setVisible(false);
        upiView.setVisible(true);

        // Tab selection logic
        upiTab.setOnAction(e -> {
            selectedMethod = PaymentMethod.UPI;
            setSegmentActive(upiTab, true);
            setSegmentActive(cardTab, false);
            setSegmentActive(cashTab, false);
            upiView.setVisible(true);
            cardView.setVisible(false);
            cashView.setVisible(false);
        });

        cardTab.setOnAction(e -> {
            selectedMethod = PaymentMethod.CARD;
            setSegmentActive(upiTab, false);
            setSegmentActive(cardTab, true);
            setSegmentActive(cashTab, false);
            upiView.setVisible(false);
            cardView.setVisible(true);
            cashView.setVisible(false);
        });

        cashTab.setOnAction(e -> {
            selectedMethod = PaymentMethod.CASH;
            setSegmentActive(upiTab, false);
            setSegmentActive(cardTab, false);
            setSegmentActive(cashTab, true);
            upiView.setVisible(false);
            cardView.setVisible(false);
            cashView.setVisible(true);
        });

        section.getChildren().addAll(secTitle, segmentedBar, formContainer);
        return section;
    }

    private Button createSegmentButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);
        setSegmentActive(btn, active);
        return btn;
    }

    private void setSegmentActive(Button btn, boolean active) {
        if (active) {
            btn.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 14px; -fx-padding: 8px 12px; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #98989d; -fx-background-radius: 14px; -fx-padding: 8px 12px; -fx-cursor: hand;");
        }
    }

    private VBox createUpiForm() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 4, 4, 4));

        Label prompt = new Label("Enter Virtual Payment Address (VPA / UPI ID):");
        prompt.getStyleClass().add("text-subtitle");

        upiField = new TextField();
        upiField.setPromptText("e.g. yourname@okaxis, 9876543210@paytm");
        upiField.setText(AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getUsername() + "@okhdfcbank" : "customer@upi");

        // Quick click chips for popular UPI providers
        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER_LEFT);
        chips.getChildren().addAll(
                createUpiChip("@oksbi"),
                createUpiChip("@okaxis"),
                createUpiChip("@paytm"),
                createUpiChip("@ybl")
        );

        box.getChildren().addAll(prompt, upiField, chips);
        return box;
    }

    private Button createUpiChip(String handle) {
        Button chip = new Button(handle);
        chip.getStyleClass().add("button-secondary");
        chip.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px; -fx-background-radius: 12px;");
        chip.setOnAction(e -> {
            String current = upiField.getText();
            int atIdx = current.indexOf('@');
            String prefix = atIdx > 0 ? current.substring(0, atIdx) : current;
            if (prefix.isBlank()) prefix = "customer";
            upiField.setText(prefix + handle);
        });
        return chip;
    }

    private VBox createCardForm() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 4, 4, 4));

        cardNumberField = new TextField("4532 8901 2345 7890");
        cardNumberField.setPromptText("Card Number (16 digits)");

        HBox cardRow = new HBox(10);
        cardExpiryField = new TextField("12/28");
        cardExpiryField.setPromptText("MM/YY");
        cardExpiryField.setPrefWidth(120);

        cardCvvField = new TextField("888");
        cardCvvField.setPromptText("CVV");
        cardCvvField.setPrefWidth(90);

        Button autofillBtn = new Button("Auto-Fill Test Card");
        autofillBtn.getStyleClass().add("button-secondary");
        autofillBtn.setStyle("-fx-font-size: 11px;");
        autofillBtn.setOnAction(e -> {
            cardNumberField.setText("4532 8901 2345 7890");
            cardExpiryField.setText("12/28");
            cardCvvField.setText("888");
        });

        cardRow.getChildren().addAll(cardExpiryField, cardCvvField, autofillBtn);

        box.getChildren().addAll(new Label("Simulated Credit/Debit Card Details:"), cardNumberField, cardRow);
        return box;
    }

    private VBox createCashForm() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(14, 4, 4, 4));
        box.setAlignment(Pos.CENTER_LEFT);

        Label infoTitle = new Label("Box Office Counter Cash Payment");
        infoTitle.getStyleClass().add("title-small");
        infoTitle.setStyle("-fx-text-fill: #34c759;");

        Label desc = new Label("Your reservation will be confirmed immediately. Please show your Booking ID at the box office ticket counter 15 minutes before showtime to pay in cash and collect printed tickets.");
        desc.getStyleClass().add("text-body");
        desc.setWrapText(true);

        box.getChildren().addAll(infoTitle, desc);
        return box;
    }

    private HBox createActionRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> handleCancelPayment());

        BigDecimal total = BigDecimal.ZERO;
        List<BigDecimal> seatPrices = new ArrayList<>();
        for (Seat s : selectedSeats) {
            BigDecimal p = s.getSeatType() == SeatType.PREMIUM ? showtime.getPricePremium() : showtime.getPriceRegular();
            total = total.add(p);
            seatPrices.add(p);
        }
        final BigDecimal finalTotal = total;

        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(22, 22);
        progressIndicator.setVisible(false);

        confirmBtn = new Button("Confirm & Pay ₹ " + finalTotal.toPlainString());
        confirmBtn.getStyleClass().add("button-primary");
        confirmBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px 24px;");
        confirmBtn.setOnAction(e -> triggerPaymentProcessing(finalTotal, seatPrices));

        row.getChildren().addAll(progressIndicator, cancelBtn, confirmBtn);
        return row;
    }

    private void triggerPaymentProcessing(BigDecimal totalAmount, List<BigDecimal> seatPrices) {
        User user = AuthService.getCurrentUser();
        if (user == null) {
            AlertHelper.showError("Auth Error", "Session Expired", "Please log in again.");
            return;
        }

        // Validate method specific inputs
        if (selectedMethod == PaymentMethod.UPI) {
            String upi = upiField != null ? upiField.getText().trim() : "";
            if (upi.isEmpty() || !upi.contains("@")) {
                AlertHelper.showWarning("Invalid UPI ID", "Please enter a valid UPI ID (e.g. name@bank).", "UPI format must contain '@'.");
                return;
            }
        } else if (selectedMethod == PaymentMethod.CARD) {
            String cNum = cardNumberField != null ? cardNumberField.getText().replaceAll("\\s+", "") : "";
            if (cNum.length() < 12) {
                AlertHelper.showWarning("Invalid Card", "Please enter a valid card number.", "Card number must contain at least 12 digits.");
                return;
            }
        }

        // Visual simulation: disable button and show progress indicator
        confirmBtn.setDisable(true);
        confirmBtn.setText("Processing Secure Payment...");
        progressIndicator.setVisible(true);

        // 800ms realistic payment gateway simulation before committing JDBC transaction
        PauseTransition delay = new PauseTransition(Duration.millis(800));
        delay.setOnFinished(e -> handleConfirmPayment(totalAmount, seatPrices));
        delay.play();
    }

    private void handleConfirmPayment(BigDecimal totalAmount, List<BigDecimal> seatPrices) {
        User user = AuthService.getCurrentUser();
        if (user == null) {
            confirmBtn.setDisable(false);
            progressIndicator.setVisible(false);
            AlertHelper.showError("Auth Error", "Session Expired", "Please log in again.");
            return;
        }

        Booking booking = new Booking(user.getId(), showtime.getId(), null, totalAmount, BookingStatus.CONFIRMED);
        Payment payment = new Payment(0, selectedMethod, totalAmount, PaymentStatus.PAID);

        List<Integer> seatIds = new ArrayList<>();
        for (Seat s : selectedSeats) {
            seatIds.add(s.getId());
        }

        try {
            // Execute the ACID JDBC transaction
            boolean success = bookingService.confirmBooking(booking, seatIds, seatPrices, payment);

            if (success) {
                if (countdownTimeline != null) {
                    countdownTimeline.stop();
                }

                // Populate model display fields for ticket summary view
                booking.setUsername(user.getUsername());
                booking.setCustomerName(user.getFullName());
                booking.setMovieTitle(showtime.getMovieTitle());
                booking.setScreenName(showtime.getScreenName());
                booking.setShowtimeStart(showtime.getStartTime());
                booking.setPaymentMethod(selectedMethod);

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
            } else {
                confirmBtn.setDisable(false);
                confirmBtn.setText("Confirm & Pay ₹ " + totalAmount.toPlainString());
                progressIndicator.setVisible(false);
                AlertHelper.showError("Booking Failed", "Transaction Failed", "Could not verify reservation.");
            }

        } catch (SQLException e) {
            confirmBtn.setDisable(false);
            confirmBtn.setText("Confirm & Pay ₹ " + totalAmount.toPlainString());
            progressIndicator.setVisible(false);
            AlertHelper.showError("Booking Failed", "Transaction Rolled Back",
                    "Could not complete booking: " + e.getMessage());
        }
    }

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
}

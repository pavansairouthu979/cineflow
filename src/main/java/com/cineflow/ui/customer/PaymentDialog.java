package com.cineflow.ui.customer;

import com.cineflow.model.*;
import com.cineflow.service.AuthService;
import com.cineflow.service.BookingService;
import com.cineflow.util.AlertHelper;
import com.cineflow.util.PosterHelper;
import javafx.animation.*;
import javafx.application.Platform;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * CineFlow — Premium Secure Checkout Dialog.
 *
 * Fixes applied:
 *  1. JDBC transaction runs on a background thread (Platform.runLater for UI updates)
 *     → UI never freezes during "Processing..." phase
 *  2. Stage uses a solid dark background (no transparent scene hack)
 *     → Dialog renders correctly on all platforms
 *  3. Payment method state is reliably captured at confirm-click time
 */
public class PaymentDialog {

    private final Showtime showtime;
    private final List<Seat> selectedSeats;
    private final BookingService bookingService;
    private final Consumer<Booking> onBookingSuccessful;

    private Stage dialogStage;
    private Timeline countdownTimeline;
    private int remainingSeconds = 300;

    private Label timerLabel;
    private PaymentMethod selectedMethod = PaymentMethod.UPI;

    private TextField upiField;
    private TextField cardNumberField;
    private TextField cardExpiryField;
    private TextField cardCvvField;
    private Button confirmBtn;
    private ProgressIndicator progressIndicator;

    public PaymentDialog(Showtime showtime,
                         List<Seat> selectedSeats,
                         Consumer<Booking> onBookingSuccessful) {
        this.showtime = showtime;
        this.selectedSeats = new ArrayList<>(selectedSeats);   // defensive copy
        this.bookingService = new BookingService();
        this.onBookingSuccessful = onBookingSuccessful;
    }

    /* ================================================================ */
    /* SHOW                                                               */
    /* ================================================================ */
    public void show() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("CineFlow  •  Secure Checkout");
        dialogStage.setResizable(false);
        dialogStage.setMinWidth(580);

        // ── Root layout ───────────────────────────────────────────────
        VBox content = buildContent();

        // ── Scene — solid dark background, no transparency tricks ─────
        Scene scene = new Scene(content);
        scene.setFill(Color.web("#0D0B18")); // matches card bg, no white flash

        try {
            String css = getClass().getResource("/css/styles.css") != null
                    ? getClass().getResource("/css/styles.css").toExternalForm() : null;
            if (css != null) scene.getStylesheets().add(css);
        } catch (Exception ignored) {}

        dialogStage.setScene(scene);
        dialogStage.setOnCloseRequest(e -> handleCancel());

        // Slide-in entrance
        content.setOpacity(0);
        content.setTranslateY(12);
        dialogStage.show();

        FadeTransition ft = new FadeTransition(Duration.millis(300), content);
        ft.setToValue(1.0);
        ft.play();
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), content);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.play();

        startCountdown();
    }

    /* ================================================================ */
    /* CONTENT BUILDER                                                    */
    /* ================================================================ */
    private VBox buildContent() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #0D0B18;");
        root.setPrefWidth(580);

        // ── Coloured top accent bar ───────────────────────────────────
        Region accentBar = new Region();
        accentBar.setPrefHeight(4);
        accentBar.setStyle(
            "-fx-background-color: linear-gradient(to right, #8B5CF6, #00C2FF);"
        );

        // ── Main card padding wrapper ─────────────────────────────────
        VBox card = new VBox(16);
        card.setPadding(new Insets(24, 28, 24, 28));
        card.setStyle(
            "-fx-background-color: #0D0B18;" +
            "-fx-border-color: rgba(139,92,246,0.35);" +
            "-fx-border-width: 0 1px 1px 1px;"
        );

        card.getChildren().addAll(
                buildHeader(),
                buildTimerBanner(),
                buildSummaryBox(),
                divider(),
                buildPaymentMethodSection(),
                divider(),
                buildActionRow()
        );

        root.getChildren().addAll(accentBar, card);
        return root;
    }

    /* ================================================================ */
    /* HEADER                                                             */
    /* ================================================================ */
    private HBox buildHeader() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label brand = new Label("🔐  SECURE CHECKOUT");
        brand.setStyle(
            "-fx-font-size: 17px; -fx-font-weight: 900; -fx-text-fill: #F8FAFC;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label("256-BIT ENCRYPTED");
        badge.setStyle(
            "-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #00C2FF;" +
            "-fx-background-color: rgba(0,194,255,0.10);" +
            "-fx-border-color: rgba(0,194,255,0.35);" +
            "-fx-border-width: 1px; -fx-border-radius: 20px; -fx-background-radius: 20px;" +
            "-fx-padding: 3px 10px;"
        );

        row.getChildren().addAll(brand, spacer, badge);
        return row;
    }

    /* ================================================================ */
    /* TIMER BANNER                                                       */
    /* ================================================================ */
    private HBox buildTimerBanner() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(
            "-fx-background-color: rgba(245,158,11,0.09);" +
            "-fx-padding: 10px 16px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: rgba(245,158,11,0.30);" +
            "-fx-border-radius: 10px; -fx-border-width: 1px;"
        );

        Label icon = new Label("⏳");
        icon.setStyle("-fx-font-size: 16px;");

        timerLabel = new Label("Seats reserved for: 05:00");
        timerLabel.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #FCD34D;"
        );

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label hint = new Label("Complete payment before time expires");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #78350F;");

        box.getChildren().addAll(icon, timerLabel, sp, hint);
        return box;
    }

    /* ================================================================ */
    /* ORDER SUMMARY                                                      */
    /* ================================================================ */
    private HBox buildSummaryBox() {
        HBox box = new HBox(16);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(
            "-fx-background-color: rgba(139,92,246,0.07);" +
            "-fx-padding: 16px;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(139,92,246,0.22);" +
            "-fx-border-radius: 12px; -fx-border-width: 1px;"
        );

        // Movie poster thumbnail
        ImageView poster = PosterHelper.createPosterImageView(
                showtime.getMovieTitle(), 70, 96, 10);

        // Details
        VBox details = new VBox(5);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label movieLbl = new Label(showtime.getMovieTitle());
        movieLbl.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #F8FAFC;"
        );

        Label screenLbl = new Label("🏛  " + showtime.getScreenName()
                + "    🕒  " + showtime.getFormattedStartTime());
        screenLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #A1A1AA;");

        // Calculate total and seat list
        StringBuilder seatCodes = new StringBuilder();
        BigDecimal runningTotal = BigDecimal.ZERO;
        for (int i = 0; i < selectedSeats.size(); i++) {
            Seat s = selectedSeats.get(i);
            BigDecimal p = s.getSeatType() == SeatType.PREMIUM
                    ? showtime.getPricePremium() : showtime.getPriceRegular();
            runningTotal = runningTotal.add(p);
            if (i > 0) seatCodes.append(", ");
            seatCodes.append(s.getSeatCode());
        }

        Label seatsLbl = new Label("🎟  Seats: " + seatCodes
                + "  (" + selectedSeats.size() + ")");
        seatsLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #CBD5E1;");

        Label totalLbl = new Label("Amount: ₹ " + runningTotal.toPlainString());
        totalLbl.setStyle(
            "-fx-font-size: 19px; -fx-font-weight: 900; -fx-text-fill: #22C55E;"
        );

        details.getChildren().addAll(movieLbl, screenLbl, seatsLbl, totalLbl);
        box.getChildren().addAll(poster, details);
        return box;
    }

    /* ================================================================ */
    /* PAYMENT METHOD SELECTOR                                            */
    /* ================================================================ */
    private VBox buildPaymentMethodSection() {
        VBox section = new VBox(12);

        Label lbl = new Label("Select Payment Method");
        lbl.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #CBD5E1;"
        );

        // Segmented tab bar
        HBox segBar = new HBox(4);
        segBar.setStyle(
            "-fx-background-color: rgba(0,0,0,0.40);" +
            "-fx-padding: 4px; -fx-background-radius: 12px;" +
            "-fx-border-color: rgba(139,92,246,0.18);" +
            "-fx-border-radius: 12px; -fx-border-width: 1px;"
        );

        Button upiTab  = segBtn("📱  UPI",          true);
        Button cardTab = segBtn("💳  Card",          false);
        Button cashTab = segBtn("💵  Cash",          false);
        segBar.getChildren().addAll(upiTab, cardTab, cashTab);

        // Form pane — only one visible at a time
        StackPane forms = new StackPane();
        forms.setMinHeight(115);

        VBox upiForm  = buildUpiForm();
        VBox cardForm = buildCardForm();
        VBox cashForm = buildCashForm();

        cardForm.setVisible(false);
        cashForm.setVisible(false);
        forms.getChildren().addAll(cardForm, cashForm, upiForm);

        // Tab switching
        upiTab.setOnAction(e -> {
            selectedMethod = PaymentMethod.UPI;
            setActive(upiTab, true); setActive(cardTab, false); setActive(cashTab, false);
            upiForm.setVisible(true); cardForm.setVisible(false); cashForm.setVisible(false);
        });
        cardTab.setOnAction(e -> {
            selectedMethod = PaymentMethod.CARD;
            setActive(upiTab, false); setActive(cardTab, true); setActive(cashTab, false);
            upiForm.setVisible(false); cardForm.setVisible(true); cashForm.setVisible(false);
        });
        cashTab.setOnAction(e -> {
            selectedMethod = PaymentMethod.CASH;
            setActive(upiTab, false); setActive(cardTab, false); setActive(cashTab, true);
            upiForm.setVisible(false); cardForm.setVisible(false); cashForm.setVisible(true);
        });

        section.getChildren().addAll(lbl, segBar, forms);
        return section;
    }

    private Button segBtn(String text, boolean active) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(b, Priority.ALWAYS);
        setActive(b, active);
        return b;
    }

    private void setActive(Button b, boolean active) {
        if (active) {
            b.setStyle(
                "-fx-background-color: linear-gradient(to right, #8B5CF6, #6D28D9);" +
                "-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 13px;" +
                "-fx-background-radius: 9px; -fx-padding: 8px 14px; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.45), 10, 0, 0, 3);"
            );
        } else {
            b.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #6B7280;" +
                "-fx-font-size: 13px; -fx-background-radius: 9px;" +
                "-fx-padding: 8px 14px; -fx-cursor: hand;"
            );
        }
    }

    /* ── UPI form ───────────────────────────────────────────────────── */
    private VBox buildUpiForm() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(6, 0, 0, 0));

        Label prompt = new Label("UPI ID / Virtual Payment Address");
        prompt.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        upiField = new TextField();
        upiField.setPromptText("yourname@okaxis");
        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null) {
            upiField.setText(currentUser.getUsername() + "@okhdfcbank");
        }

        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER_LEFT);
        chips.getChildren().addAll(
                upiChip("@oksbi"), upiChip("@okaxis"),
                upiChip("@paytm"), upiChip("@ybl"));

        box.getChildren().addAll(prompt, upiField, chips);
        return box;
    }

    private Button upiChip(String suffix) {
        Button c = new Button(suffix);
        c.setStyle(
            "-fx-font-size: 11px; -fx-padding: 4px 10px;" +
            "-fx-background-color: rgba(139,92,246,0.10); -fx-text-fill: #C4B5FD;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(139,92,246,0.28);" +
            "-fx-border-radius: 12px; -fx-border-width: 1px; -fx-cursor: hand;"
        );
        c.setOnAction(e -> {
            if (upiField == null) return;
            String cur = upiField.getText();
            int at = cur.indexOf('@');
            String prefix = at > 0 ? cur.substring(0, at) : cur;
            if (prefix.isBlank()) prefix = "customer";
            upiField.setText(prefix + suffix);
        });
        return c;
    }

    /* ── Card form ──────────────────────────────────────────────────── */
    private VBox buildCardForm() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(6, 0, 0, 0));

        Label prompt = new Label("Credit / Debit Card  (Simulated)");
        prompt.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        cardNumberField = new TextField("4532 8901 2345 7890");
        cardNumberField.setPromptText("Card number");

        HBox row2 = new HBox(10);
        cardExpiryField = new TextField("12/28");
        cardExpiryField.setPromptText("MM/YY");
        cardExpiryField.setPrefWidth(100);

        cardCvvField = new TextField("888");
        cardCvvField.setPromptText("CVV");
        cardCvvField.setPrefWidth(80);

        Button autoFill = new Button("Auto-fill Test Card");
        autoFill.getStyleClass().add("button-secondary");
        autoFill.setStyle("-fx-font-size: 11px; -fx-padding: 5px 12px;");
        autoFill.setOnAction(e -> {
            cardNumberField.setText("4532 8901 2345 7890");
            cardExpiryField.setText("12/28");
            cardCvvField.setText("888");
        });

        row2.getChildren().addAll(cardExpiryField, cardCvvField, autoFill);
        box.getChildren().addAll(prompt, cardNumberField, row2);
        return box;
    }

    /* ── Cash form ──────────────────────────────────────────────────── */
    private VBox buildCashForm() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        box.setStyle(
            "-fx-background-color: rgba(34,197,94,0.06);" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: rgba(34,197,94,0.22);" +
            "-fx-border-radius: 10px; -fx-border-width: 1px;"
        );

        Label title = new Label("📍  Pay at Box Office Counter");
        title.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #22C55E;"
        );

        Label desc = new Label(
            "Your booking is confirmed immediately. Please visit the box office " +
            "15 minutes before showtime with your Booking ID to pay and collect printed tickets."
        );
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #A1A1AA; -fx-line-spacing: 2px;");
        desc.setWrapText(true);

        box.getChildren().addAll(title, desc);
        return box;
    }

    /* ================================================================ */
    /* ACTION ROW                                                         */
    /* ================================================================ */
    private HBox buildActionRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(4, 0, 0, 0));

        // Compute total & seat prices once
        BigDecimal total = BigDecimal.ZERO;
        List<BigDecimal> seatPrices = new ArrayList<>();
        for (Seat s : selectedSeats) {
            BigDecimal p = s.getSeatType() == SeatType.PREMIUM
                    ? showtime.getPricePremium() : showtime.getPriceRegular();
            total = total.add(p);
            seatPrices.add(p);
        }
        final BigDecimal finalTotal  = total;
        final List<BigDecimal> finalPrices = seatPrices;

        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(24, 24);
        progressIndicator.setVisible(false);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setStyle("-fx-font-size: 14px; -fx-padding: 11px 22px;");
        cancelBtn.setOnAction(e -> handleCancel());

        confirmBtn = new Button("Confirm & Pay  ₹ " + finalTotal.toPlainString());
        confirmBtn.getStyleClass().add("button-primary");
        confirmBtn.setStyle("-fx-font-size: 14px; -fx-padding: 11px 26px;");
        confirmBtn.setOnAction(e -> triggerPayment(finalTotal, finalPrices));

        row.getChildren().addAll(progressIndicator, cancelBtn, confirmBtn);
        return row;
    }

    /* ================================================================ */
    /* PAYMENT PROCESSING — JDBC runs on background thread              */
    /* ================================================================ */
    private void triggerPayment(BigDecimal total, List<BigDecimal> prices) {
        // ── Input validation ──────────────────────────────────────────
        if (selectedMethod == PaymentMethod.UPI) {
            String upi = upiField != null ? upiField.getText().trim() : "";
            if (upi.isEmpty() || !upi.contains("@")) {
                AlertHelper.showWarning("Invalid UPI ID",
                        "Validation Error",
                        "Please enter a valid UPI ID (e.g. yourname@okaxis).");
                return;
            }
        } else if (selectedMethod == PaymentMethod.CARD) {
            String num = cardNumberField != null
                    ? cardNumberField.getText().replaceAll("\\s+", "") : "";
            if (num.length() < 12) {
                AlertHelper.showWarning("Invalid Card Number",
                        "Validation Error",
                        "Card number must be at least 12 digits.");
                return;
            }
        }

        User user = AuthService.getCurrentUser();
        if (user == null) {
            AlertHelper.showError("Session Expired", "Auth Error", "Please log in again.");
            return;
        }

        // ── Lock UI ───────────────────────────────────────────────────
        confirmBtn.setDisable(true);
        confirmBtn.setText("Processing...");
        progressIndicator.setVisible(true);

        // Snapshot payment method — captured before background thread starts
        final PaymentMethod methodSnapshot = selectedMethod;

        // ── Run JDBC on background thread — never block FX thread ─────
        Thread worker = new Thread(() -> {
            // Brief visual delay so user sees "Processing..." state
            try { Thread.sleep(700); } catch (InterruptedException ignored) {}
            doBookingTransaction(total, prices, user, methodSnapshot);
        }, "cineflow-booking-worker");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Executes the ACID JDBC booking transaction on a background thread.
     * All UI updates are marshalled back to the FX thread via Platform.runLater().
     */
    private void doBookingTransaction(BigDecimal total,
                                      List<BigDecimal> prices,
                                      User user,
                                      PaymentMethod method) {

        Booking booking = new Booking(
                user.getId(), showtime.getId(), null, total, BookingStatus.CONFIRMED);
        Payment payment = new Payment(
                0, method, total, PaymentStatus.PAID);

        List<Integer> seatIds = new ArrayList<>();
        for (Seat s : selectedSeats) seatIds.add(s.getId());

        try {
            boolean success = bookingService.confirmBooking(booking, seatIds, prices, payment);

            if (success) {
                // Stop countdown from background thread (safe — Timeline is FX-managed but
                // stop() is thread-safe in JavaFX)
                if (countdownTimeline != null) countdownTimeline.stop();

                // Populate display fields
                booking.setUsername(user.getUsername());
                booking.setCustomerName(user.getFullName());
                booking.setMovieTitle(showtime.getMovieTitle());
                booking.setScreenName(showtime.getScreenName());
                booking.setShowtimeStart(showtime.getStartTime());
                booking.setBookingTime(LocalDateTime.now());
                booking.setPaymentMethod(method);
                booking.setStatus(BookingStatus.CONFIRMED);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < selectedSeats.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(selectedSeats.get(i).getSeatCode());
                }
                booking.setSeatNumbers(sb.toString());

                // If DAO didn't set an ID, re-fetch most recent booking
                if (booking.getId() <= 0) {
                    try {
                        List<Booking> recent = bookingService.getCustomerBookings(user.getId());
                        if (!recent.isEmpty()) {
                            Booking freshBooking = recent.get(0);
                            freshBooking.setPaymentMethod(method);
                            Platform.runLater(() -> {
                                dialogStage.close();
                                if (onBookingSuccessful != null) onBookingSuccessful.accept(freshBooking);
                            });
                            return;
                        }
                    } catch (SQLException ignored) {}
                }

                final Booking finalBooking = booking;
                Platform.runLater(() -> {
                    dialogStage.close();
                    if (onBookingSuccessful != null) onBookingSuccessful.accept(finalBooking);
                });

            } else {
                Platform.runLater(() -> {
                    resetButton(total);
                    AlertHelper.showError("Booking Failed",
                            "Transaction Failed",
                            "Could not confirm your reservation. Please try again.");
                });
            }

        } catch (SQLException ex) {
            final String msg = ex.getMessage();
            Platform.runLater(() -> {
                resetButton(total);
                AlertHelper.showError("Booking Failed", "Database Error", msg);
            });
        }
    }

    private void resetButton(BigDecimal total) {
        confirmBtn.setDisable(false);
        confirmBtn.setText("Confirm & Pay  ₹ " + total.toPlainString());
        progressIndicator.setVisible(false);
    }

    /* ================================================================ */
    /* COUNTDOWN TIMER                                                    */
    /* ================================================================ */
    private void startCountdown() {
        remainingSeconds = 300;
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            int m = remainingSeconds / 60;
            int s = remainingSeconds % 60;
            timerLabel.setText(String.format("Seats reserved for: %02d:%02d", m, s));

            if (remainingSeconds <= 60) {
                timerLabel.setStyle(
                    "-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #EF4444;"
                );
            }
            if (remainingSeconds <= 0) {
                countdownTimeline.stop();
                handleExpiry();
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void handleExpiry() {
        User user = AuthService.getCurrentUser();
        if (user != null) bookingService.releaseUserHolds(showtime.getId(), user.getId());
        AlertHelper.showWarning("Time Expired",
                "Seat Hold Expired",
                "Your 5-minute hold has expired. The seats have been released. Please try again.");
        if (dialogStage != null) dialogStage.close();
    }

    private void handleCancel() {
        if (countdownTimeline != null) countdownTimeline.stop();
        User user = AuthService.getCurrentUser();
        if (user != null) bookingService.releaseUserHolds(showtime.getId(), user.getId());
        if (dialogStage != null) dialogStage.close();
    }

    /* ================================================================ */
    /* HELPERS                                                            */
    /* ================================================================ */
    private Region divider() {
        Region r = new Region();
        r.setMinHeight(1);
        r.setStyle("-fx-background-color: rgba(139,92,246,0.14);");
        return r;
    }
}

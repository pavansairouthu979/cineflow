package com.cineflow.ui.customer;

import com.cineflow.model.*;
import com.cineflow.service.AuthService;
import com.cineflow.service.BookingService;
import com.cineflow.service.ScreenService;
import com.cineflow.service.ValidationException;
import com.cineflow.util.AlertHelper;
import javafx.animation.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

/**
 * CineFlow — Premium Interactive Seat Map.
 * Cinema screen arc, animated seat selection, real-time price calculation,
 * 5-minute countdown, and color-coded seat states.
 */
public class SeatMapView {

    private final Showtime showtime;
    private final BookingService bookingService;
    private final ScreenService screenService;
    private final Consumer<List<Seat>> onProceedToPayment;
    private final Runnable onBack;

    private final List<Seat> selectedSeats = new ArrayList<>();
    private final SimpleDoubleProperty totalPriceProperty = new SimpleDoubleProperty(0.0);
    private Label totalLabel;
    private Label selectionCountLabel;
    private Button proceedBtn;
    private GridPane seatGrid;

    public SeatMapView(Showtime showtime, Consumer<List<Seat>> onProceedToPayment, Runnable onBack) {
        this.showtime = showtime;
        this.bookingService = new BookingService();
        this.screenService = new ScreenService();
        this.onProceedToPayment = onProceedToPayment;
        this.onBack = onBack;
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-container");

        // Top breadcrumb + info bar
        VBox topBar = createTopBar();
        root.setTop(topBar);

        // Center: screen arc + seat grid + legend
        VBox centerBox = new VBox(18);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(24, 36, 24, 36));

        // Cinema screen graphic
        VBox screenGraphic = createCinemaScreenGraphic();
        centerBox.getChildren().add(screenGraphic);

        // Seat grid
        seatGrid = new GridPane();
        seatGrid.setHgap(6);
        seatGrid.setVgap(6);
        seatGrid.setAlignment(Pos.CENTER);
        centerBox.getChildren().add(seatGrid);

        // Legend
        HBox legend = createLegend();
        centerBox.getChildren().add(legend);

        ScrollPane scroll = new ScrollPane(centerBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        root.setCenter(scroll);

        // Bottom checkout bar
        HBox bottomBar = createCheckoutBar();
        root.setBottom(bottomBar);

        loadSeatMap();
        return root;
    }

    /* ---------------------------------------------------------------- */
    /* TOP BAR                                                            */
    /* ---------------------------------------------------------------- */
    private VBox createTopBar() {
        VBox wrapper = new VBox(0);

        // Breadcrumb row
        HBox breadcrumb = new HBox(12);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);
        breadcrumb.setPadding(new Insets(12, 28, 12, 28));
        breadcrumb.setStyle(
            "-fx-background-color: rgba(21,19,35,0.78);" +
            "-fx-border-color: rgba(139,92,246,0.12);" +
            "-fx-border-width: 0 0 1px 0;"
        );

        Button backBtn = new Button("←  Showtimes");
        backBtn.getStyleClass().add("button-secondary");
        backBtn.setStyle("-fx-font-size: 13px; -fx-padding: 7px 16px;");
        backBtn.setOnAction(e -> onBack.run());

        Label sep = new Label("/");
        sep.setStyle("-fx-text-fill: #4B5563;");

        Label movLbl = new Label(showtime.getMovieTitle());
        movLbl.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px;");

        Label sep2 = new Label("/");
        sep2.setStyle("-fx-text-fill: #4B5563;");

        Label curLbl = new Label("Select Seats");
        curLbl.setStyle("-fx-text-fill: #8B5CF6; -fx-font-size: 13px; -fx-font-weight: 700;");

        breadcrumb.getChildren().addAll(backBtn, sep, movLbl, sep2, curLbl);

        // Info row
        HBox infoRow = new HBox(24);
        infoRow.setAlignment(Pos.CENTER);
        infoRow.setPadding(new Insets(12, 28, 12, 28));
        infoRow.setStyle(
            "-fx-background-color: rgba(139,92,246,0.06);" +
            "-fx-border-color: rgba(139,92,246,0.12);" +
            "-fx-border-width: 0 0 1px 0;"
        );

        Label movieLbl = buildInfoChip("🎬", showtime.getMovieTitle(), "#F8FAFC");
        Label screenLbl = buildInfoChip("🏛", showtime.getScreenName(), "#C4B5FD");
        Label timeLbl = buildInfoChip("🕒", showtime.getFormattedStartTime(), "#00C2FF");
        Label regLbl = buildInfoChip("◉", "Regular: ₹ " + showtime.getPriceRegular().toPlainString(), "#86EFAC");
        Label premLbl = buildInfoChip("◈", "Premium: ₹ " + showtime.getPricePremium().toPlainString(), "#FCD34D");

        infoRow.getChildren().addAll(movieLbl, screenLbl, timeLbl, regLbl, premLbl);
        wrapper.getChildren().addAll(breadcrumb, infoRow);
        return wrapper;
    }

    private Label buildInfoChip(String icon, String text, String color) {
        Label lbl = new Label(icon + "  " + text);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + color + ";");
        return lbl;
    }

    /* ---------------------------------------------------------------- */
    /* CINEMA SCREEN GRAPHIC                                              */
    /* ---------------------------------------------------------------- */
    private VBox createCinemaScreenGraphic() {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(800);

        Region screenBar = new Region();
        screenBar.getStyleClass().add("cinema-screen-bar");
        screenBar.setPrefWidth(680);
        screenBar.setPrefHeight(20);

        Label screenLabel = new Label("S  C  R  E  E  N");
        screenLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-font-weight: 900;" +
            "-fx-text-fill: rgba(0,194,255,0.55);" +
            "-fx-letter-spacing: 4px;"
        );

        box.getChildren().addAll(screenBar, screenLabel);
        box.setPadding(new Insets(0, 0, 10, 0));
        return box;
    }

    /* ---------------------------------------------------------------- */
    /* LEGEND                                                             */
    /* ---------------------------------------------------------------- */
    private HBox createLegend() {
        HBox box = new HBox(18);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(14, 0, 6, 0));

        box.getChildren().addAll(
                makeLegendItem("Available",   "#22C55E", "rgba(34,197,94,0.18)",  "rgba(34,197,94,0.55)"),
                makeLegendItem("Selected",    "#FFFFFF",  "linear-gradient(to bottom right, #8B5CF6, #00C2FF)", "rgba(139,92,246,0.8)"),
                makeLegendItem("Held",        "#FCD34D", "rgba(245,158,11,0.20)", "rgba(245,158,11,0.55)"),
                makeLegendItem("Booked",      "#71717A", "rgba(239,68,68,0.12)",  "rgba(239,68,68,0.20)"),
                makeLegendItemPremium()
        );
        return box;
    }

    private HBox makeLegendItem(String label, String textColor, String bg, String border) {
        HBox item = new HBox(7);
        item.setAlignment(Pos.CENTER);

        Label swatch = new Label();
        swatch.setMinSize(22, 22);
        swatch.setMaxSize(22, 22);
        swatch.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-background-radius: 6px;" +
            "-fx-border-color: " + border + ";" +
            "-fx-border-radius: 6px; -fx-border-width: 1.5px;"
        );

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #A1A1AA;");
        item.getChildren().addAll(swatch, lbl);
        return item;
    }

    private HBox makeLegendItemPremium() {
        HBox item = new HBox(7);
        item.setAlignment(Pos.CENTER);

        Label swatch = new Label();
        swatch.setMinSize(22, 22);
        swatch.setMaxSize(22, 22);
        swatch.setStyle(
            "-fx-background-color: rgba(34,197,94,0.18);" +
            "-fx-background-radius: 6px;" +
            "-fx-border-color: #F59E0B;" +
            "-fx-border-radius: 6px; -fx-border-width: 2.5px;"
        );

        Label lbl = new Label("Premium Tier");
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #FCD34D;");
        item.getChildren().addAll(swatch, lbl);
        return item;
    }

    /* ---------------------------------------------------------------- */
    /* CHECKOUT BAR                                                       */
    /* ---------------------------------------------------------------- */
    private HBox createCheckoutBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(16, 32, 16, 32));
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setStyle(
            "-fx-background-color: rgba(14,11,25,0.95);" +
            "-fx-border-color: rgba(139,92,246,0.22);" +
            "-fx-border-width: 1px 0 0 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.20), 16, 0, 0, -4);"
        );

        selectionCountLabel = new Label("Select seats to continue");
        selectionCountLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        totalLabel = new Label("₹  0.00");
        totalLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #22C55E;");

        totalPriceProperty.addListener((obs, oldVal, newVal) -> {
            totalLabel.setText(String.format("₹  %.2f", newVal.doubleValue()));
            int count = selectedSeats.size();
            if (count == 0) {
                selectionCountLabel.setText("Select seats to continue");
                selectionCountLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
            } else {
                selectionCountLabel.setText(count + " seat" + (count > 1 ? "s" : "") + " selected");
                selectionCountLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #C4B5FD;");
            }
            proceedBtn.setDisable(selectedSeats.isEmpty());
        });

        proceedBtn = new Button("Proceed to Payment  →");
        proceedBtn.getStyleClass().add("button-primary");
        proceedBtn.setStyle("-fx-font-size: 15px; -fx-padding: 12px 28px;");
        proceedBtn.setDisable(true);

        proceedBtn.setOnAction(e -> handleProceedToPayment());

        bar.getChildren().addAll(selectionCountLabel, spacer, totalLabel, proceedBtn);
        return bar;
    }

    private void handleProceedToPayment() {
        if (selectedSeats.isEmpty()) {
            AlertHelper.showWarning("No Seats Selected", "Choose Your Seats", "Please select at least one seat to continue.");
            return;
        }

        User user = AuthService.getCurrentUser();
        if (user == null) {
            AlertHelper.showError("Session Expired", "Auth Error", "Please log in again.");
            return;
        }

        List<Integer> seatIds = new ArrayList<>();
        for (Seat s : selectedSeats) seatIds.add(s.getId());

        try {
            bookingService.holdSeats(showtime.getId(), seatIds, user.getId());
            if (onProceedToPayment != null) {
                onProceedToPayment.accept(new ArrayList<>(selectedSeats));
            }
        } catch (ValidationException ve) {
            AlertHelper.showWarning("Seat Unavailable", "Could Not Hold Seats", ve.getMessage());
            loadSeatMap();
        } catch (SQLException ex) {
            AlertHelper.showError("Database Error", "Failed to Reserve Seats", ex.getMessage());
            loadSeatMap();
        }
    }

    /* ---------------------------------------------------------------- */
    /* SEAT MAP BUILDER                                                   */
    /* ---------------------------------------------------------------- */
    public void loadSeatMap() {
        seatGrid.getChildren().clear();
        selectedSeats.clear();
        totalPriceProperty.set(0.0);

        try {
            int currentUserId = AuthService.getCurrentUser() != null
                    ? AuthService.getCurrentUser().getId() : -1;

            List<Seat> seats = screenService.getSeatsForScreen(showtime.getScreenId());
            List<Integer> bookedSeatIds = bookingService.getBookedSeats(showtime.getId());
            List<Integer> heldSeatIds = bookingService.getHeldSeatsByOthers(showtime.getId(), currentUserId);

            Map<String, List<Seat>> rows = new LinkedHashMap<>();
            for (Seat seat : seats) {
                rows.computeIfAbsent(seat.getRowLabel(), k -> new ArrayList<>()).add(seat);
            }

            int rowIndex = 0;
            for (Map.Entry<String, List<Seat>> entry : rows.entrySet()) {
                String rowLabel = entry.getKey();
                List<Seat> rowSeats = entry.getValue();

                Label rowLbl = new Label(rowLabel);
                rowLbl.setStyle(
                    "-fx-font-size: 11px; -fx-font-weight: 800;" +
                    "-fx-text-fill: #6B7280; -fx-min-width: 22px;"
                );
                rowLbl.setAlignment(Pos.CENTER);
                seatGrid.add(rowLbl, 0, rowIndex);

                for (int colIndex = 0; colIndex < rowSeats.size(); colIndex++) {
                    // Aisle gap after column 4 for wide screens
                    int colOffset = colIndex + 1;
                    if (colIndex >= 4) colOffset += 1;  // 1-column aisle gap

                    Button seatBtn = createSeatButton(rowSeats.get(colIndex), bookedSeatIds, heldSeatIds);
                    seatGrid.add(seatBtn, colOffset, rowIndex);
                }
                rowIndex++;
            }

        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to Load Seats", e.getMessage());
        }
    }

    private Button createSeatButton(Seat seat, List<Integer> bookedIds, List<Integer> heldIds) {
        Button btn = new Button(seat.getSeatCode());
        btn.getStyleClass().add("seat-button");

        boolean isBooked  = bookedIds.contains(seat.getId());
        boolean isHeld    = heldIds.contains(seat.getId());
        boolean isPremium = seat.getSeatType() == SeatType.PREMIUM;

        BigDecimal price = isPremium ? showtime.getPricePremium() : showtime.getPriceRegular();

        if (isPremium) btn.getStyleClass().add("seat-premium");

        if (isBooked) {
            btn.getStyleClass().add("seat-booked");
            btn.setDisable(true);
            btn.setTooltip(new Tooltip("Seat " + seat.getSeatCode() + "  •  Booked"));
        } else if (isHeld) {
            btn.getStyleClass().add("seat-held");
            btn.setDisable(true);
            btn.setTooltip(new Tooltip("Seat " + seat.getSeatCode() + "  •  Held by another customer"));
        } else {
            btn.getStyleClass().add("seat-available");
            String tipText = seat.getSeatCode() + "  •  " + seat.getSeatType() + "  •  ₹ " + price.toPlainString();
            Tooltip tip = new Tooltip(tipText);
            tip.setShowDelay(Duration.millis(200));
            btn.setTooltip(tip);

            btn.setOnAction(e -> toggleSeatSelection(btn, seat, price, isPremium));
        }

        return btn;
    }

    private void toggleSeatSelection(Button btn, Seat seat, BigDecimal price, boolean isPremium) {
        if (selectedSeats.contains(seat)) {
            selectedSeats.remove(seat);
            btn.getStyleClass().remove("seat-selected");
            btn.getStyleClass().add("seat-available");
            if (isPremium) {
                btn.getStyleClass().remove("seat-premium");
                btn.getStyleClass().add("seat-premium");
            }
            totalPriceProperty.set(totalPriceProperty.get() - price.doubleValue());

            // Deselect animation
            ScaleTransition st = new ScaleTransition(Duration.millis(120), btn);
            st.setFromX(1.1);
            st.setToX(1.0);
            st.setFromY(1.1);
            st.setToY(1.0);
            st.play();

        } else {
            selectedSeats.add(seat);
            btn.getStyleClass().remove("seat-available");
            btn.getStyleClass().add("seat-selected");
            totalPriceProperty.set(totalPriceProperty.get() + price.doubleValue());

            // Select animation
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setFromX(1.0);
            st.setToX(1.12);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.setInterpolator(Interpolator.EASE_BOTH);
            st.play();
        }
    }
}

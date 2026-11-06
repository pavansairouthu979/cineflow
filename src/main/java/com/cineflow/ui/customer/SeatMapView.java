package com.cineflow.ui.customer;

import com.cineflow.model.*;
import com.cineflow.service.AuthService;
import com.cineflow.service.BookingService;
import com.cineflow.service.ScreenService;
import com.cineflow.service.ValidationException;
import com.cineflow.util.AlertHelper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Interactive JavaFX Seat Map representation for selecting auditorium seats.
 * Handles selection, live price calculation, hover effects, and legends.
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

        // Top Toolbar
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Center Content: Cinema Screen Curve + Seat Grid + Legend
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(20, 30, 20, 30));

        // Movie Info Bar
        HBox infoBar = createInfoBar();
        centerBox.getChildren().add(infoBar);

        // Cinema Screen Graphic
        VBox screenGraphic = createCinemaScreenGraphic();
        centerBox.getChildren().add(screenGraphic);

        // Interactive Seat Grid
        seatGrid = new GridPane();
        seatGrid.setHgap(8);
        seatGrid.setVgap(8);
        seatGrid.setAlignment(Pos.CENTER);
        centerBox.getChildren().add(seatGrid);

        // Seat Legend
        HBox legendBox = createLegendBox();
        centerBox.getChildren().add(legendBox);

        ScrollPane scroll = new ScrollPane(centerBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scroll);

        // Bottom Price Summary & Checkout Action Bar
        HBox bottomBar = createBottomSummaryBar();
        root.setBottom(bottomBar);

        // Build the seat buttons from database
        loadSeatMap();

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(12);
        topBar.setPadding(new Insets(14, 30, 14, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #111a2e; -fx-border-color: #232d48; -fx-border-width: 0 0 1px 0;");

        Button backBtn = new Button("← Back to Showtimes");
        backBtn.getStyleClass().add("button-secondary");
        // ActionEvent: Returns to the showtime selection view
        backBtn.setOnAction(e -> onBack.run());

        Label title = new Label("Select Your Seats • " + showtime.getScreenName());
        title.getStyleClass().add("title-medium");

        topBar.getChildren().addAll(backBtn, title);
        return topBar;
    }

    private HBox createInfoBar() {
        HBox bar = new HBox(20);
        bar.setAlignment(Pos.CENTER);
        bar.getStyleClass().add("card");
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setMaxWidth(800);

        Label movieLbl = new Label("🎬 " + showtime.getMovieTitle());
        movieLbl.getStyleClass().add("title-small");

        Label timeLbl = new Label("🕒 " + showtime.getFormattedStartTime());
        timeLbl.getStyleClass().add("text-body");

        Label regLbl = new Label("Regular: ₹ " + showtime.getPriceRegular().toPlainString());
        regLbl.getStyleClass().add("text-muted");

        Label premLbl = new Label("Premium: ₹ " + showtime.getPricePremium().toPlainString());
        premLbl.getStyleClass().add("text-muted");
        premLbl.setStyle("-fx-text-fill: #fbbf24;");

        bar.getChildren().addAll(movieLbl, timeLbl, regLbl, premLbl);
        return bar;
    }

    private VBox createCinemaScreenGraphic() {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(680);

        Region screenBar = new Region();
        screenBar.getStyleClass().add("cinema-screen-bar");
        screenBar.setPrefWidth(600);

        Label screenLabel = new Label("ALL EYES THIS WAY • CINEMA SCREEN");
        screenLabel.getStyleClass().add("text-muted");
        screenLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 2px;");

        box.getChildren().addAll(screenBar, screenLabel);
        return box;
    }

    private HBox createLegendBox() {
        HBox box = new HBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        box.getChildren().addAll(
                createLegendItem("Available", "seat-available", false),
                createLegendItem("Selected", "seat-selected", false),
                createLegendItem("Held", "seat-held", false),
                createLegendItem("Booked", "seat-booked", false),
                createLegendItem("Premium Tier", "seat-available", true)
        );
        return box;
    }

    private HBox createLegendItem(String label, String styleClass, boolean premiumBorder) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER);

        Button sample = new Button();
        sample.setMinSize(18, 18);
        sample.setMaxSize(18, 18);
        sample.getStyleClass().add(styleClass);
        if (premiumBorder) {
            sample.getStyleClass().add("seat-premium");
        }

        Label lbl = new Label(label);
        lbl.getStyleClass().add("text-muted");
        item.getChildren().addAll(sample, lbl);
        return item;
    }

    private HBox createBottomSummaryBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(14, 30, 14, 30));
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setStyle("-fx-background-color: #111a2e; -fx-border-color: #232d48; -fx-border-width: 1px 0 0 0;");

        selectionCountLabel = new Label("Selected: 0 seat(s)");
        selectionCountLabel.getStyleClass().add("text-body");

        totalLabel = new Label("Total: ₹ 0.00");
        totalLabel.getStyleClass().add("title-medium");
        totalLabel.setStyle("-fx-text-fill: #34d399;");

        // Property listener: updates total label whenever totalPriceProperty changes
        totalPriceProperty.addListener((obs, oldVal, newVal) -> {
            totalLabel.setText(String.format("Total: ₹ %.2f", newVal.doubleValue()));
            selectionCountLabel.setText("Selected: " + selectedSeats.size() + " seat(s)");
            proceedBtn.setDisable(selectedSeats.isEmpty());
        });

        proceedBtn = new Button("Proceed to Payment →");
        proceedBtn.getStyleClass().add("button-primary");
        proceedBtn.setDisable(true);

        // ActionEvent: Proceed to payment with selected seats and place 5-minute holds
        proceedBtn.setOnAction(e -> {
            if (selectedSeats.isEmpty()) {
                AlertHelper.showWarning("No Seats", "Seat Selection Required", "Please choose at least one seat.");
                return;
            }

            User user = AuthService.getCurrentUser();
            if (user == null) {
                AlertHelper.showError("Auth Error", "Session Expired", "Please log in again.");
                return;
            }

            List<Integer> seatIds = new ArrayList<>();
            for (Seat s : selectedSeats) {
                seatIds.add(s.getId());
            }

            try {
                // Reserve temporary 5-minute hold in database
                bookingService.holdSeats(showtime.getId(), seatIds, user.getId());

                // Launch payment dialog
                if (onProceedToPayment != null) {
                    onProceedToPayment.accept(new ArrayList<>(selectedSeats));
                }
            } catch (ValidationException ve) {
                AlertHelper.showWarning("Seat Unavailable", "Could not hold seats", ve.getMessage());
                loadSeatMap();
            } catch (SQLException ex) {
                AlertHelper.showError("Database Error", "Failed to reserve seats", ex.getMessage());
                loadSeatMap();
            }
        });

        bar.getChildren().addAll(selectionCountLabel, totalLabel, proceedBtn);
        return bar;
    }

    public void loadSeatMap() {
        seatGrid.getChildren().clear();
        selectedSeats.clear();
        totalPriceProperty.set(0.0);

        try {
            int currentUserId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : -1;

            // Fetch seats for this screen
            List<Seat> seats = screenService.getSeatsForScreen(showtime.getScreenId());
            // Fetch booked seats
            List<Integer> bookedSeatIds = bookingService.getBookedSeats(showtime.getId());
            // Fetch held seats by other customers
            List<Integer> heldSeatIds = bookingService.getHeldSeatsByOthers(showtime.getId(), currentUserId);

            // Group seats by row
            Map<String, List<Seat>> rows = new LinkedHashMap<>();
            for (Seat seat : seats) {
                rows.computeIfAbsent(seat.getRowLabel(), k -> new ArrayList<>()).add(seat);
            }

            int rowIndex = 0;
            for (Map.Entry<String, List<Seat>> entry : rows.entrySet()) {
                String rowLabel = entry.getKey();
                List<Seat> rowSeats = entry.getValue();

                // Row header label
                Label rowLbl = new Label(rowLabel);
                rowLbl.getStyleClass().add("title-small");
                rowLbl.setPrefWidth(24);
                rowLbl.setAlignment(Pos.CENTER);
                seatGrid.add(rowLbl, 0, rowIndex);

                for (int colIndex = 0; colIndex < rowSeats.size(); colIndex++) {
                    Seat seat = rowSeats.get(colIndex);
                    Button seatBtn = createSeatButton(seat, bookedSeatIds, heldSeatIds);
                    seatGrid.add(seatBtn, colIndex + 1, rowIndex);
                }
                rowIndex++;
            }

        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to load seat layout", e.getMessage());
        }
    }

    private Button createSeatButton(Seat seat, List<Integer> bookedIds, List<Integer> heldIds) {
        Button btn = new Button(seat.getSeatCode());
        btn.getStyleClass().add("seat-button");

        boolean isBooked = bookedIds.contains(seat.getId());
        boolean isHeld = heldIds.contains(seat.getId());
        boolean isPremium = seat.getSeatType() == SeatType.PREMIUM;

        BigDecimal price = isPremium ? showtime.getPricePremium() : showtime.getPriceRegular();

        if (isPremium) {
            btn.getStyleClass().add("seat-premium");
        }

        if (isBooked) {
            btn.getStyleClass().add("seat-booked");
            btn.setDisable(true);
            btn.setTooltip(new Tooltip("Seat " + seat.getSeatCode() + " • Already Booked"));
        } else if (isHeld) {
            btn.getStyleClass().add("seat-held");
            btn.setDisable(true);
            btn.setTooltip(new Tooltip("Seat " + seat.getSeatCode() + " • Temporary Hold by another customer"));
        } else {
            btn.getStyleClass().add("seat-available");
            btn.setTooltip(new Tooltip(seat.getSeatCode() + " • " + seat.getSeatType() + " • ₹ " + price.toPlainString()));

            // MouseEvent: Handles seat selection and deselection
            btn.setOnAction(e -> {
                if (selectedSeats.contains(seat)) {
                    selectedSeats.remove(seat);
                    btn.getStyleClass().remove("seat-selected");
                    btn.getStyleClass().add("seat-available");
                    totalPriceProperty.set(totalPriceProperty.get() - price.doubleValue());
                } else {
                    selectedSeats.add(seat);
                    btn.getStyleClass().remove("seat-available");
                    btn.getStyleClass().add("seat-selected");
                    totalPriceProperty.set(totalPriceProperty.get() + price.doubleValue());
                }
            });

            // MouseEvent: Hover styling
            btn.setOnMouseEntered(e -> {
                if (!selectedSeats.contains(seat)) {
                    btn.setStyle("-fx-scale-x: 1.1; -fx-scale-y: 1.1;");
                }
            });

            // MouseEvent: Remove hover styling on exit
            btn.setOnMouseExited(e -> btn.setStyle(""));
        }

        return btn;
    }
}


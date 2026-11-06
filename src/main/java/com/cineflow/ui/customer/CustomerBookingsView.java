package com.cineflow.ui.customer;

import com.cineflow.model.Booking;
import com.cineflow.model.User;
import com.cineflow.service.AuthService;
import com.cineflow.service.BookingService;
import com.cineflow.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * CineFlow — Customer Bookings History View.
 *
 * KEY FIX: getView() builds the UI exactly ONCE and caches the root.
 * refreshBookings() only updates the ObservableList — it never re-creates
 * the TableView. This means calling showMyBookings() repeatedly always
 * shows fresh DB data in the same stable table.
 */
public class CustomerBookingsView {

    private final BookingService bookingService;
    private final Consumer<Booking> onViewTicket;

    // Cached root — built once, reused on every call to getView()
    private VBox cachedRoot;
    private TableView<Booking> tableView;
    private ObservableList<Booking> bookingData;

    public CustomerBookingsView(Consumer<Booking> onViewTicket) {
        this.bookingService = new BookingService();
        this.onViewTicket = onViewTicket;
    }

    /**
     * Returns the cached root view, building it on first call only.
     * Subsequent calls just return the same node (already in the scene graph)
     * after refreshing the data.
     */
    public Parent getView() {
        if (cachedRoot == null) {
            cachedRoot = buildView();
        }
        // Always refresh data whenever this view is shown
        refreshBookings();
        return cachedRoot;
    }

    /* ---------------------------------------------------------------- */
    /* BUILD (called once only)                                          */
    /* ---------------------------------------------------------------- */
    private VBox buildView() {
        VBox root = new VBox(0);
        root.getStyleClass().add("app-container");

        // Section header
        root.getChildren().add(createSectionHeader());

        // Toolbar with action buttons
        root.getChildren().add(createToolbar());

        // Table (created once, data updated via bookingData list)
        tableView = createTableView();
        VBox tableWrapper = new VBox(tableView);
        tableWrapper.setPadding(new Insets(0, 24, 24, 24));
        VBox.setVgrow(tableWrapper, Priority.ALWAYS);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        root.getChildren().add(tableWrapper);

        return root;
    }

    /* ---------------------------------------------------------------- */
    /* SECTION HEADER                                                     */
    /* ---------------------------------------------------------------- */
    private HBox createSectionHeader() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(28, 28, 16, 28));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, rgba(139,92,246,0.10), transparent);" +
            "-fx-border-color: rgba(139,92,246,0.12);" +
            "-fx-border-width: 0 0 1px 0;"
        );

        Label title = new Label("My Reservations");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #F8FAFC;");

        Label dotSep = new Label("✦");
        dotSep.setStyle("-fx-text-fill: #8B5CF6;");

        Label subtitle = new Label("Your complete booking history");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #A1A1AA;");

        header.getChildren().addAll(title, dotSep, subtitle);
        return header;
    }

    /* ---------------------------------------------------------------- */
    /* TOOLBAR                                                            */
    /* ---------------------------------------------------------------- */
    private HBox createToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 24, 14, 24));
        bar.setStyle(
            "-fx-background-color: rgba(21,19,35,0.60);" +
            "-fx-border-color: rgba(139,92,246,0.10);" +
            "-fx-border-width: 0 0 1px 0;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewTicketBtn = new Button("🎟  View Ticket");
        viewTicketBtn.getStyleClass().add("button-primary");
        viewTicketBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 18px;");
        viewTicketBtn.setOnAction(e -> {
            Booking selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null && onViewTicket != null) {
                onViewTicket.accept(selected);
            } else {
                AlertHelper.showWarning("Select a Booking",
                        "No Booking Selected", "Please click on a booking row first.");
            }
        });

        Button cancelBtn = new Button("✕  Cancel Booking");
        cancelBtn.getStyleClass().add("button-danger");
        cancelBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 18px;");
        cancelBtn.setOnAction(e -> handleCancellation());

        Button refreshBtn = new Button("↻  Refresh");
        refreshBtn.getStyleClass().add("button-secondary");
        refreshBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 14px;");
        refreshBtn.setOnAction(e -> refreshBookings());

        bar.getChildren().addAll(spacer, viewTicketBtn, cancelBtn, refreshBtn);
        return bar;
    }

    /* ---------------------------------------------------------------- */
    /* TABLE (built once)                                                 */
    /* ---------------------------------------------------------------- */
    private TableView<Booking> createTableView() {
        TableView<Booking> table = new TableView<>();

        // Single ObservableList shared for the lifetime of this view
        bookingData = FXCollections.observableArrayList();
        table.setItems(bookingData);

        table.setStyle("-fx-background-color: transparent;");
        table.setPlaceholder(createEmptyPlaceholder());

        TableColumn<Booking, Integer> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(95);

        TableColumn<Booking, String> movieCol = new TableColumn<>("Movie");
        movieCol.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        movieCol.setPrefWidth(190);

        TableColumn<Booking, String> screenCol = new TableColumn<>("Auditorium");
        screenCol.setCellValueFactory(new PropertyValueFactory<>("screenName"));
        screenCol.setPrefWidth(140);

        TableColumn<Booking, String> timeCol = new TableColumn<>("Showtime");
        timeCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFormattedShowtimeStart()));
        timeCol.setPrefWidth(155);

        TableColumn<Booking, String> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seatNumbers"));
        seatsCol.setPrefWidth(130);

        TableColumn<Booking, String> totalCol = new TableColumn<>("Total Paid");
        totalCol.setCellValueFactory(c ->
                new SimpleStringProperty("₹ " + c.getValue().getTotalAmount().toPlainString()));
        totalCol.setPrefWidth(110);

        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatus().name()));
        statusCol.setPrefWidth(105);
        // Status badge renderer
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().add("CONFIRMED".equals(item) ? "badge-success" : "badge-danger");
                badge.setStyle("-fx-font-size: 11px;");
                setGraphic(badge);
                setText(null);
            }
        });

        TableColumn<Booking, String> bookedCol = new TableColumn<>("Booked On");
        bookedCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFormattedBookingTime()));
        bookedCol.setPrefWidth(150);

        table.getColumns().addAll(
                idCol, movieCol, screenCol, timeCol,
                seatsCol, totalCol, statusCol, bookedCol
        );

        // Double-click row → view ticket
        table.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Booking selected = table.getSelectionModel().getSelectedItem();
                if (selected != null && onViewTicket != null) {
                    onViewTicket.accept(selected);
                }
            }
        });

        return table;
    }

    private Label createEmptyPlaceholder() {
        Label lbl = new Label(
            "🎬  No bookings yet.\n" +
            "Browse movies and reserve your first seats!"
        );
        lbl.setStyle(
            "-fx-font-size: 14px; -fx-text-fill: #6B7280;" +
            "-fx-padding: 50px; -fx-text-alignment: center;"
        );
        return lbl;
    }

    /* ---------------------------------------------------------------- */
    /* DATA REFRESH (updates existing list, never rebuilds the table)    */
    /* ---------------------------------------------------------------- */
    public void refreshBookings() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        // bookingData may not be initialised yet if getView() hasn't been
        // called (e.g. refreshBookings called before first getView call from
        // CustomerDashboardView constructor). Guard against that.
        if (bookingData == null) return;

        try {
            List<Booking> list = bookingService.getCustomerBookings(user.getId());
            bookingData.setAll(list);    // replaces all rows in the existing table
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to Load Bookings", e.getMessage());
        }
    }

    /* ---------------------------------------------------------------- */
    /* CANCELLATION                                                       */
    /* ---------------------------------------------------------------- */
    private void handleCancellation() {
        if (tableView == null) return;
        Booking selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Select a Booking",
                    "No Booking Selected", "Please click on a booking row first.");
            return;
        }

        if (!selected.canCancel()) {
            AlertHelper.showWarning("Cannot Cancel",
                    "Cancellation Not Available",
                    "This booking cannot be cancelled — the screening has already started, " +
                    "or it was previously cancelled.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation(
                "Cancel Booking",
                "Cancel Reservation #" + selected.getId() + "?",
                "Cancel your tickets for \"" + selected.getMovieTitle() + "\"?\n" +
                "A refund of ₹ " + selected.getTotalAmount() + " will be processed."
        );

        if (confirm) {
            User user = AuthService.getCurrentUser();
            try {
                boolean cancelled = bookingService.cancelBooking(selected.getId(), user.getId());
                if (cancelled) {
                    AlertHelper.showInfo("Booking Cancelled", "Refund Initiated",
                            "Reservation #" + selected.getId() +
                            " cancelled. Refund processed successfully.");
                    refreshBookings();   // reload table in place — no rebuild
                }
            } catch (SQLException e) {
                AlertHelper.showError("Cancellation Failed", "Transaction Error", e.getMessage());
            }
        }
    }
}

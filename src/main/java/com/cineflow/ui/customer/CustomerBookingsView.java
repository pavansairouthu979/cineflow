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
 * Customer view displaying own reservation history, ticket viewing,
 * and pre-showtime cancellation with refund processing.
 */
public class CustomerBookingsView {

    private final BookingService bookingService;
    private final Consumer<Booking> onViewTicket;
    private TableView<Booking> tableView;
    private ObservableList<Booking> bookingData;

    public CustomerBookingsView(Consumer<Booking> onViewTicket) {
        this.bookingService = new BookingService();
        this.onViewTicket = onViewTicket;
    }

    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        // Top Toolbar
        HBox topBar = createTopBar();
        root.getChildren().add(topBar);

        // TableView
        tableView = createTableView();
        VBox.setVgrow(tableView, Priority.ALWAYS);
        root.getChildren().add(tableView);

        // Initial Data Load
        refreshBookings();

        return root;
    }

    private HBox createTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("My Movie Reservations");
        title.getStyleClass().add("title-medium");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewTicketBtn = new Button("View Ticket");
        viewTicketBtn.getStyleClass().add("button-primary");
        // ActionEvent: Displays ticket summary for the selected booking
        viewTicketBtn.setOnAction(e -> {
            Booking selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null && onViewTicket != null) {
                onViewTicket.accept(selected);
            } else {
                AlertHelper.showWarning("Selection Required", "No Booking Selected", "Please select a booking to view its ticket.");
            }
        });

        Button cancelBtn = new Button("Cancel Booking");
        cancelBtn.getStyleClass().add("button-danger");
        // ActionEvent: Cancels booking if showtime has not yet started
        cancelBtn.setOnAction(e -> handleCancellation());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("button-secondary");
        // ActionEvent: Refreshes booking records
        refreshBtn.setOnAction(e -> refreshBookings());

        bar.getChildren().addAll(title, spacer, viewTicketBtn, cancelBtn, refreshBtn);
        return bar;
    }

    private TableView<Booking> createTableView() {
        TableView<Booking> table = new TableView<>();
        bookingData = FXCollections.observableArrayList();
        table.setItems(bookingData);

        TableColumn<Booking, Integer> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);

        TableColumn<Booking, String> movieCol = new TableColumn<>("Movie");
        movieCol.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        movieCol.setPrefWidth(180);

        TableColumn<Booking, String> screenCol = new TableColumn<>("Auditorium");
        screenCol.setCellValueFactory(new PropertyValueFactory<>("screenName"));
        screenCol.setPrefWidth(140);

        TableColumn<Booking, String> timeCol = new TableColumn<>("Showtime");
        timeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedShowtimeStart()));
        timeCol.setPrefWidth(140);

        TableColumn<Booking, String> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seatNumbers"));
        seatsCol.setPrefWidth(110);

        TableColumn<Booking, String> totalCol = new TableColumn<>("Total Paid");
        totalCol.setCellValueFactory(cellData -> new SimpleStringProperty("₹ " + cellData.getValue().getTotalAmount().toPlainString()));
        totalCol.setPrefWidth(100);

        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));
        statusCol.setPrefWidth(100);

        TableColumn<Booking, String> bookedCol = new TableColumn<>("Booked On");
        bookedCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedBookingTime()));
        bookedCol.setPrefWidth(140);

        table.getColumns().addAll(idCol, movieCol, screenCol, timeCol, seatsCol, totalCol, statusCol, bookedCol);

        // MouseEvent: Double-click row to view ticket
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

    public void refreshBookings() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        try {
            List<Booking> list = bookingService.getCustomerBookings(user.getId());
            bookingData.setAll(list);
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to load bookings", e.getMessage());
        }
    }

    private void handleCancellation() {
        Booking selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Selection Required", "No Booking Selected", "Please select a booking to cancel.");
            return;
        }

        if (!selected.canCancel()) {
            AlertHelper.showWarning(
                    "Cancellation Ineligible",
                    "Cannot Cancel Reservation",
                    "This booking cannot be cancelled because the screening has already started or the booking was previously cancelled."
            );
            return;
        }

        boolean confirm = AlertHelper.showConfirmation(
                "Confirm Cancellation",
                "Cancel Reservation #" + selected.getId() + "?",
                "Are you sure you want to cancel your tickets for " + selected.getMovieTitle() + "?\n" +
                "A refund of ₹ " + selected.getTotalAmount() + " will be recorded."
        );

        if (confirm) {
            User user = AuthService.getCurrentUser();
            try {
                boolean cancelled = bookingService.cancelBooking(selected.getId(), user.getId());
                if (cancelled) {
                    AlertHelper.showInfo("Booking Cancelled", "Refund Processed",
                            "Reservation #" + selected.getId() + " was cancelled and refunded successfully.");
                    refreshBookings();
                }
            } catch (SQLException e) {
                AlertHelper.showError("Cancellation Failed", "Database Transaction Error", e.getMessage());
            }
        }
    }
}

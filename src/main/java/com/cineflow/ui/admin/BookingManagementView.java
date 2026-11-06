package com.cineflow.ui.admin;

import com.cineflow.model.Booking;
import com.cineflow.model.BookingStatus;
import com.cineflow.model.Movie;
import com.cineflow.service.BookingService;
import com.cineflow.service.MovieService;
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
import java.time.LocalDate;
import java.util.List;

/**
 * Admin view for managing and filtering customer bookings using SQL JOINs.
 */
public class BookingManagementView {

    private final BookingService bookingService;
    private final MovieService movieService;

    private TableView<Booking> tableView;
    private ObservableList<Booking> bookingData;

    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> statusFilter;
    private ComboBox<Movie> movieFilter;
    private TextField userSearchField;

    public BookingManagementView() {
        this.bookingService = new BookingService();
        this.movieService = new MovieService();
    }

    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        // Filter Controls
        VBox filterCard = createFilterBar();
        root.getChildren().add(filterCard);

        // TableView
        tableView = createTableView();
        VBox.setVgrow(tableView, Priority.ALWAYS);
        root.getChildren().add(tableView);

        // Initial search
        applyFilters();

        return root;
    }

    private VBox createFilterBar() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(14, 18, 14, 18));

        Label filterTitle = new Label("🔍 Filter & Inspect Customer Reservations (SQL JOIN Queries)");
        filterTitle.getStyleClass().add("title-small");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.setPrefWidth(125);

        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        endDatePicker.setPrefWidth(125);

        statusFilter = new ComboBox<>(FXCollections.observableArrayList("ALL", "CONFIRMED", "CANCELLED"));
        statusFilter.setValue("ALL");
        statusFilter.setPrefWidth(130);

        movieFilter = new ComboBox<>();
        movieFilter.setPromptText("Filter by Movie");
        movieFilter.setPrefWidth(160);
        loadMovieFilterOptions();

        userSearchField = new TextField();
        userSearchField.setPromptText("👤 Username...");
        userSearchField.setPrefWidth(140);

        Button applyBtn = new Button("🔍 Apply Filters");
        applyBtn.getStyleClass().add("button-primary");
        applyBtn.setOnAction(e -> applyFilters());

        Button resetBtn = new Button("🔄 Reset");
        resetBtn.getStyleClass().add("button-secondary");
        resetBtn.setOnAction(e -> {
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            statusFilter.setValue("ALL");
            movieFilter.setValue(null);
            userSearchField.clear();
            applyFilters();
        });

        row.getChildren().addAll(
                new Label("From:"), startDatePicker,
                new Label("To:"), endDatePicker,
                statusFilter, movieFilter, userSearchField,
                applyBtn, resetBtn
        );

        card.getChildren().addAll(filterTitle, row);
        return card;
    }

    private TableView<Booking> createTableView() {
        TableView<Booking> table = new TableView<>();
        bookingData = FXCollections.observableArrayList();
        table.setItems(bookingData);

        TableColumn<Booking, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Booking, String> userCol = new TableColumn<>("👤 Customer");
        userCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCustomerName() + " (@" + cellData.getValue().getUsername() + ")"));
        userCol.setPrefWidth(160);

        TableColumn<Booking, String> movieCol = new TableColumn<>("🎬 Movie");
        movieCol.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        movieCol.setPrefWidth(150);

        TableColumn<Booking, String> screenCol = new TableColumn<>("🏛 Auditorium");
        screenCol.setCellValueFactory(new PropertyValueFactory<>("screenName"));
        screenCol.setPrefWidth(120);

        TableColumn<Booking, String> showtimeCol = new TableColumn<>("🕒 Showtime");
        showtimeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedShowtimeStart()));
        showtimeCol.setPrefWidth(130);

        TableColumn<Booking, String> seatsCol = new TableColumn<>("🎟 Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seatNumbers"));
        seatsCol.setPrefWidth(95);

        TableColumn<Booking, String> amountCol = new TableColumn<>("💵 Total Paid");
        amountCol.setCellValueFactory(cellData -> new SimpleStringProperty("₹ " + cellData.getValue().getTotalAmount().toPlainString()));
        amountCol.setPrefWidth(100);

        TableColumn<Booking, String> payCol = new TableColumn<>("💳 Payment");
        payCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPaymentMethod() != null ? cellData.getValue().getPaymentMethod().name() : "N/A"));
        payCol.setPrefWidth(85);

        TableColumn<Booking, String> statusCol = new TableColumn<>("⚡ Status");
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));
        statusCol.setPrefWidth(95);

        TableColumn<Booking, String> timeCol = new TableColumn<>("📅 Booked At");
        timeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedBookingTime()));
        timeCol.setPrefWidth(135);

        table.getColumns().addAll(idCol, userCol, movieCol, screenCol, showtimeCol, seatsCol, amountCol, payCol, statusCol, timeCol);

        // MouseEvent: Double-click an admin booking row to view complete details
        table.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Booking selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showBookingDetails(selected);
                }
            }
        });

        return table;
    }

    private void loadMovieFilterOptions() {
        try {
            List<Movie> movies = movieService.getAllMovies();
            movieFilter.setItems(FXCollections.observableArrayList(movies));
        } catch (SQLException ignored) {}
    }

    public void applyFilters() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        BookingStatus status = null;
        if (statusFilter.getValue() != null && !statusFilter.getValue().equals("ALL")) {
            status = BookingStatus.fromString(statusFilter.getValue());
        }

        Integer movieId = movieFilter.getValue() != null ? movieFilter.getValue().getId() : null;
        String username = userSearchField.getText();

        try {
            List<Booking> list = bookingService.getFilteredAdminBookings(start, end, movieId, status, username);
            bookingData.setAll(list);
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to load bookings", e.getMessage());
        }
    }

    private void showBookingDetails(Booking b) {
        AlertHelper.showInfo(
                "Booking Details",
                "Reservation #" + b.getId() + " • " + b.getStatus(),
                "Customer: " + b.getCustomerName() + " (@" + b.getUsername() + ")\n" +
                "Movie: " + b.getMovieTitle() + "\n" +
                "Auditorium: " + b.getScreenName() + "\n" +
                "Showtime: " + b.getFormattedShowtimeStart() + "\n" +
                "Seats: " + b.getSeatNumbers() + "\n" +
                "Total Amount: ₹ " + b.getTotalAmount() + "\n" +
                "Payment Method: " + b.getPaymentMethod() + "\n" +
                "Booking Time: " + b.getFormattedBookingTime()
        );
    }
}


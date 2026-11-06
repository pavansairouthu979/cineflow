package com.cineflow.ui.admin;

import com.cineflow.model.Movie;
import com.cineflow.model.Screen;
import com.cineflow.model.Showtime;
import com.cineflow.service.MovieService;
import com.cineflow.service.ScreenService;
import com.cineflow.service.ShowtimeService;
import com.cineflow.service.ValidationException;
import com.cineflow.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Admin view for scheduling and managing movie screenings with overlap prevention.
 */
public class ShowtimeManagementView {

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final ScreenService screenService;

    private TableView<Showtime> tableView;
    private ObservableList<Showtime> showtimeData;

    public ShowtimeManagementView() {
        this.showtimeService = new ShowtimeService();
        this.movieService = new MovieService();
        this.screenService = new ScreenService();
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

        // Initial load
        refreshTable();

        return root;
    }

    private HBox createTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Showtime Scheduling & Management");
        title.getStyleClass().add("title-medium");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Schedule Showtime");
        addBtn.getStyleClass().add("button-primary");

        // ActionEvent: Opens dialog to schedule a new showtime
        addBtn.setOnAction(e -> openScheduleDialog());

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");

        // ActionEvent: Deletes selected showtime
        deleteBtn.setOnAction(e -> deleteSelectedShowtime());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("button-secondary");

        // ActionEvent: Refreshes showtime list
        refreshBtn.setOnAction(e -> refreshTable());

        bar.getChildren().addAll(title, spacer, addBtn, deleteBtn, refreshBtn);
        return bar;
    }

    private TableView<Showtime> createTableView() {
        TableView<Showtime> table = new TableView<>();
        showtimeData = FXCollections.observableArrayList();
        table.setItems(showtimeData);

        TableColumn<Showtime, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Showtime, String> movieCol = new TableColumn<>("Movie Title");
        movieCol.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        movieCol.setPrefWidth(180);

        TableColumn<Showtime, String> screenCol = new TableColumn<>("Auditorium");
        screenCol.setCellValueFactory(new PropertyValueFactory<>("screenName"));
        screenCol.setPrefWidth(150);

        TableColumn<Showtime, String> timeCol = new TableColumn<>("Start Time");
        timeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedStartTime()));
        timeCol.setPrefWidth(140);

        TableColumn<Showtime, String> durCol = new TableColumn<>("Duration");
        durCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDurationMinutes() + " mins"));
        durCol.setPrefWidth(90);

        TableColumn<Showtime, String> regCol = new TableColumn<>("Regular Price");
        regCol.setCellValueFactory(cellData -> new SimpleStringProperty("₹ " + cellData.getValue().getPriceRegular().toPlainString()));
        regCol.setPrefWidth(100);

        TableColumn<Showtime, String> premCol = new TableColumn<>("Premium Price");
        premCol.setCellValueFactory(cellData -> new SimpleStringProperty("₹ " + cellData.getValue().getPricePremium().toPlainString()));
        premCol.setPrefWidth(110);

        table.getColumns().addAll(idCol, movieCol, screenCol, timeCol, durCol, regCol, premCol);
        return table;
    }

    public void refreshTable() {
        try {
            List<Showtime> list = showtimeService.getAllShowtimes();
            showtimeData.setAll(list);
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to load showtimes", e.getMessage());
        }
    }

    private void deleteSelectedShowtime() {
        Showtime selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Selection Required", "No Showtime Selected", "Please select a showtime to delete.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation(
                "Confirm Deletion",
                "Delete Showtime?",
                "Are you sure you want to delete this showtime for " + selected.getMovieTitle() + "?"
        );

        if (confirm) {
            try {
                showtimeService.deleteShowtime(selected.getId());
                refreshTable();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Cannot delete showtime (bookings may exist)", e.getMessage());
            }
        }
    }

    private void openScheduleDialog() {
        try {
            List<Movie> activeMovies = movieService.getActiveMovies();
            List<Screen> allScreens = screenService.getAllScreens();

            if (activeMovies.isEmpty()) {
                AlertHelper.showWarning("Prerequisite Missing", "No Active Movies", "Please add at least one active movie first.");
                return;
            }
            if (allScreens.isEmpty()) {
                AlertHelper.showWarning("Prerequisite Missing", "No Screens", "Please add at least one auditorium screen first.");
                return;
            }

            Dialog<Showtime> dialog = new Dialog<>();
            dialog.setTitle("Schedule New Showtime");
            dialog.setHeaderText("Specify movie, screen, start time, and ticket pricing");

            ButtonType scheduleBtn = new ButtonType("Schedule", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(scheduleBtn, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(12);
            grid.setPadding(new Insets(20));

            ComboBox<Movie> movieCombo = new ComboBox<>(FXCollections.observableArrayList(activeMovies));
            movieCombo.setValue(activeMovies.get(0));

            ComboBox<Screen> screenCombo = new ComboBox<>(FXCollections.observableArrayList(allScreens));
            screenCombo.setValue(allScreens.get(0));

            DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
            Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 18);
            Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0, 15);

            HBox timeBox = new HBox(6, hourSpinner, new Label("hr"), minuteSpinner, new Label("min"));
            timeBox.setAlignment(Pos.CENTER_LEFT);

            TextField regPriceField = new TextField("250.00");
            TextField premPriceField = new TextField("350.00");

            grid.add(new Label("Select Movie:"), 0, 0); grid.add(movieCombo, 1, 0);
            grid.add(new Label("Select Auditorium:"), 0, 1); grid.add(screenCombo, 1, 1);
            grid.add(new Label("Date:"), 0, 2); grid.add(datePicker, 1, 2);
            grid.add(new Label("Time:"), 0, 3); grid.add(timeBox, 1, 3);
            grid.add(new Label("Regular Price (₹):"), 0, 4); grid.add(regPriceField, 1, 4);
            grid.add(new Label("Premium Price (₹):"), 0, 5); grid.add(premPriceField, 1, 5);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(btn -> {
                if (btn == scheduleBtn) {
                    try {
                        Movie m = movieCombo.getValue();
                        Screen s = screenCombo.getValue();
                        LocalDate date = datePicker.getValue();
                        LocalTime time = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
                        LocalDateTime start = LocalDateTime.of(date, time);

                        BigDecimal regPrice = new BigDecimal(regPriceField.getText().trim());
                        BigDecimal premPrice = new BigDecimal(premPriceField.getText().trim());

                        return new Showtime(m.getId(), s.getId(), start, regPrice, premPrice);
                    } catch (Exception ex) {
                        AlertHelper.showError("Invalid Input", "Pricing or Date Error", ex.getMessage());
                        return null;
                    }
                }
                return null;
            });

            dialog.showAndWait().ifPresent(st -> {
                try {
                    showtimeService.scheduleShowtime(st);
                    refreshTable();
                    AlertHelper.showInfo("Showtime Scheduled", "Success", "Screening scheduled successfully.");
                } catch (ValidationException | SQLException e) {
                    AlertHelper.showError("Scheduling Failed", "Conflict or Validation Error", e.getMessage());
                }
            });

        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to load options", e.getMessage());
        }
    }
}

package com.cineflow.ui.admin;

import com.cineflow.model.Movie;
import com.cineflow.service.MovieService;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Admin view for Movie Catalog CRUD operations.
 */
public class MovieManagementView {

    private final MovieService movieService;
    private TableView<Movie> tableView;
    private ObservableList<Movie> movieData;
    private TextField searchField;
    private ComboBox<String> genreFilter;

    public MovieManagementView() {
        this.movieService = new MovieService();
    }

    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        // Header and Toolbar
        HBox topBar = createTopBar();
        root.getChildren().add(topBar);

        // TableView
        tableView = createTableView();
        VBox.setVgrow(tableView, Priority.ALWAYS);
        root.getChildren().add(tableView);

        // Load initial data
        refreshTable();

        return root;
    }

    private HBox createTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Movie Catalog Management");
        title.getStyleClass().add("title-medium");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search by title...");
        searchField.setPrefWidth(200);

        // Property listener: Search-as-you-type filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterMovies());

        genreFilter = new ComboBox<>(FXCollections.observableArrayList(
                "ALL", "Action", "Sci-Fi", "Drama", "Comedy", "Thriller", "Animation"
        ));
        genreFilter.setValue("ALL");

        // Selection listener: Genre ComboBox filter
        genreFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterMovies());

        Button addBtn = new Button("+ Add Movie");
        addBtn.getStyleClass().add("button-primary");

        // ActionEvent: Opens dialog to add a new movie
        addBtn.setOnAction(e -> openMovieDialog(null));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button-secondary");

        // ActionEvent: Edits the selected movie
        editBtn.setOnAction(e -> {
            Movie selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openMovieDialog(selected);
            } else {
                AlertHelper.showWarning("Selection Required", "No Movie Selected", "Please select a movie to edit.");
            }
        });

        Button toggleBtn = new Button("Toggle Active");
        toggleBtn.getStyleClass().add("button-secondary");

        // ActionEvent: Toggles movie activation status
        toggleBtn.setOnAction(e -> toggleSelectedMovieActive());

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");

        // ActionEvent: Deletes selected movie with confirmation
        deleteBtn.setOnAction(e -> deleteSelectedMovie());

        bar.getChildren().addAll(title, spacer, searchField, genreFilter, addBtn, editBtn, toggleBtn, deleteBtn);
        return bar;
    }

    private TableView<Movie> createTableView() {
        TableView<Movie> table = new TableView<>();
        movieData = FXCollections.observableArrayList();
        table.setItems(movieData);

        TableColumn<Movie, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Movie, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(180);

        TableColumn<Movie, String> genreCol = new TableColumn<>("Genre");
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        genreCol.setPrefWidth(100);

        TableColumn<Movie, String> langCol = new TableColumn<>("Language");
        langCol.setCellValueFactory(new PropertyValueFactory<>("language"));
        langCol.setPrefWidth(90);

        TableColumn<Movie, Integer> durCol = new TableColumn<>("Duration");
        durCol.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        durCol.setPrefWidth(80);

        TableColumn<Movie, Double> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCol.setPrefWidth(70);

        TableColumn<Movie, String> dateCol = new TableColumn<>("Release Date");
        dateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReleaseDate() != null ? cellData.getValue().getReleaseDate().toString() : ""));
        dateCol.setPrefWidth(100);

        TableColumn<Movie, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActive() ? "ACTIVE" : "INACTIVE"));
        statusCol.setPrefWidth(90);

        table.getColumns().addAll(idCol, titleCol, genreCol, langCol, durCol, ratingCol, dateCol, statusCol);

        // MouseEvent: Double-click a movie TableView row to open edit dialog
        table.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Movie selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openMovieDialog(selected);
                }
            }
        });

        // KeyEvent: Delete removes the selected admin table row after confirmation
        table.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                deleteSelectedMovie();
            }
        });

        return table;
    }

    private void filterMovies() {
        String query = searchField.getText();
        String genre = genreFilter.getValue();
        try {
            List<Movie> results = movieService.searchMovies(query, genre, "ALL");
            movieData.setAll(results);
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Search Failed", e.getMessage());
        }
    }

    public void refreshTable() {
        try {
            List<Movie> list = movieService.getAllMovies();
            movieData.setAll(list);
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to Load Movies", e.getMessage());
        }
    }

    private void toggleSelectedMovieActive() {
        Movie selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Selection Required", "No Movie Selected", "Please select a movie to toggle.");
            return;
        }

        try {
            movieService.toggleActive(selected.getId(), !selected.isActive());
            refreshTable();
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to update movie status", e.getMessage());
        }
    }

    private void deleteSelectedMovie() {
        Movie selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Selection Required", "No Movie Selected", "Please select a movie to delete.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation(
                "Confirm Deletion",
                "Delete Movie?",
                "Are you sure you want to delete '" + selected.getTitle() + "'?\nIf bookings exist, it will be safely deactivated."
        );

        if (confirm) {
            try {
                movieService.deleteMovie(selected.getId());
                refreshTable();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not delete movie", e.getMessage());
            }
        }
    }

    private void openMovieDialog(Movie existingMovie) {
        Dialog<Movie> dialog = new Dialog<>();
        dialog.setTitle(existingMovie == null ? "Add New Movie" : "Edit Movie: " + existingMovie.getTitle());
        dialog.setHeaderText(existingMovie == null ? "Enter Movie Details" : "Update Movie Details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField(existingMovie != null ? existingMovie.getTitle() : "");
        TextField genreField = new TextField(existingMovie != null ? existingMovie.getGenre() : "");
        TextField langField = new TextField(existingMovie != null ? existingMovie.getLanguage() : "");
        TextField durField = new TextField(existingMovie != null ? String.valueOf(existingMovie.getDurationMinutes()) : "120");
        TextField ratingField = new TextField(existingMovie != null ? String.valueOf(existingMovie.getRating()) : "8.0");
        TextArea descArea = new TextArea(existingMovie != null ? existingMovie.getDescription() : "");
        descArea.setPrefRowCount(3);
        DatePicker releasePicker = new DatePicker(existingMovie != null ? existingMovie.getReleaseDate() : LocalDate.now());
        CheckBox activeCheck = new CheckBox("Active in Catalog");
        activeCheck.setSelected(existingMovie == null || existingMovie.isActive());

        grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Genre:"), 0, 1); grid.add(genreField, 1, 1);
        grid.add(new Label("Language:"), 0, 2); grid.add(langField, 1, 2);
        grid.add(new Label("Duration (mins):"), 0, 3); grid.add(durField, 1, 3);
        grid.add(new Label("Rating (0.0-10.0):"), 0, 4); grid.add(ratingField, 1, 4);
        grid.add(new Label("Release Date:"), 0, 5); grid.add(releasePicker, 1, 5);
        grid.add(new Label("Description:"), 0, 6); grid.add(descArea, 1, 6);
        grid.add(activeCheck, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int duration = Integer.parseInt(durField.getText().trim());
                    double rating = Double.parseDouble(ratingField.getText().trim());

                    Movie m = existingMovie != null ? existingMovie : new Movie();
                    m.setTitle(titleField.getText());
                    m.setGenre(genreField.getText());
                    m.setLanguage(langField.getText());
                    m.setDurationMinutes(duration);
                    m.setRating(rating);
                    m.setDescription(descArea.getText());
                    m.setReleaseDate(releasePicker.getValue());
                    m.setActive(activeCheck.isSelected());
                    return m;
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Invalid Number", "Input Error", "Duration must be an integer and Rating must be a number.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(movie -> {
            try {
                movieService.saveMovie(movie);
                refreshTable();
            } catch (ValidationException | SQLException e) {
                AlertHelper.showError("Save Failed", "Could not save movie", e.getMessage());
            }
        });
    }
}

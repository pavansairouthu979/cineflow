package com.cineflow.ui.customer;

import com.cineflow.model.Movie;
import com.cineflow.service.MovieService;
import com.cineflow.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Customer Movie Catalog View featuring search-as-you-type and genre/language filtering.
 */
public class MovieCatalogView {

    private final MovieService movieService;
    private final Consumer<Movie> onMovieSelected;

    private FlowPane movieGrid;
    private TextField searchField;
    private ComboBox<String> genreFilter;
    private ComboBox<String> langFilter;

    public MovieCatalogView(Consumer<Movie> onMovieSelected) {
        this.movieService = new MovieService();
        this.onMovieSelected = onMovieSelected;
    }

    public Parent getView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(20));

        // Filter / Search Toolbar
        VBox filterCard = createFilterBar();
        root.getChildren().add(filterCard);

        // Movie Cards FlowPane
        movieGrid = new FlowPane(20, 20);
        movieGrid.setPadding(new Insets(10));
        movieGrid.setAlignment(Pos.TOP_LEFT);

        ScrollPane scroll = new ScrollPane(movieGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);

        // Load initial movies
        applySearch();

        return root;
    }

    private VBox createFilterBar() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16));

        Label title = new Label("Explore Featured Movies");
        title.getStyleClass().add("title-medium");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search movies by title...");
        searchField.setPrefWidth(240);

        // Property listener: Search-as-you-type instantly filters movie list
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applySearch());

        genreFilter = new ComboBox<>(FXCollections.observableArrayList(
                "ALL", "Action", "Sci-Fi", "Drama", "Animation", "Comedy", "Thriller"
        ));
        genreFilter.setValue("ALL");
        // Selection listener: Genre ComboBox filter
        genreFilter.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> applySearch());

        langFilter = new ComboBox<>(FXCollections.observableArrayList(
                "ALL", "English", "Telugu", "Kannada", "Hindi", "Japanese"
        ));
        langFilter.setValue("ALL");
        // Selection listener: Language ComboBox filter
        langFilter.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> applySearch());

        Button resetBtn = new Button("Clear Filters");
        resetBtn.getStyleClass().add("button-secondary");
        // ActionEvent: Clears search and filters
        resetBtn.setOnAction(e -> {
            searchField.clear();
            genreFilter.setValue("ALL");
            langFilter.setValue("ALL");
            applySearch();
        });

        row.getChildren().addAll(
                new Label("Search:"), searchField,
                new Label("Genre:"), genreFilter,
                new Label("Language:"), langFilter,
                resetBtn
        );

        card.getChildren().addAll(title, row);
        return card;
    }

    public void applySearch() {
        movieGrid.getChildren().clear();
        String query = searchField.getText();
        String genre = genreFilter.getValue();
        String lang = langFilter.getValue();

        try {
            List<Movie> movies = movieService.searchMovies(query, genre, lang);
            if (movies.isEmpty()) {
                Label emptyLbl = new Label("No movies found matching your search criteria.");
                emptyLbl.getStyleClass().add("text-muted");
                movieGrid.getChildren().add(emptyLbl);
                return;
            }

            for (Movie movie : movies) {
                movieGrid.getChildren().add(createMovieCard(movie));
            }
        } catch (SQLException e) {
            AlertHelper.showError("Catalog Error", "Failed to load movies", e.getMessage());
        }
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(280);
        card.setMinHeight(240);

        Label titleLbl = new Label(movie.getTitle());
        titleLbl.getStyleClass().add("title-small");
        titleLbl.setWrapText(true);

        HBox badgeRow = new HBox(8);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        Label genreBadge = new Label(movie.getGenre());
        genreBadge.getStyleClass().add("badge-info");

        Label langBadge = new Label(movie.getLanguage());
        langBadge.getStyleClass().add("badge-warning");

        Label ratingBadge = new Label("★ " + movie.getRating());
        ratingBadge.getStyleClass().add("badge-success");

        badgeRow.getChildren().addAll(genreBadge, langBadge, ratingBadge);

        Label durLbl = new Label("⏱ Duration: " + movie.getDurationMinutes() + " mins");
        durLbl.getStyleClass().add("text-muted");

        Label descLbl = new Label(movie.getDescription());
        descLbl.getStyleClass().add("text-body");
        descLbl.setWrapText(true);
        descLbl.setMaxHeight(60);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button showtimesBtn = new Button("View Showtimes →");
        showtimesBtn.getStyleClass().add("button-primary");
        showtimesBtn.setMaxWidth(Double.MAX_VALUE);

        // ActionEvent: Handles view showtimes button click
        showtimesBtn.setOnAction(e -> {
            if (onMovieSelected != null) {
                onMovieSelected.accept(movie);
            }
        });

        card.getChildren().addAll(titleLbl, badgeRow, durLbl, descLbl, spacer, showtimesBtn);
        return card;
    }
}

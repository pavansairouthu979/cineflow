package com.cineflow.ui.customer;

import com.cineflow.model.Movie;
import com.cineflow.service.MovieService;
import com.cineflow.util.AlertHelper;
import com.cineflow.util.PosterHelper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Customer Movie Catalog View featuring Apple Liquid Glass styling,
 * high-resolution movie posters, and live search-as-you-type filtering.
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
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        // Filter / Search Toolbar (Apple Translucent Glass Card)
        VBox filterCard = createFilterBar();
        root.getChildren().add(filterCard);

        // Movie Cards FlowPane
        movieGrid = new FlowPane(24, 24);
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
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18, 22, 18, 22));

        Label title = new Label("Explore Featured Movies • Now Showing");
        title.getStyleClass().add("title-medium");

        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("🔍 Search movies by title...");
        searchField.setPrefWidth(260);

        // Property listener: Search-as-you-type instantly filters movie list
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applySearch());

        genreFilter = new ComboBox<>(FXCollections.observableArrayList(
                "ALL", "Action", "Sci-Fi", "Drama", "Animation", "Comedy", "Biography", "Thriller"
        ));
        genreFilter.setValue("ALL");
        genreFilter.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> applySearch());

        langFilter = new ComboBox<>(FXCollections.observableArrayList(
                "ALL", "English", "Telugu", "Kannada", "Hindi", "Japanese"
        ));
        langFilter.setValue("ALL");
        langFilter.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> applySearch());

        Button resetBtn = new Button("Clear Filters");
        resetBtn.getStyleClass().add("button-secondary");
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
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPrefWidth(270);
        card.setMaxWidth(270);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(14));

        // High-Resolution Movie Poster (Apple Rounded Glass)
        ImageView poster = PosterHelper.createPosterImageView(movie.getTitle(), 242, 330, 14);

        // Movie Meta Container
        VBox meta = new VBox(8);
        meta.setAlignment(Pos.TOP_LEFT);
        meta.setPadding(new Insets(4, 0, 0, 0));

        Label titleLbl = new Label(movie.getTitle());
        titleLbl.getStyleClass().add("title-small");
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        titleLbl.setWrapText(true);

        HBox badgeRow = new HBox(6);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        Label genreBadge = new Label(movie.getGenre());
        genreBadge.getStyleClass().add("badge-info");
        genreBadge.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");

        Label langBadge = new Label(movie.getLanguage());
        langBadge.getStyleClass().add("badge-warning");
        langBadge.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");

        Label ratingBadge = new Label("★ " + movie.getRating());
        ratingBadge.getStyleClass().add("badge-success");
        ratingBadge.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");

        badgeRow.getChildren().addAll(genreBadge, langBadge, ratingBadge);

        Label durLbl = new Label("⏱ " + movie.getDurationMinutes() + " mins • Released: " + movie.getReleaseDate());
        durLbl.getStyleClass().add("text-muted");

        Label descLbl = new Label(movie.getDescription());
        descLbl.getStyleClass().add("text-body");
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #a1a1aa;");
        descLbl.setWrapText(true);
        descLbl.setMaxHeight(40);

        Button showtimesBtn = new Button("Book Tickets →");
        showtimesBtn.getStyleClass().add("button-primary");
        showtimesBtn.setMaxWidth(Double.MAX_VALUE);
        showtimesBtn.setOnAction(e -> {
            if (onMovieSelected != null) {
                onMovieSelected.accept(movie);
            }
        });

        meta.getChildren().addAll(titleLbl, badgeRow, durLbl, descLbl, showtimesBtn);
        card.getChildren().addAll(poster, meta);

        // Apple smooth lift hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-translate-y: -4px; -fx-border-color: rgba(10, 132, 255, 0.6); -fx-effect: dropshadow(gaussian, rgba(10, 132, 255, 0.35), 20, 0, 0, 8);"));
        card.setOnMouseExited(e -> card.setStyle(""));

        return card;
    }
}

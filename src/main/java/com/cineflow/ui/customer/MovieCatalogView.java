package com.cineflow.ui.customer;

import com.cineflow.model.Movie;
import com.cineflow.service.MovieService;
import com.cineflow.util.AlertHelper;
import com.cineflow.util.PosterHelper;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * CineFlow — Premium Movie Discovery View.
 * Cinematic glassmorphism cards, animated hover 3D lift effects,
 * search-as-you-type with genre and language filters.
 */
public class MovieCatalogView {

    private final MovieService movieService;
    private final Consumer<Movie> onMovieSelected;

    private FlowPane movieGrid;
    private TextField searchField;
    private ComboBox<String> genreFilter;
    private ComboBox<String> langFilter;
    private Label countLabel;

    public MovieCatalogView(Consumer<Movie> onMovieSelected) {
        this.movieService = new MovieService();
        this.onMovieSelected = onMovieSelected;
    }

    public Parent getView() {
        VBox root = new VBox(0);
        root.getStyleClass().add("app-container");

        // Hero header bar
        VBox heroBar = createHeroBar();
        root.getChildren().add(heroBar);

        // Filter toolbar
        HBox filterBar = createFilterBar();
        root.getChildren().add(filterBar);

        // Movie grid
        movieGrid = new FlowPane(20, 20);
        movieGrid.setPadding(new Insets(22, 28, 28, 28));
        movieGrid.setAlignment(Pos.TOP_LEFT);

        ScrollPane scroll = new ScrollPane(movieGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);

        applySearch();
        return root;
    }

    /* ---------------------------------------------------------------- */
    /* HERO BAR                                                           */
    /* ---------------------------------------------------------------- */
    private VBox createHeroBar() {
        VBox hero = new VBox(6);
        hero.setPadding(new Insets(28, 28, 18, 28));
        hero.setStyle(
            "-fx-background-color: linear-gradient(to right, rgba(139,92,246,0.12), rgba(0,194,255,0.06), transparent);" +
            "-fx-border-color: rgba(139,92,246,0.12);" +
            "-fx-border-width: 0 0 1px 0;"
        );

        HBox titleRow = new HBox(14);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("Now Showing");
        sectionTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #F8FAFC;");

        Label dotSep = new Label("✦");
        dotSep.setStyle("-fx-font-size: 14px; -fx-text-fill: #8B5CF6;");

        Label subtitleLbl = new Label("Discover & Book Your Perfect Movie Experience");
        subtitleLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #A1A1AA;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        countLabel = new Label("");
        countLabel.getStyleClass().add("badge-purple");
        countLabel.setStyle("-fx-font-size: 12px;");

        titleRow.getChildren().addAll(sectionTitle, dotSep, subtitleLbl, spacer, countLabel);
        hero.getChildren().add(titleRow);
        return hero;
    }

    /* ---------------------------------------------------------------- */
    /* FILTER BAR                                                         */
    /* ---------------------------------------------------------------- */
    private HBox createFilterBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 28, 14, 28));
        bar.setStyle(
            "-fx-background-color: rgba(21,19,35,0.70);" +
            "-fx-border-color: rgba(139,92,246,0.12);" +
            "-fx-border-width: 0 0 1px 0;"
        );

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 16px;");

        searchField = new TextField();
        searchField.setPromptText("Search movies by title...");
        searchField.setPrefWidth(240);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applySearch());

        Region sep1 = buildVSep();

        Label genreIcon = new Label("🎭");
        genreIcon.setStyle("-fx-font-size: 14px;");
        genreFilter = new ComboBox<>(FXCollections.observableArrayList(
                "ALL", "Action", "Sci-Fi", "Drama", "Animation", "Comedy", "Biography", "Thriller"
        ));
        genreFilter.setValue("ALL");
        genreFilter.setPrefWidth(130);
        genreFilter.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> applySearch());

        Region sep2 = buildVSep();

        Label langIcon = new Label("🌐");
        langIcon.setStyle("-fx-font-size: 14px;");
        langFilter = new ComboBox<>(FXCollections.observableArrayList(
                "ALL", "English", "Telugu", "Kannada", "Hindi", "Japanese"
        ));
        langFilter.setValue("ALL");
        langFilter.setPrefWidth(120);
        langFilter.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> applySearch());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button resetBtn = new Button("✕  Clear");
        resetBtn.getStyleClass().add("button-secondary");
        resetBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 14px;");
        resetBtn.setOnAction(e -> {
            searchField.clear();
            genreFilter.setValue("ALL");
            langFilter.setValue("ALL");
        });

        bar.getChildren().addAll(
                searchIcon, searchField,
                sep1, genreIcon, genreFilter,
                sep2, langIcon, langFilter,
                spacer, resetBtn
        );
        return bar;
    }

    private Region buildVSep() {
        Region sep = new Region();
        sep.setStyle("-fx-border-color: rgba(139,92,246,0.20); -fx-border-width: 0 1px 0 0;");
        sep.setMinHeight(24);
        sep.setMinWidth(1);
        return sep;
    }

    /* ---------------------------------------------------------------- */
    /* SEARCH / FILTER LOGIC                                              */
    /* ---------------------------------------------------------------- */
    public void applySearch() {
        movieGrid.getChildren().clear();
        String query = searchField != null ? searchField.getText() : "";
        String genre = genreFilter != null ? genreFilter.getValue() : "ALL";
        String lang  = langFilter  != null ? langFilter.getValue()  : "ALL";

        try {
            List<Movie> movies = movieService.searchMovies(query, genre, lang);

            if (countLabel != null) {
                countLabel.setText(movies.size() + " films");
            }

            if (movies.isEmpty()) {
                VBox empty = createEmptyState();
                movieGrid.getChildren().add(empty);
                return;
            }

            for (int i = 0; i < movies.size(); i++) {
                VBox card = createMovieCard(movies.get(i));
                card.setOpacity(0);
                movieGrid.getChildren().add(card);

                // Staggered entrance animation
                int delay = i * 45;
                FadeTransition ft = new FadeTransition(Duration.millis(380), card);
                ft.setToValue(1.0);
                ft.setDelay(Duration.millis(delay));
                ft.play();

                TranslateTransition tt = new TranslateTransition(Duration.millis(380), card);
                tt.setFromY(14);
                tt.setToY(0);
                tt.setDelay(Duration.millis(delay));
                tt.setInterpolator(Interpolator.EASE_OUT);
                tt.play();
            }
        } catch (SQLException e) {
            AlertHelper.showError("Catalog Error", "Failed to load movies", e.getMessage());
        }
    }

    private VBox createEmptyState() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));

        Label icon = new Label("🎬");
        icon.setStyle("-fx-font-size: 48px;");

        Label msg = new Label("No movies match your search criteria.");
        msg.getStyleClass().add("text-subtitle");
        msg.setStyle("-fx-font-size: 16px;");

        Label hint = new Label("Try clearing filters or searching for a different title.");
        hint.getStyleClass().add("text-muted");

        box.getChildren().addAll(icon, msg, hint);
        return box;
    }

    /* ---------------------------------------------------------------- */
    /* MOVIE CARD                                                         */
    /* ---------------------------------------------------------------- */
    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(0);
        card.setPrefWidth(235);
        card.setMaxWidth(235);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
            "-fx-background-color: rgba(21,19,35,0.88);" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(139,92,246,0.28);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.15), 16, 0, 0, 5);" +
            "-fx-cursor: hand;"
        );

        // Poster with gradient overlay
        StackPane posterPane = new StackPane();
        ImageView poster = PosterHelper.createPosterImageView(movie.getTitle(), 235, 310, 0);
        poster.setStyle("-fx-background-radius: 16px 16px 0 0;");

        // Clip poster top corners
        Rectangle posterClip = new Rectangle(235, 310);
        posterClip.setArcWidth(32);
        posterClip.setArcHeight(32);
        poster.setClip(posterClip);

        // Gradient overlay at bottom of poster
        Pane gradientOverlay = new Pane();
        gradientOverlay.setPrefSize(235, 80);
        gradientOverlay.setStyle(
            "-fx-background-color: linear-gradient(to bottom, transparent, rgba(9,9,15,0.92));"
        );
        gradientOverlay.setTranslateY(230);

        // Rating badge overlaid on poster (top right)
        Label ratingBadge = new Label("★  " + movie.getRating());
        ratingBadge.setStyle(
            "-fx-background-color: rgba(9,9,15,0.78);" +
            "-fx-text-fill: #FCD34D;" +
            "-fx-font-size: 11px; -fx-font-weight: 800;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 4px 8px;" +
            "-fx-border-color: rgba(245,158,11,0.35);" +
            "-fx-border-width: 1px; -fx-border-radius: 8px;"
        );
        StackPane.setAlignment(ratingBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(ratingBadge, new Insets(10, 10, 0, 0));

        posterPane.getChildren().addAll(poster, gradientOverlay, ratingBadge);
        posterPane.setMaxHeight(310);

        // Info panel below poster
        VBox info = new VBox(8);
        info.setPadding(new Insets(12, 14, 14, 14));
        info.setStyle("-fx-background-color: transparent;");

        // Title
        Label titleLbl = new Label(movie.getTitle());
        titleLbl.getStyleClass().add("title-small");
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #F8FAFC;");
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(207);

        // Genre + Language badges
        HBox badgeRow = new HBox(6);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        Label genreBadge = new Label(movie.getGenre());
        genreBadge.getStyleClass().add("badge-info");
        genreBadge.setStyle("-fx-font-size: 10px; -fx-padding: 2px 8px;");

        Label langBadge = new Label(movie.getLanguage());
        langBadge.getStyleClass().add("badge-warning");
        langBadge.setStyle("-fx-font-size: 10px; -fx-padding: 2px 8px;");

        badgeRow.getChildren().addAll(genreBadge, langBadge);

        // Duration
        Label durLbl = new Label("⏱  " + movie.getDurationMinutes() + " min");
        durLbl.getStyleClass().add("text-muted");
        durLbl.setStyle("-fx-font-size: 11px;");

        // Book button
        Button bookBtn = new Button("Book Tickets  →");
        bookBtn.getStyleClass().add("button-primary");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setStyle("-fx-font-size: 13px; -fx-padding: 9px 14px;");
        bookBtn.setOnAction(e -> {
            if (onMovieSelected != null) onMovieSelected.accept(movie);
        });

        info.getChildren().addAll(titleLbl, badgeRow, durLbl, bookBtn);
        card.getChildren().addAll(posterPane, info);

        // 3D lift hover effect
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();

            card.setStyle(
                "-fx-background-color: rgba(21,19,35,0.95);" +
                "-fx-background-radius: 16px;" +
                "-fx-border-color: rgba(139,92,246,0.65);" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 16px;" +
                "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.45), 28, 0, 0, 10);" +
                "-fx-cursor: hand;" +
                "-fx-translate-y: -4px;"
            );
        });

        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();

            card.setStyle(
                "-fx-background-color: rgba(21,19,35,0.88);" +
                "-fx-background-radius: 16px;" +
                "-fx-border-color: rgba(139,92,246,0.28);" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 16px;" +
                "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.15), 16, 0, 0, 5);" +
                "-fx-cursor: hand;" +
                "-fx-translate-y: 0px;"
            );
        });

        return card;
    }
}

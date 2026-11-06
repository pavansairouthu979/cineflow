package com.cineflow.ui.customer;

import com.cineflow.model.Movie;
import com.cineflow.model.Showtime;
import com.cineflow.service.ShowtimeService;
import com.cineflow.util.AlertHelper;
import com.cineflow.util.PosterHelper;
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
 * Customer view displaying scheduled showtimes for a chosen movie,
 * featuring an Apple Liquid Glass movie showcase banner with poster art.
 */
public class ShowtimeSelectionView {

    private final Movie movie;
    private final ShowtimeService showtimeService;
    private final Consumer<Showtime> onShowtimeSelected;
    private final Runnable onBack;

    public ShowtimeSelectionView(Movie movie, Consumer<Showtime> onShowtimeSelected, Runnable onBack) {
        this.movie = movie;
        this.showtimeService = new ShowtimeService();
        this.onShowtimeSelected = onShowtimeSelected;
        this.onBack = onBack;
    }

    public Parent getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        // Header with Back button
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Back to Movies");
        backBtn.getStyleClass().add("button-secondary");
        backBtn.setOnAction(e -> onBack.run());

        Label title = new Label("Select Showtime • " + movie.getTitle());
        title.getStyleClass().add("title-medium");

        topBar.getChildren().addAll(backBtn, title);
        root.getChildren().add(topBar);

        // Movie Info Banner Card with Poster
        HBox movieBanner = createMovieBanner();
        root.getChildren().add(movieBanner);

        // Showtimes Cards Container
        Label sectionTitle = new Label("Available Upcoming Screenings:");
        sectionTitle.getStyleClass().add("title-small");
        root.getChildren().add(sectionTitle);

        FlowPane showtimesGrid = new FlowPane(18, 18);
        showtimesGrid.setPadding(new Insets(8));

        try {
            List<Showtime> showtimes = showtimeService.getShowtimesForMovie(movie.getId());
            if (showtimes.isEmpty()) {
                Label noShowsLbl = new Label("No upcoming screenings scheduled for this movie. Please check back later.");
                noShowsLbl.getStyleClass().add("text-muted");
                showtimesGrid.getChildren().add(noShowsLbl);
            } else {
                for (Showtime st : showtimes) {
                    showtimesGrid.getChildren().add(createShowtimeCard(st));
                }
            }
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to load showtimes", e.getMessage());
        }

        ScrollPane scroll = new ScrollPane(showtimesGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);

        return root;
    }

    private HBox createMovieBanner() {
        HBox card = new HBox(20);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18));

        // High-Resolution Movie Poster
        ImageView poster = PosterHelper.createPosterImageView(movie.getTitle(), 120, 165, 12);

        VBox details = new VBox(8);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label movieTitle = new Label(movie.getTitle());
        movieTitle.getStyleClass().add("title-large");

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label genreBadge = new Label(movie.getGenre() != null ? movie.getGenre() : "FEATURE");
        genreBadge.getStyleClass().add("badge-info");

        Label langBadge = new Label(movie.getLanguage() != null ? movie.getLanguage() : "ORIGINAL");
        langBadge.getStyleClass().add("badge-warning");

        Label ratingBadge = new Label("★ " + (movie.getRating() > 0 ? movie.getRating() : "8.5"));
        ratingBadge.getStyleClass().add("badge-success");

        Label durLbl = new Label("⏱ " + (movie.getDurationMinutes() > 0 ? movie.getDurationMinutes() : "148") + " mins");
        durLbl.getStyleClass().add("text-muted");

        metaRow.getChildren().addAll(genreBadge, langBadge, ratingBadge, durLbl);

        Label desc = new Label(movie.getDescription() != null && !movie.getDescription().isBlank()
                ? movie.getDescription()
                : "Experience this cinematic masterpiece in high-definition digital sound and projection at CineFlow.");
        desc.getStyleClass().add("text-body");
        desc.setWrapText(true);

        details.getChildren().addAll(movieTitle, metaRow, desc);
        card.getChildren().addAll(poster, details);
        return card;
    }

    private VBox createShowtimeCard(Showtime st) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(270);
        card.setPadding(new Insets(16));

        Label screenLbl = new Label("🏛 " + st.getScreenName());
        screenLbl.getStyleClass().add("title-small");
        screenLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label timeLbl = new Label("🕒 " + st.getFormattedStartTime());
        timeLbl.getStyleClass().add("text-body");
        timeLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0A84FF; -fx-font-size: 14px;");

        VBox priceBox = new VBox(4);
        priceBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.25); -fx-padding: 10px; -fx-background-radius: 10px; -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-radius: 10px;");

        Label regPrice = new Label("Regular Tier: ₹ " + st.getPriceRegular().toPlainString());
        regPrice.getStyleClass().add("text-muted");

        Label premPrice = new Label("Premium Tier: ₹ " + st.getPricePremium().toPlainString());
        premPrice.getStyleClass().add("text-muted");
        premPrice.setStyle("-fx-text-fill: #ffd60a; -fx-font-weight: bold;");

        priceBox.getChildren().addAll(regPrice, premPrice);

        Button selectBtn = new Button("Select Seats →");
        selectBtn.getStyleClass().add("button-primary");
        selectBtn.setMaxWidth(Double.MAX_VALUE);

        // ActionEvent: Navigates to the interactive seat map
        selectBtn.setOnAction(e -> {
            if (onShowtimeSelected != null) {
                onShowtimeSelected.accept(st);
            }
        });

        card.getChildren().addAll(screenLbl, timeLbl, priceBox, selectBtn);

        card.setOnMouseEntered(e -> card.setStyle("-fx-translate-y: -3px; -fx-border-color: rgba(10, 132, 255, 0.5);"));
        card.setOnMouseExited(e -> card.setStyle(""));

        return card;
    }
}

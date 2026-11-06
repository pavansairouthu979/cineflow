package com.cineflow.ui.customer;

import com.cineflow.model.Movie;
import com.cineflow.model.Showtime;
import com.cineflow.service.ShowtimeService;
import com.cineflow.util.AlertHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Customer view displaying scheduled showtimes for a chosen movie.
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
        // ActionEvent: Returns to the movie catalog
        backBtn.setOnAction(e -> onBack.run());

        Label title = new Label("Select Showtime for " + movie.getTitle());
        title.getStyleClass().add("title-medium");

        topBar.getChildren().addAll(backBtn, title);
        root.getChildren().add(topBar);

        // Movie Info Banner Card
        VBox movieBanner = createMovieBanner();
        root.getChildren().add(movieBanner);

        // Showtimes Cards Container
        Label sectionTitle = new Label("Available Upcoming Showtimes:");
        sectionTitle.getStyleClass().add("title-small");
        root.getChildren().add(sectionTitle);

        FlowPane showtimesGrid = new FlowPane(16, 16);
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

    private VBox createMovieBanner() {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16));

        Label movieTitle = new Label(movie.getTitle());
        movieTitle.getStyleClass().add("title-large");

        HBox metaRow = new HBox(12);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label genreLbl = new Label("Genre: " + movie.getGenre());
        genreLbl.getStyleClass().add("text-subtitle");
        Label langLbl = new Label("Language: " + movie.getLanguage());
        langLbl.getStyleClass().add("text-subtitle");
        Label durLbl = new Label("Duration: " + movie.getDurationMinutes() + " mins");
        durLbl.getStyleClass().add("text-subtitle");
        Label ratingLbl = new Label("Rating: ★ " + movie.getRating());
        ratingLbl.getStyleClass().add("text-subtitle");
        metaRow.getChildren().addAll(genreLbl, langLbl, durLbl, ratingLbl);

        Label desc = new Label(movie.getDescription());
        desc.getStyleClass().add("text-body");
        desc.setWrapText(true);

        card.getChildren().addAll(movieTitle, metaRow, desc);
        return card;
    }

    private VBox createShowtimeCard(Showtime st) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(260);
        card.setPadding(new Insets(14));

        Label screenLbl = new Label("🏛 " + st.getScreenName());
        screenLbl.getStyleClass().add("title-small");

        Label timeLbl = new Label("🕒 " + st.getFormattedStartTime());
        timeLbl.getStyleClass().add("text-body");
        timeLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #818cf8;");

        VBox priceBox = new VBox(2);
        priceBox.setStyle("-fx-background-color: #0d1525; -fx-padding: 8px; -fx-background-radius: 6px;");
        Label regPrice = new Label("Regular: ₹ " + st.getPriceRegular().toPlainString());
        regPrice.getStyleClass().add("text-muted");
        Label premPrice = new Label("Premium: ₹ " + st.getPricePremium().toPlainString());
        premPrice.getStyleClass().add("text-muted");
        premPrice.setStyle("-fx-text-fill: #fbbf24;");
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
        return card;
    }
}

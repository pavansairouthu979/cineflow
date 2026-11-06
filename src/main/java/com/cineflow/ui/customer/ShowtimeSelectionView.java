package com.cineflow.ui.customer;

import com.cineflow.model.Movie;
import com.cineflow.model.Showtime;
import com.cineflow.service.ShowtimeService;
import com.cineflow.util.AlertHelper;
import com.cineflow.util.PosterHelper;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * CineFlow — Premium Showtime Selection View.
 * Movie showcase banner with poster art, and elegant showtime cards.
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
        VBox root = new VBox(0);
        root.getStyleClass().add("app-container");

        // Breadcrumb nav bar
        HBox topBar = createTopBar();
        root.getChildren().add(topBar);

        // Movie showcase banner
        HBox movieBanner = createMovieBanner();
        root.getChildren().add(movieBanner);

        // Showtimes section
        VBox showtimesSection = createShowtimesSection();
        VBox.setVgrow(showtimesSection, Priority.ALWAYS);
        root.getChildren().add(showtimesSection);

        return root;
    }

    /* ---------------------------------------------------------------- */
    /* TOP BAR                                                            */
    /* ---------------------------------------------------------------- */
    private HBox createTopBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 28, 14, 28));
        bar.setStyle(
            "-fx-background-color: rgba(21,19,35,0.75);" +
            "-fx-border-color: rgba(139,92,246,0.12);" +
            "-fx-border-width: 0 0 1px 0;"
        );

        Button backBtn = new Button("←  All Movies");
        backBtn.getStyleClass().add("button-secondary");
        backBtn.setStyle("-fx-font-size: 13px; -fx-padding: 7px 16px;");
        backBtn.setOnAction(e -> onBack.run());

        Label sep = new Label("/");
        sep.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 14px;");

        Label breadcrumb = new Label(movie.getTitle());
        breadcrumb.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 14px;");

        Label sep2 = new Label("/");
        sep2.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 14px;");

        Label currentPage = new Label("Select Showtime");
        currentPage.setStyle("-fx-text-fill: #8B5CF6; -fx-font-size: 14px; -fx-font-weight: 700;");

        bar.getChildren().addAll(backBtn, sep, breadcrumb, sep2, currentPage);
        return bar;
    }

    /* ---------------------------------------------------------------- */
    /* MOVIE BANNER                                                       */
    /* ---------------------------------------------------------------- */
    private HBox createMovieBanner() {
        HBox card = new HBox(24);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(28, 36, 28, 36));
        card.setStyle(
            "-fx-background-color: linear-gradient(to right," +
            "  rgba(139,92,246,0.14) 0%," +
            "  rgba(0,194,255,0.06) 60%," +
            "  transparent 100%);" +
            "-fx-border-color: rgba(139,92,246,0.12);" +
            "-fx-border-width: 0 0 1px 0;"
        );

        // Poster with rounded corners
        ImageView poster = PosterHelper.createPosterImageView(movie.getTitle(), 130, 180, 14);

        // Details
        VBox details = new VBox(10);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label movieTitle = new Label(movie.getTitle());
        movieTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #F8FAFC;");
        movieTitle.setWrapText(true);

        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label genreBadge = new Label(movie.getGenre() != null ? movie.getGenre() : "FEATURE");
        genreBadge.getStyleClass().add("badge-info");

        Label langBadge = new Label(movie.getLanguage() != null ? movie.getLanguage() : "ORIGINAL");
        langBadge.getStyleClass().add("badge-warning");

        Label ratingBadge = new Label("★  " + (movie.getRating() > 0 ? movie.getRating() : "8.5"));
        ratingBadge.getStyleClass().add("badge-success");

        Label durLbl = new Label("⏱  " + (movie.getDurationMinutes() > 0 ? movie.getDurationMinutes() : "148") + " min");
        durLbl.getStyleClass().add("text-muted");

        metaRow.getChildren().addAll(genreBadge, langBadge, ratingBadge, durLbl);

        Label desc = new Label(movie.getDescription() != null && !movie.getDescription().isBlank()
                ? movie.getDescription()
                : "Experience this cinematic masterpiece at CineFlow — premium sound, premium seating, premium experience.");
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #A1A1AA; -fx-line-spacing: 2px;");
        desc.setWrapText(true);
        desc.setMaxWidth(600);

        details.getChildren().addAll(movieTitle, metaRow, desc);
        card.getChildren().addAll(poster, details);
        return card;
    }

    /* ---------------------------------------------------------------- */
    /* SHOWTIMES SECTION                                                  */
    /* ---------------------------------------------------------------- */
    private VBox createShowtimesSection() {
        VBox section = new VBox(0);
        section.setPadding(new Insets(0));

        // Section header
        HBox sectionHeader = new HBox(12);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        sectionHeader.setPadding(new Insets(22, 28, 14, 28));

        Label sectionTitle = new Label("Available Screenings");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #F8FAFC;");

        Label dotSep = new Label("✦");
        dotSep.setStyle("-fx-text-fill: #8B5CF6; -fx-font-size: 12px;");

        Label hint = new Label("Select a time slot to continue to seat selection");
        hint.getStyleClass().add("text-muted");

        sectionHeader.getChildren().addAll(sectionTitle, dotSep, hint);

        // Showtimes grid
        FlowPane showtimesGrid = new FlowPane(16, 16);
        showtimesGrid.setPadding(new Insets(4, 28, 28, 28));

        try {
            List<Showtime> showtimes = showtimeService.getShowtimesForMovie(movie.getId());
            if (showtimes.isEmpty()) {
                VBox emptyBox = new VBox(10);
                emptyBox.setAlignment(Pos.CENTER_LEFT);
                emptyBox.setPadding(new Insets(20));
                Label noShowsLbl = new Label("🎬  No upcoming screenings scheduled for this movie.");
                noShowsLbl.getStyleClass().add("text-subtitle");
                Label hintLbl = new Label("Please check back later or browse other movies.");
                hintLbl.getStyleClass().add("text-muted");
                emptyBox.getChildren().addAll(noShowsLbl, hintLbl);
                showtimesGrid.getChildren().add(emptyBox);
            } else {
                for (int i = 0; i < showtimes.size(); i++) {
                    VBox card = createShowtimeCard(showtimes.get(i));
                    card.setOpacity(0);
                    showtimesGrid.getChildren().add(card);

                    FadeTransition ft = new FadeTransition(Duration.millis(320), card);
                    ft.setToValue(1.0);
                    ft.setDelay(Duration.millis(i * 60L));
                    ft.play();
                }
            }
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to load showtimes", e.getMessage());
        }

        ScrollPane scroll = new ScrollPane(showtimesGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        section.getChildren().addAll(sectionHeader, scroll);
        return section;
    }

    /* ---------------------------------------------------------------- */
    /* SHOWTIME CARD                                                      */
    /* ---------------------------------------------------------------- */
    private VBox createShowtimeCard(Showtime st) {
        VBox card = new VBox(12);
        card.setPrefWidth(260);
        card.setPadding(new Insets(18));
        card.setStyle(
            "-fx-background-color: rgba(21,19,35,0.88);" +
            "-fx-background-radius: 14px;" +
            "-fx-border-color: rgba(139,92,246,0.28);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 14px;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.12), 14, 0, 0, 4);" +
            "-fx-cursor: hand;"
        );

        // Screen name
        HBox screenRow = new HBox(8);
        screenRow.setAlignment(Pos.CENTER_LEFT);
        Label screenIcon = new Label("🏛");
        screenIcon.setStyle("-fx-font-size: 14px;");
        Label screenLbl = new Label(st.getScreenName());
        screenLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #F8FAFC;");
        screenRow.getChildren().addAll(screenIcon, screenLbl);

        // Time
        HBox timeRow = new HBox(8);
        timeRow.setAlignment(Pos.CENTER_LEFT);
        Label clockIcon = new Label("🕒");
        clockIcon.setStyle("-fx-font-size: 13px;");
        Label timeLbl = new Label(st.getFormattedStartTime());
        timeLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #00C2FF;");
        timeRow.getChildren().addAll(clockIcon, timeLbl);

        // Price box
        VBox priceBox = new VBox(5);
        priceBox.setStyle(
            "-fx-background-color: rgba(139,92,246,0.06);" +
            "-fx-padding: 10px 12px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: rgba(139,92,246,0.16);" +
            "-fx-border-width: 1px; -fx-border-radius: 10px;"
        );

        HBox regRow = new HBox(8);
        regRow.setAlignment(Pos.CENTER_LEFT);
        Label regDot = new Label("●");
        regDot.setStyle("-fx-font-size: 8px; -fx-text-fill: #22C55E;");
        Label regLbl = new Label("Regular  —  ₹ " + st.getPriceRegular().toPlainString());
        regLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #A1A1AA;");
        regRow.getChildren().addAll(regDot, regLbl);

        HBox premRow = new HBox(8);
        premRow.setAlignment(Pos.CENTER_LEFT);
        Label premDot = new Label("●");
        premDot.setStyle("-fx-font-size: 8px; -fx-text-fill: #F59E0B;");
        Label premLbl = new Label("Premium  —  ₹ " + st.getPricePremium().toPlainString());
        premLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #FCD34D;");
        premRow.getChildren().addAll(premDot, premLbl);

        priceBox.getChildren().addAll(regRow, premRow);

        // Select button
        Button selectBtn = new Button("Select Seats  →");
        selectBtn.getStyleClass().add("button-primary");
        selectBtn.setMaxWidth(Double.MAX_VALUE);
        selectBtn.setStyle("-fx-font-size: 13px; -fx-padding: 9px;");
        selectBtn.setOnAction(e -> {
            if (onShowtimeSelected != null) onShowtimeSelected.accept(st);
        });

        card.getChildren().addAll(screenRow, timeRow, priceBox, selectBtn);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: rgba(21,19,35,0.95);" +
            "-fx-background-radius: 14px;" +
            "-fx-border-color: rgba(139,92,246,0.60);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 14px;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.38), 22, 0, 0, 8);" +
            "-fx-cursor: hand;" +
            "-fx-translate-y: -3px;"
        ));

        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: rgba(21,19,35,0.88);" +
            "-fx-background-radius: 14px;" +
            "-fx-border-color: rgba(139,92,246,0.28);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 14px;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.12), 14, 0, 0, 4);" +
            "-fx-cursor: hand;" +
            "-fx-translate-y: 0px;"
        ));

        return card;
    }
}

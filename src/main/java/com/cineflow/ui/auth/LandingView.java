package com.cineflow.ui.auth;

import com.cineflow.ui.common.NavigationManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Arrays;

/**
 * CineFlow — Premium SaaS Landing Page.
 *
 * Features:
 *  - Real cinematic background image (Unsplash, CC0) loaded async
 *  - Icon shapes drawn with JavaFX primitives (no missing resource)
 *  - Sticky glassmorphism navbar with Login / Sign Up
 *  - Hero, Stats bar, Features, How It Works, Genres, CTA, Footer
 *
 * Image credits (Unsplash, free to use):
 *  landing_bg.jpg  — photo-1489599849927  by Jeremy Yap
 *  hero_cinema.jpg — photo-1517604931442  by Myke Simon
 *  landing_bg2.jpg — photo-1574267432553  by Krists Luhaers
 */
public class LandingView {

    // Cached images loaded once
    private Image heroBgImage;
    private Image heroCinemaImage;

    public Parent getView() {
        // Start loading images on background thread immediately
        loadImagesAsync();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #09090F;");

        root.setTop(createNavbar());

        VBox body = new VBox(0);
        body.setStyle("-fx-background-color: #09090F;");

        body.getChildren().addAll(
                createHeroSection(),
                createStatsBar(),
                createFeaturesSection(),
                createHowItWorksSection(),
                createGenresSection(),
                createCtaSection(),
                createFooter()
        );

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background-color: #09090F;" +
            "-fx-background: #09090F;" +
            "-fx-border-color: transparent;"
        );
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.setCenter(scroll);

        return root;
    }

    /* ---------------------------------------------------------------- */
    /* ASYNC IMAGE LOADING — never blocks the FX thread                  */
    /* ---------------------------------------------------------------- */
    private void loadImagesAsync() {
        Thread t = new Thread(() -> {
            heroBgImage    = loadResource("/images/landing_bg.jpg");
            heroCinemaImage = loadResource("/images/hero_cinema.jpg");
        }, "cineflow-image-loader");
        t.setDaemon(true);
        t.start();
    }

    private Image loadResource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                // background=true loads on background thread inside JavaFX Image
                return new Image(is);
            }
        } catch (Exception e) {
            System.err.println("Image load failed: " + path);
        }
        return null;
    }

    /* ================================================================ */
    /* NAVBAR                                                             */
    /* ================================================================ */
    private VBox createNavbar() {
        VBox wrapper = new VBox();
        wrapper.setStyle(
            "-fx-background-color: rgba(9,9,15,0.93);" +
            "-fx-border-color: rgba(139,92,246,0.18);" +
            "-fx-border-width: 0 0 1px 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.10), 14, 0, 0, 4);"
        );

        HBox row = new HBox(18);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 36, 13, 36));

        // Traffic dots
        HBox dots = new HBox(6);
        dots.getChildren().addAll(
                dot("#FF5F56"), dot("#FFBD2E"), dot("#27C93F"));

        // Logo with film-frame icon
        HBox logoArea = new HBox(9);
        logoArea.setAlignment(Pos.CENTER_LEFT);
        logoArea.getChildren().addAll(filmFrameIcon(18), buildLogoLabel());

        Label tagPill = new Label("✦  MULTIPLEX");
        tagPill.setStyle(
            "-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #8B5CF6;" +
            "-fx-background-color: rgba(139,92,246,0.12);" +
            "-fx-border-color: rgba(139,92,246,0.30);" +
            "-fx-border-width: 1px; -fx-border-radius: 20px; -fx-background-radius: 20px;" +
            "-fx-padding: 2px 10px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Nav links
        Label featLnk = navLink("Features");
        Label howLnk  = navLink("How It Works");
        Label genLnk  = navLink("Genres");

        // Buttons
        Button loginBtn  = navOutlineBtn("Sign In");
        loginBtn.setOnAction(e -> NavigationManager.showLoginView());

        Button signupBtn = navPrimaryBtn("Get Started  →");
        signupBtn.setOnAction(e -> NavigationManager.showRegistrationView());

        row.getChildren().addAll(
                dots, logoArea, tagPill, spacer,
                featLnk, howLnk, genLnk,
                loginBtn, signupBtn
        );
        wrapper.getChildren().add(row);
        return wrapper;
    }

    /* ================================================================ */
    /* HERO SECTION                                                       */
    /* ================================================================ */
    private StackPane createHeroSection() {
        StackPane hero = new StackPane();
        hero.setMinHeight(600);
        hero.setPrefHeight(640);

        // ── Layer 1: Real background image ───────────────────────────
        StackPane imgLayer = new StackPane();
        imgLayer.setStyle("-fx-background-color: #09090F;");
        imgLayer.setPrefSize(1100, 640);
        imgLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // ImageView — loaded async, will appear once ready
        ImageView bgView = new ImageView();
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);
        bgView.fitWidthProperty().bind(imgLayer.widthProperty());
        bgView.setFitHeight(640);
        bgView.setOpacity(0);

        // Bind image when loaded
        Thread imgBind = new Thread(() -> {
            try { Thread.sleep(80); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                if (heroBgImage != null) {
                    bgView.setImage(heroBgImage);
                    FadeTransition fImg = new FadeTransition(Duration.millis(900), bgView);
                    fImg.setToValue(0.32);   // keep it dark; text stays readable
                    fImg.play();
                }
            });
        }, "img-bind");
        imgBind.setDaemon(true);
        imgBind.start();

        imgLayer.getChildren().add(bgView);
        hero.getChildren().add(imgLayer);

        // ── Layer 2: Purple/blue gradient overlay + grid ─────────────
        Pane glowLayer = buildHeroGlowLayer();
        hero.getChildren().add(glowLayer);

        // ── Layer 3: Content ─────────────────────────────────────────
        VBox content = new VBox(26);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(90, 80, 90, 80));
        content.setMaxWidth(980);
        StackPane.setAlignment(content, Pos.CENTER);

        // Eyebrow
        HBox eyebrow = buildEyebrow("🎬", "NOW SHOWING — LATEST BLOCKBUSTERS");

        // Headline
        VBox headlineBlock = new VBox(2);
        headlineBlock.setAlignment(Pos.CENTER);

        Label h1a = new Label("Your Next Movie Experience");
        h1a.setStyle(
            "-fx-font-size: 52px; -fx-font-weight: 900; -fx-text-fill: #F8FAFC;" +
            "-fx-text-alignment: center;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.60), 18, 0, 0, 4);"
        );
        h1a.setAlignment(Pos.CENTER);

        Label h1b = new Label("Starts Right Here.");
        h1b.setStyle(
            "-fx-font-size: 52px; -fx-font-weight: 900; -fx-text-alignment: center;" +
            "-fx-text-fill: linear-gradient(to right, #8B5CF6, #00C2FF);" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.45), 24, 0, 0, 6);"
        );
        h1b.setAlignment(Pos.CENTER);

        headlineBlock.getChildren().addAll(h1a, h1b);

        // Subtitle
        Label subtitle = new Label(
            "Discover movies. Choose your perfect seats. Book instantly with\n" +
            "UPI, Card or Cash — all powered by real-time availability."
        );
        subtitle.setStyle(
            "-fx-font-size: 17px; -fx-text-fill: #CBD5E1;" +
            "-fx-line-spacing: 4px; -fx-text-alignment: center;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.50), 8, 0, 0, 2);"
        );
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setMaxWidth(620);

        // CTA row
        HBox ctaRow = new HBox(16);
        ctaRow.setAlignment(Pos.CENTER);

        Button explorBtn = heroPrimaryBtn("🎬  Explore Movies");
        explorBtn.setOnAction(e -> NavigationManager.showLoginView());

        Button bookBtn = heroSecondaryBtn("Book Your Seats  →");
        bookBtn.setOnAction(e -> NavigationManager.showRegistrationView());

        ctaRow.getChildren().addAll(explorBtn, bookBtn);

        // Trust chips row
        HBox trustRow = new HBox(14);
        trustRow.setAlignment(Pos.CENTER);
        trustRow.getChildren().addAll(
                trustChip("⚡", "Instant Booking"),
                trustChip("🔒", "Secure Payment"),
                trustChip("🎫", "Digital Ticket"),
                trustChip("📡", "Live Seat Map")
        );

        content.getChildren().addAll(eyebrow, headlineBlock, subtitle, ctaRow, trustRow);

        // Staggered entrance animations
        staggerIn(eyebrow,   0,  -20);
        staggerIn(h1a,     180,  -26);
        staggerIn(h1b,     260,  -26);
        staggerIn(subtitle,380,  -18);
        staggerIn(ctaRow,  520,  -14);
        staggerIn(trustRow,660,  -10);

        hero.getChildren().add(content);
        return hero;
    }

    private Pane buildHeroGlowLayer() {
        Pane p = new Pane();
        p.setStyle("-fx-background-color: transparent;");
        p.setPrefSize(1100, 640);

        // Purple bloom top-left
        Circle b1 = new Circle(-50, 180, 420);
        b1.setFill(new RadialGradient(0,0,0.5,0.5,0.5,true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#8B5CF6", 0.22)),
            new Stop(0.65, Color.web("#6D28D9", 0.06)),
            new Stop(1, Color.web("#09090F", 0.0))));

        // Blue bloom right
        Circle b2 = new Circle(1100, 400, 380);
        b2.setFill(new RadialGradient(0,0,0.5,0.5,0.5,true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#00C2FF", 0.18)),
            new Stop(0.65, Color.web("#0066FF", 0.05)),
            new Stop(1, Color.web("#09090F", 0.0))));

        // Bottom centre glow
        Circle b3 = new Circle(550, 700, 300);
        b3.setFill(new RadialGradient(0,0,0.5,0.5,0.5,true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#7C3AED", 0.14)),
            new Stop(1, Color.web("#09090F", 0.0))));

        // Perspective grid
        for (int x = 0; x <= 1100; x += 72) {
            Line vl = new Line(x, 0, x, 640);
            vl.setStroke(Color.web("#8B5CF6", 0.05));
            vl.setStrokeWidth(0.5);
            p.getChildren().add(vl);
        }
        for (int y = 0; y <= 640; y += 72) {
            Line hl = new Line(0, y, 1100, y);
            hl.setStroke(Color.web("#8B5CF6", 0.05));
            hl.setStrokeWidth(0.5);
            p.getChildren().add(hl);
        }

        // Floating particles
        for (int i = 0; i < 28; i++) {
            Circle pt = new Circle(Math.random() * 2.4 + 0.5);
            pt.setFill(Color.web(
                i % 3 == 0 ? "#8B5CF6" : i % 3 == 1 ? "#00C2FF" : "#FFFFFF",
                Math.random() * 0.30 + 0.05));
            pt.setLayoutX(Math.random() * 1100);
            pt.setLayoutY(Math.random() * 640);
            floatParticle(pt);
            p.getChildren().add(pt);
        }

        slowFloat(b1, 22, 14);
        slowFloat(b2, -18, 20);
        slowFloat(b3,  12, -14);

        p.getChildren().addAll(0, Arrays.asList(b1, b2, b3));
        return p;
    }

    /* ================================================================ */
    /* STATS BAR                                                          */
    /* ================================================================ */
    private HBox createStatsBar() {
        HBox bar = new HBox(0);
        bar.setStyle(
            "-fx-background-color: rgba(139,92,246,0.07);" +
            "-fx-border-color: rgba(139,92,246,0.18);" +
            "-fx-border-width: 1px 0 1px 0;"
        );
        bar.setPadding(new Insets(24, 80, 24, 80));
        bar.setAlignment(Pos.CENTER);

        bar.getChildren().addAll(
            statItem("🎬", "2,400+",  "Movies Screened",   "#8B5CF6"),
            statSep(),
            statItem("🎟", "18,000+", "Tickets Booked",    "#00C2FF"),
            statSep(),
            statItem("🏛", "6",       "Multiplex Screens", "#22C55E"),
            statSep(),
            statItem("⭐", "4.9",     "Customer Rating",   "#F59E0B")
        );
        return bar;
    }

    private VBox statItem(String emoji, String value, String label, String color) {
        VBox item = new VBox(5);
        item.setAlignment(Pos.CENTER);
        item.setPrefWidth(220);

        HBox valueRow = new HBox(8);
        valueRow.setAlignment(Pos.CENTER);
        Label emojiLbl = new Label(emoji);
        emojiLbl.setStyle("-fx-font-size: 20px;");
        Label valLbl = new Label(value);
        valLbl.setStyle(
            "-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: " + color + ";" +
            "-fx-effect: dropshadow(gaussian, " + color + "55, 8, 0, 0, 0);"
        );
        valueRow.getChildren().addAll(emojiLbl, valLbl);

        Label lblLbl = new Label(label);
        lblLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-weight: 600;");
        item.getChildren().addAll(valueRow, lblLbl);
        return item;
    }

    private Region statSep() {
        Region r = new Region();
        r.setMinWidth(1); r.setMinHeight(50);
        r.setStyle("-fx-background-color: rgba(139,92,246,0.20);");
        return r;
    }

    /* ================================================================ */
    /* FEATURES SECTION                                                   */
    /* ================================================================ */
    private VBox createFeaturesSection() {
        VBox section = new VBox(40);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(80, 70, 80, 70));
        section.setStyle("-fx-background-color: #09090F;");

        section.getChildren().add(sectionHeader(
                "🎯  FEATURES",
                "Everything for the Perfect Cinema Night",
                "From discovering movies to scanning your digital ticket — CineFlow handles it all."));

        GridPane grid = new GridPane();
        grid.setHgap(18); grid.setVgap(18);
        grid.setMaxWidth(960);
        grid.setAlignment(Pos.CENTER);
        grid.getColumnConstraints().addAll(colPct(33.33), colPct(33.33), colPct(33.33));

        grid.add(featCard(filmRollShape(),    "Rich Movie Catalog",
            "Search by title, genre, language, rating and duration. Live filters with instant results."), 0, 0);
        grid.add(featCard(seatGridShape(),    "Interactive Seat Map",
            "Color-coded cinema layout — green available, purple selected, amber held, red booked."), 1, 0);
        grid.add(featCard(paymentShape(),     "3 Payment Methods",
            "Pay with UPI, credit/debit card, or reserve now and pay cash at the box office counter."), 2, 0);
        grid.add(featCard(timerShape(),       "5-Minute Seat Hold",
            "Selected seats are locked for 5 minutes during checkout — preventing double-booking live."), 0, 1);
        grid.add(featCard(ticketShape(),      "Digital Ticket Pass",
            "Premium wallet-style digital pass with barcode, booking ID and all booking details."), 1, 1);
        grid.add(featCard(chartShape(),       "Admin Analytics",
            "Revenue bar chart, top movies pie chart, occupancy rates and one-click CSV export."), 2, 1);

        section.getChildren().add(grid);
        return section;
    }

    private VBox featCard(javafx.scene.Node iconNode, String title, String desc) {
        VBox card = new VBox(12);
        card.setStyle(
            "-fx-background-color: rgba(21,19,35,0.90);" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(139,92,246,0.22);" +
            "-fx-border-width: 1px; -fx-border-radius: 16px;" +
            "-fx-padding: 24px;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.10), 14, 0, 0, 4);"
        );

        // Icon wrapper
        StackPane iconWrap = new StackPane(iconNode);
        iconWrap.setMaxSize(46, 46);
        iconWrap.setMinSize(46, 46);
        iconWrap.setStyle(
            "-fx-background-color: rgba(139,92,246,0.14);" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(139,92,246,0.30);" +
            "-fx-border-width: 1px; -fx-border-radius: 12px;"
        );

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #F8FAFC;");

        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #A1A1AA; -fx-line-spacing: 2px;");
        descLbl.setWrapText(true);

        card.getChildren().addAll(iconWrap, titleLbl, descLbl);

        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: rgba(21,19,35,0.98);" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(139,92,246,0.60);" +
            "-fx-border-width: 1px; -fx-border-radius: 16px;" +
            "-fx-padding: 24px;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.35), 22, 0, 0, 8);" +
            "-fx-translate-y: -3px;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: rgba(21,19,35,0.90);" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(139,92,246,0.22);" +
            "-fx-border-width: 1px; -fx-border-radius: 16px;" +
            "-fx-padding: 24px;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.10), 14, 0, 0, 4);" +
            "-fx-translate-y: 0px;"
        ));

        return card;
    }

    /* ================================================================ */
    /* HOW IT WORKS                                                       */
    /* ================================================================ */
    private VBox createHowItWorksSection() {
        VBox section = new VBox(40);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(80, 70, 80, 70));
        section.setStyle(
            "-fx-background-color: rgba(139,92,246,0.04);" +
            "-fx-border-color: rgba(139,92,246,0.10);" +
            "-fx-border-width: 1px 0 1px 0;"
        );

        section.getChildren().add(sectionHeader(
                "🛤  HOW IT WORKS",
                "Book in 4 Simple Steps",
                "From landing to holding a digital ticket — the entire flow takes under 2 minutes."));

        HBox stepsRow = new HBox(0);
        stepsRow.setAlignment(Pos.CENTER);
        stepsRow.setMaxWidth(900);

        stepsRow.getChildren().addAll(
            stepCard("1", searchShape(),  "Discover",    "Browse movies, filter by genre, language or rating."),
            arrowNode(),
            stepCard("2", seatShape(),    "Pick Seats",  "Choose seats on the interactive cinema layout."),
            arrowNode(),
            stepCard("3", walletShape(),  "Pay",          "Confirm with UPI, card or box-office cash."),
            arrowNode(),
            stepCard("4", starShape(),    "Enjoy!",       "Scan your digital pass at the auditorium gate.")
        );

        section.getChildren().add(stepsRow);
        return section;
    }

    private VBox stepCard(String num, javafx.scene.Node icon, String title, String desc) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(178);
        card.setPadding(new Insets(22, 14, 22, 14));
        card.setStyle(
            "-fx-background-color: rgba(21,19,35,0.80);" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(139,92,246,0.22);" +
            "-fx-border-width: 1px; -fx-border-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.08), 12, 0, 0, 3);"
        );

        Label numBadge = new Label(num);
        numBadge.setStyle(
            "-fx-font-size: 11px; -fx-font-weight: 900; -fx-text-fill: #8B5CF6;" +
            "-fx-background-color: rgba(139,92,246,0.14);" +
            "-fx-border-color: rgba(139,92,246,0.38);" +
            "-fx-border-width: 1px; -fx-border-radius: 20px; -fx-background-radius: 20px;" +
            "-fx-padding: 2px 9px;"
        );

        StackPane iconWrap = new StackPane(icon);
        iconWrap.setMaxSize(42, 42);
        iconWrap.setMinSize(42, 42);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #F8FAFC;");

        Label descLbl = new Label(desc);
        descLbl.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #A1A1AA;" +
            "-fx-text-alignment: center; -fx-line-spacing: 1.5px;"
        );
        descLbl.setWrapText(true);
        descLbl.setAlignment(Pos.CENTER);

        card.getChildren().addAll(numBadge, iconWrap, titleLbl, descLbl);
        return card;
    }

    private Label arrowNode() {
        Label a = new Label("→");
        a.setStyle("-fx-font-size: 20px; -fx-text-fill: rgba(139,92,246,0.38); -fx-padding: 0 6px;");
        a.setTranslateY(8);
        return a;
    }

    /* ================================================================ */
    /* GENRES SECTION                                                     */
    /* ================================================================ */
    private VBox createGenresSection() {
        VBox section = new VBox(32);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(80, 70, 80, 70));
        section.setStyle("-fx-background-color: #09090F;");

        section.getChildren().add(sectionHeader(
                "🎭  BROWSE BY GENRE",
                "Find Your Perfect Film",
                "Action to Animation, Sci-Fi to Thriller — something great is always showing."));

        HBox genreRow = new HBox(14);
        genreRow.setAlignment(Pos.CENTER);
        genreRow.getChildren().addAll(
            genrePill("🎬", "Action",    "#EF4444"),
            genrePill("🚀", "Sci-Fi",    "#00C2FF"),
            genrePill("🎭", "Drama",     "#8B5CF6"),
            genrePill("😂", "Comedy",    "#F59E0B"),
            genrePill("✨", "Animation", "#22C55E"),
            genrePill("🔍", "Thriller",  "#EC4899"),
            genrePill("📖", "Biography", "#A78BFA")
        );

        section.getChildren().add(genreRow);
        return section;
    }

    private VBox genrePill(String icon, String name, String color) {
        VBox pill = new VBox(8);
        pill.setAlignment(Pos.CENTER);
        pill.setPrefWidth(110);
        pill.setPadding(new Insets(18, 12, 18, 12));
        String base =
            "-fx-background-color: rgba(21,19,35,0.85);" +
            "-fx-background-radius: 14px;" +
            "-fx-border-color: " + color + "30;" +
            "-fx-border-width: 1px; -fx-border-radius: 14px;" +
            "-fx-cursor: hand;";
        pill.setStyle(base);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 28px;");
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #CBD5E1;");

        pill.getChildren().addAll(iconLbl, nameLbl);

        pill.setOnMouseEntered(e -> {
            pill.setStyle(
                "-fx-background-color: " + color + "18;" +
                "-fx-background-radius: 14px;" +
                "-fx-border-color: " + color + "75;" +
                "-fx-border-width: 1.5px; -fx-border-radius: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, " + color + "50, 16, 0, 0, 5);" +
                "-fx-translate-y: -3px;"
            );
            nameLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        });
        pill.setOnMouseExited(e -> {
            pill.setStyle(base);
            nameLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #CBD5E1;");
        });
        pill.setOnMouseClicked(e -> NavigationManager.showLoginView());
        return pill;
    }

    /* ================================================================ */
    /* CTA SECTION                                                        */
    /* ================================================================ */
    private StackPane createCtaSection() {
        StackPane section = new StackPane();
        section.setMinHeight(300);

        // Background image layer (using second downloaded image)
        StackPane imgLayer2 = new StackPane();
        imgLayer2.setStyle("-fx-background-color: #09090F;");
        imgLayer2.setPrefSize(1100, 300);

        ImageView bg2 = new ImageView();
        bg2.setPreserveRatio(false);
        bg2.setSmooth(true);
        bg2.fitWidthProperty().bind(imgLayer2.widthProperty());
        bg2.setFitHeight(300);
        bg2.setOpacity(0);

        Thread bind2 = new Thread(() -> {
            try { Thread.sleep(120); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                Image ctaBg = loadResource("/images/landing_bg2.jpg");
                if (ctaBg != null) {
                    bg2.setImage(ctaBg);
                    FadeTransition ft = new FadeTransition(Duration.millis(800), bg2);
                    ft.setToValue(0.22);
                    ft.play();
                }
            });
        }, "img-bind2");
        bind2.setDaemon(true);
        bind2.start();

        imgLayer2.getChildren().add(bg2);
        section.getChildren().add(imgLayer2);

        // Glow overlay
        Pane glow = new Pane();
        glow.setPrefSize(1100, 300);
        Circle c1 = new Circle(300, 150, 280);
        c1.setFill(new RadialGradient(0,0,0.5,0.5,0.5,true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#8B5CF6", 0.22)),
            new Stop(1, Color.web("#09090F", 0.0))));
        Circle c2 = new Circle(800, 150, 240);
        c2.setFill(new RadialGradient(0,0,0.5,0.5,0.5,true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#00C2FF", 0.15)),
            new Stop(1, Color.web("#09090F", 0.0))));
        glow.getChildren().addAll(c1, c2);
        section.getChildren().add(glow);

        // Content
        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(70, 40, 70, 40));

        Label headline = new Label("Ready to book your next cinema night?");
        headline.setStyle(
            "-fx-font-size: 30px; -fx-font-weight: 900; -fx-text-fill: #F8FAFC;" +
            "-fx-text-alignment: center;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 12, 0, 0, 3);"
        );
        headline.setWrapText(true);
        headline.setAlignment(Pos.CENTER);

        Label sub = new Label("Join thousands of movie lovers — create a free account in seconds.");
        sub.setStyle("-fx-font-size: 15px; -fx-text-fill: #A1A1AA; -fx-text-alignment: center;");
        sub.setAlignment(Pos.CENTER);

        HBox btnRow = new HBox(16);
        btnRow.setAlignment(Pos.CENTER);

        Button createBtn = heroPrimaryBtn("🎬  Create Free Account");
        createBtn.setOnAction(e -> NavigationManager.showRegistrationView());

        Button siBtn = heroSecondaryBtn("Sign In  →");
        siBtn.setOnAction(e -> NavigationManager.showLoginView());

        btnRow.getChildren().addAll(createBtn, siBtn);
        content.getChildren().addAll(headline, sub, btnRow);
        section.getChildren().add(content);
        return section;
    }

    /* ================================================================ */
    /* FOOTER                                                             */
    /* ================================================================ */
    private HBox createFooter() {
        HBox footer = new HBox(18);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(20, 36, 20, 36));
        footer.setStyle(
            "-fx-background-color: rgba(9,9,15,0.97);" +
            "-fx-border-color: rgba(139,92,246,0.14);" +
            "-fx-border-width: 1px 0 0 0;"
        );

        HBox logoArea = new HBox(8);
        logoArea.setAlignment(Pos.CENTER_LEFT);
        logoArea.getChildren().addAll(filmFrameIcon(14), buildLogoLabel());

        Label copy = new Label("© 2026 CineFlow  ✦  All rights reserved");
        copy.setStyle("-fx-font-size: 11px; -fx-text-fill: #374151;");

        // Image credit
        Label credit = new Label("Background: Unsplash (CC0)");
        credit.setStyle("-fx-font-size: 10px; -fx-text-fill: #1F2937;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button loginFtr = footerLink("Sign In");
        loginFtr.setOnAction(e -> NavigationManager.showLoginView());

        Button regFtr = footerLink("Create Account");
        regFtr.setOnAction(e -> NavigationManager.showRegistrationView());

        footer.getChildren().addAll(logoArea, copy, credit, spacer, loginFtr, regFtr);
        return footer;
    }

    /* ================================================================ */
    /* JAVAFX ICON SHAPES (no external SVG needed)                       */
    /* ================================================================ */

    /** Film frame / clapperboard icon */
    private javafx.scene.Node filmFrameIcon(double size) {
        StackPane sp = new StackPane();
        Rectangle outer = new Rectangle(size * 1.1, size * 0.9);
        outer.setFill(Color.TRANSPARENT);
        outer.setStroke(Color.web("#8B5CF6", 0.80));
        outer.setStrokeWidth(size * 0.08);
        outer.setArcWidth(size * 0.2);
        outer.setArcHeight(size * 0.2);

        // Film perforations (small rects top and bottom)
        HBox topPerfs = new HBox(size * 0.12);
        topPerfs.setAlignment(Pos.CENTER);
        for (int i = 0; i < 4; i++) {
            Rectangle r = new Rectangle(size * 0.13, size * 0.10);
            r.setFill(Color.web("#8B5CF6", 0.70));
            r.setArcWidth(2); r.setArcHeight(2);
            topPerfs.getChildren().add(r);
        }
        sp.getChildren().addAll(outer, topPerfs);
        sp.setMaxSize(size * 1.3, size * 1.1);
        return sp;
    }

    /** Film roll */
    private javafx.scene.Node filmRollShape() {
        StackPane sp = new StackPane();
        Circle outer = new Circle(14); outer.setFill(Color.TRANSPARENT);
        outer.setStroke(Color.web("#8B5CF6", 0.80)); outer.setStrokeWidth(2.5);
        Circle inner = new Circle(6); inner.setFill(Color.TRANSPARENT);
        inner.setStroke(Color.web("#00C2FF", 0.65)); inner.setStrokeWidth(2);
        Circle hub = new Circle(3);
        hub.setFill(Color.web("#8B5CF6", 0.90));
        sp.getChildren().addAll(outer, inner, hub);
        return sp;
    }

    /** Seat grid */
    private javafx.scene.Node seatGridShape() {
        GridPane g = new GridPane(); g.setHgap(3); g.setVgap(3);
        for (int r = 0; r < 2; r++) for (int c = 0; c < 4; c++) {
            Rectangle s = new Rectangle(7, 7);
            s.setFill(Color.web(c == 2 && r == 0 ? "#8B5CF6" : "#22C55E", 0.75));
            s.setArcWidth(2); s.setArcHeight(2);
            g.add(s, c, r);
        }
        StackPane sp = new StackPane(g); return sp;
    }

    /** Payment / card */
    private javafx.scene.Node paymentShape() {
        StackPane sp = new StackPane();
        Rectangle card = new Rectangle(28, 20);
        card.setFill(Color.TRANSPARENT);
        card.setStroke(Color.web("#8B5CF6", 0.80)); card.setStrokeWidth(2);
        card.setArcWidth(4); card.setArcHeight(4);
        Rectangle stripe = new Rectangle(28, 5);
        stripe.setFill(Color.web("#8B5CF6", 0.40));
        stripe.setTranslateY(-5);
        sp.getChildren().addAll(card, stripe);
        return sp;
    }

    /** Timer / clock */
    private javafx.scene.Node timerShape() {
        StackPane sp = new StackPane();
        Circle face = new Circle(13); face.setFill(Color.TRANSPARENT);
        face.setStroke(Color.web("#F59E0B", 0.80)); face.setStrokeWidth(2.5);
        // Hour hand
        Line h = new Line(0, 0, 0, -8);
        h.setStroke(Color.web("#F59E0B", 0.90)); h.setStrokeWidth(2);
        h.setStrokeLineCap(StrokeLineCap.ROUND);
        // Minute hand
        Line m = new Line(0, 0, 6, 0);
        m.setStroke(Color.web("#F59E0B", 0.70)); m.setStrokeWidth(1.5);
        m.setStrokeLineCap(StrokeLineCap.ROUND);
        sp.getChildren().addAll(face, h, m);
        return sp;
    }

    /** Ticket */
    private javafx.scene.Node ticketShape() {
        StackPane sp = new StackPane();
        Rectangle base = new Rectangle(28, 16);
        base.setFill(Color.TRANSPARENT);
        base.setStroke(Color.web("#22C55E", 0.80)); base.setStrokeWidth(2);
        base.setArcWidth(4); base.setArcHeight(4);
        // Perforated left
        Line perf = new Line(-8, -6, -8, 6);
        perf.setStroke(Color.web("#22C55E", 0.55)); perf.setStrokeWidth(1.5);
        perf.getStrokeDashArray().addAll(2.0, 2.0);
        sp.getChildren().addAll(base, perf);
        return sp;
    }

    /** Bar chart */
    private javafx.scene.Node chartShape() {
        HBox bars = new HBox(3);
        bars.setAlignment(Pos.BOTTOM_LEFT);
        int[] heights = {10, 18, 13, 22, 16};
        String[] colors = {"#8B5CF6","#00C2FF","#8B5CF6","#A78BFA","#00C2FF"};
        for (int i = 0; i < 5; i++) {
            Rectangle b = new Rectangle(5, heights[i]);
            b.setFill(Color.web(colors[i], 0.80));
            b.setArcWidth(2); b.setArcHeight(2);
            bars.getChildren().add(b);
        }
        return new StackPane(bars);
    }

    /** Search magnifier */
    private javafx.scene.Node searchShape() {
        StackPane sp = new StackPane();
        Circle lens = new Circle(10); lens.setFill(Color.TRANSPARENT);
        lens.setStroke(Color.web("#8B5CF6", 0.80)); lens.setStrokeWidth(2.5);
        Line handle = new Line(7, 7, 13, 13);
        handle.setStroke(Color.web("#8B5CF6", 0.80)); handle.setStrokeWidth(2.5);
        handle.setStrokeLineCap(StrokeLineCap.ROUND);
        sp.getChildren().addAll(lens, handle);
        return sp;
    }

    /** Seat outline */
    private javafx.scene.Node seatShape() {
        StackPane sp = new StackPane();
        Rectangle s = new Rectangle(20, 16);
        s.setFill(Color.web("#8B5CF6", 0.25));
        s.setStroke(Color.web("#8B5CF6", 0.80)); s.setStrokeWidth(2);
        s.setArcWidth(4); s.setArcHeight(4);
        Rectangle back = new Rectangle(20, 6);
        back.setFill(Color.web("#8B5CF6", 0.45));
        back.setStroke(Color.web("#8B5CF6", 0.80)); back.setStrokeWidth(1.5);
        back.setArcWidth(3); back.setArcHeight(3);
        back.setTranslateY(-11);
        sp.getChildren().addAll(s, back);
        return sp;
    }

    /** Wallet */
    private javafx.scene.Node walletShape() {
        StackPane sp = new StackPane();
        Rectangle body = new Rectangle(26, 18);
        body.setFill(Color.TRANSPARENT);
        body.setStroke(Color.web("#22C55E", 0.80)); body.setStrokeWidth(2);
        body.setArcWidth(4); body.setArcHeight(4);
        Circle coin = new Circle(5);
        coin.setFill(Color.TRANSPARENT);
        coin.setStroke(Color.web("#22C55E", 0.65)); coin.setStrokeWidth(1.5);
        coin.setTranslateX(8);
        sp.getChildren().addAll(body, coin);
        return sp;
    }

    /** Star */
    private javafx.scene.Node starShape() {
        Label star = new Label("★");
        star.setStyle("-fx-font-size: 24px; -fx-text-fill: #F59E0B;");
        return new StackPane(star);
    }

    /* ================================================================ */
    /* REUSABLE COMPONENTS                                               */
    /* ================================================================ */
    private Label buildLogoLabel() {
        Label logo = new Label("CINEFLOW");
        logo.setStyle(
            "-fx-font-size: 20px; -fx-font-weight: 900;" +
            "-fx-text-fill: linear-gradient(to right, #8B5CF6, #00C2FF);" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.50), 12, 0, 0, 1);"
        );
        return logo;
    }

    private HBox buildEyebrow(String icon, String text) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER);
        row.setStyle(
            "-fx-background-color: rgba(139,92,246,0.12);" +
            "-fx-border-color: rgba(139,92,246,0.35);" +
            "-fx-border-width: 1px; -fx-border-radius: 20px; -fx-background-radius: 20px;" +
            "-fx-padding: 5px 18px;"
        );
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 14px;");
        Label textLbl = new Label(text);
        textLbl.setStyle(
            "-fx-font-size: 11px; -fx-font-weight: 900; -fx-text-fill: #8B5CF6; -fx-letter-spacing: 1px;"
        );
        row.getChildren().addAll(iconLbl, textLbl);
        return row;
    }

    private HBox trustChip(String icon, String text) {
        HBox chip = new HBox(7);
        chip.setAlignment(Pos.CENTER);
        chip.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-border-color: rgba(255,255,255,0.12);" +
            "-fx-border-width: 1px; -fx-border-radius: 20px; -fx-background-radius: 20px;" +
            "-fx-padding: 6px 14px;"
        );
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 14px;");
        Label textLbl = new Label(text);
        textLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #A1A1AA; -fx-font-weight: 600;");
        chip.getChildren().addAll(iconLbl, textLbl);
        return chip;
    }

    private VBox sectionHeader(String eyebrow, String title, String subtitle) {
        VBox h = new VBox(10);
        h.setAlignment(Pos.CENTER);
        h.setMaxWidth(700);

        Label eyeLbl = new Label(eyebrow);
        eyeLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #8B5CF6; -fx-letter-spacing: 1px;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 30px; -fx-font-weight: 900; -fx-text-fill: #F8FAFC; -fx-text-alignment: center;");
        titleLbl.setWrapText(true);
        titleLbl.setAlignment(Pos.CENTER);

        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #A1A1AA; -fx-text-alignment: center; -fx-line-spacing: 2px;");
        subLbl.setWrapText(true);
        subLbl.setAlignment(Pos.CENTER);

        h.getChildren().addAll(eyeLbl, titleLbl, subLbl);
        return h;
    }

    private Circle dot(String hex) { return new Circle(5, Color.web(hex)); }

    private Label navLink(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #A1A1AA; -fx-cursor: hand; -fx-padding: 0 8px;");
        l.setOnMouseEntered(e -> l.setStyle("-fx-font-size: 13px; -fx-text-fill: #C4B5FD; -fx-cursor: hand; -fx-padding: 0 8px;"));
        l.setOnMouseExited(e -> l.setStyle("-fx-font-size: 13px; -fx-text-fill: #A1A1AA; -fx-cursor: hand; -fx-padding: 0 8px;"));
        return l;
    }

    private Button navOutlineBtn(String text) {
        Button b = new Button(text);
        String s0 = "-fx-background-color: transparent; -fx-text-fill: #C4B5FD; -fx-font-weight: 700; -fx-font-size: 13px; -fx-border-color: rgba(139,92,246,0.40); -fx-border-width: 1px; -fx-border-radius: 9px; -fx-background-radius: 9px; -fx-padding: 7px 18px; -fx-cursor: hand;";
        String s1 = "-fx-background-color: rgba(139,92,246,0.12); -fx-text-fill: #DDD6FE; -fx-font-weight: 700; -fx-font-size: 13px; -fx-border-color: rgba(139,92,246,0.65); -fx-border-width: 1px; -fx-border-radius: 9px; -fx-background-radius: 9px; -fx-padding: 7px 18px; -fx-cursor: hand;";
        b.setStyle(s0);
        b.setOnMouseEntered(e -> b.setStyle(s1));
        b.setOnMouseExited(e -> b.setStyle(s0));
        return b;
    }

    private Button navPrimaryBtn(String text) {
        Button b = new Button(text);
        String s0 = "-fx-background-color: linear-gradient(to right, #8B5CF6, #6D28D9); -fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 13px; -fx-border-radius: 9px; -fx-background-radius: 9px; -fx-padding: 7px 20px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(139,92,246,0.45), 12, 0, 0, 3);";
        String s1 = "-fx-background-color: linear-gradient(to right, #9D6FFF, #7C3AED); -fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 13px; -fx-border-radius: 9px; -fx-background-radius: 9px; -fx-padding: 7px 20px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(139,92,246,0.70), 18, 0, 0, 5);";
        b.setStyle(s0);
        b.setOnMouseEntered(e -> b.setStyle(s1));
        b.setOnMouseExited(e -> b.setStyle(s0));
        return b;
    }

    private Button heroPrimaryBtn(String text) {
        Button b = new Button(text);
        String s0 = "-fx-background-color: linear-gradient(to right, #8B5CF6, #6D28D9); -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 15px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 13px 32px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(139,92,246,0.55), 18, 0, 0, 5);";
        String s1 = "-fx-background-color: linear-gradient(to right, #9D6FFF, #7C3AED); -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 15px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 13px 32px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(139,92,246,0.80), 24, 0, 0, 7); -fx-scale-x: 1.03; -fx-scale-y: 1.03;";
        b.setStyle(s0);
        b.setOnMouseEntered(e -> b.setStyle(s1));
        b.setOnMouseExited(e -> b.setStyle(s0));
        return b;
    }

    private Button heroSecondaryBtn(String text) {
        Button b = new Button(text);
        String s0 = "-fx-background-color: rgba(139,92,246,0.10); -fx-text-fill: #C4B5FD; -fx-font-weight: 700; -fx-font-size: 15px; -fx-border-color: rgba(139,92,246,0.45); -fx-border-width: 1px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 13px 30px; -fx-cursor: hand;";
        String s1 = "-fx-background-color: rgba(139,92,246,0.22); -fx-text-fill: #DDD6FE; -fx-font-weight: 700; -fx-font-size: 15px; -fx-border-color: rgba(139,92,246,0.70); -fx-border-width: 1px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 13px 30px; -fx-cursor: hand; -fx-scale-x: 1.03; -fx-scale-y: 1.03;";
        b.setStyle(s0);
        b.setOnMouseEntered(e -> b.setStyle(s1));
        b.setOnMouseExited(e -> b.setStyle(s0));
        return b;
    }

    private Button footerLink(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-font-size: 11px; -fx-text-fill: #8B5CF6; -fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-font-size: 11px; -fx-text-fill: #A78BFA; -fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-font-size: 11px; -fx-text-fill: #8B5CF6; -fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;"));
        return b;
    }

    private ColumnConstraints colPct(double pct) {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(pct);
        return cc;
    }

    /* ================================================================ */
    /* ANIMATIONS                                                         */
    /* ================================================================ */
    private void staggerIn(javafx.scene.Node node, int delayMs, double fromY) {
        node.setOpacity(0);
        node.setTranslateY(fromY);
        FadeTransition ft = new FadeTransition(Duration.millis(580), node);
        ft.setToValue(1.0);
        ft.setDelay(Duration.millis(delayMs));
        ft.play();
        TranslateTransition tt = new TranslateTransition(Duration.millis(580), node);
        tt.setToY(0);
        tt.setDelay(Duration.millis(delayMs));
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.play();
    }

    private void slowFloat(Circle orb, double dx, double dy) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(9 + Math.random() * 4), orb);
        tt.setByX(dx); tt.setByY(dy);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    private void floatParticle(Circle p) {
        double dur = 5 + Math.random() * 9;
        TranslateTransition tt = new TranslateTransition(Duration.seconds(dur), p);
        tt.setByX((Math.random() - 0.5) * 100);
        tt.setByY((Math.random() - 0.5) * 80);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        FadeTransition ft = new FadeTransition(Duration.seconds(dur * 0.8), p);
        ft.setFromValue(p.getOpacity());
        ft.setToValue(Math.random() * 0.22 + 0.04);
        ft.setAutoReverse(true);
        ft.setCycleCount(Animation.INDEFINITE);
        tt.play(); ft.play();
    }
}

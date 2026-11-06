package com.cineflow.ui.auth;

import com.cineflow.model.User;
import com.cineflow.service.AuthService;
import com.cineflow.service.AuthenticationException;
import com.cineflow.ui.common.NavigationManager;
import com.cineflow.util.AlertHelper;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.util.Duration;

/**
 * CineFlow — Premium Cinematic Login View.
 * Deep space glassmorphism with animated background orbs, particle effects,
 * and a premium cinema-grade authentication experience.
 */
public class LoginView {

    private final AuthService authService;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button loginButton;

    public LoginView() {
        this.authService = new AuthService();
    }

    public Parent getView() {
        StackPane root = new StackPane();
        root.getStyleClass().add("app-container");

        // Animated background layer
        Pane bgLayer = createAnimatedBackground();
        root.getChildren().add(bgLayer);

        // Split layout: left branding, right form
        HBox splitLayout = createSplitLayout();
        root.getChildren().add(splitLayout);

        return root;
    }

    /* ---------------------------------------------------------------- */
    /* ANIMATED BACKGROUND                                                */
    /* ---------------------------------------------------------------- */
    private Pane createAnimatedBackground() {
        Pane bg = new Pane();
        bg.setStyle("-fx-background-color: transparent;");
        bg.setPrefSize(1000, 700);

        // Glowing orb — purple (left)
        Circle orb1 = createGlowOrb(180, 200, 240,
                Color.web("#8B5CF6", 0.18), Color.web("#6D28D9", 0.04));
        // Glowing orb — electric blue (right)
        Circle orb2 = createGlowOrb(820, 500, 200,
                Color.web("#00C2FF", 0.15), Color.web("#0066FF", 0.03));
        // Glowing orb — deep purple (bottom centre)
        Circle orb3 = createGlowOrb(500, 650, 150,
                Color.web("#7C3AED", 0.12), Color.web("#5B21B6", 0.02));

        // Animated floating particles
        for (int i = 0; i < 22; i++) {
            Circle particle = new Circle(Math.random() * 2.5 + 0.5);
            particle.setFill(Color.web(i % 3 == 0 ? "#8B5CF6" : i % 3 == 1 ? "#00C2FF" : "#FFFFFF",
                    Math.random() * 0.35 + 0.08));
            particle.setLayoutX(Math.random() * 1000);
            particle.setLayoutY(Math.random() * 700);
            animateParticle(particle);
            bg.getChildren().add(particle);
        }

        // Subtle grid lines (cinematic depth)
        for (int x = 0; x < 1000; x += 80) {
            Line vLine = new Line(x, 0, x, 700);
            vLine.setStroke(Color.web("#8B5CF6", 0.04));
            vLine.setStrokeWidth(0.5);
            bg.getChildren().add(vLine);
        }
        for (int y = 0; y < 700; y += 80) {
            Line hLine = new Line(0, y, 1000, y);
            hLine.setStroke(Color.web("#8B5CF6", 0.04));
            hLine.setStrokeWidth(0.5);
            bg.getChildren().add(hLine);
        }

        // Animate orbs slowly
        animateOrb(orb1, 18, 12);
        animateOrb(orb2, -14, 16);
        animateOrb(orb3, 10, -10);

        bg.getChildren().addAll(0, java.util.Arrays.asList(orb1, orb2, orb3));
        return bg;
    }

    private Circle createGlowOrb(double cx, double cy, double r, Color innerColor, Color outerColor) {
        Circle orb = new Circle(cx, cy, r);
        RadialGradient rg = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, innerColor),
                new Stop(1, outerColor));
        orb.setFill(rg);
        return orb;
    }

    private void animateOrb(Circle orb, double dx, double dy) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(8 + Math.random() * 4), orb);
        tt.setByX(dx);
        tt.setByY(dy);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    private void animateParticle(Circle particle) {
        double duration = 4 + Math.random() * 10;
        TranslateTransition tt = new TranslateTransition(Duration.seconds(duration), particle);
        tt.setByX((Math.random() - 0.5) * 120);
        tt.setByY((Math.random() - 0.5) * 80);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition ft = new FadeTransition(Duration.seconds(duration * 0.7), particle);
        ft.setFromValue(particle.getOpacity());
        ft.setToValue(Math.random() * 0.30 + 0.05);
        ft.setAutoReverse(true);
        ft.setCycleCount(Animation.INDEFINITE);

        tt.play();
        ft.play();
    }

    /* ---------------------------------------------------------------- */
    /* SPLIT LAYOUT                                                       */
    /* ---------------------------------------------------------------- */
    private HBox createSplitLayout() {
        HBox layout = new HBox();
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(1000, 700);

        // Left branding panel
        VBox leftPanel = createBrandingPanel();
        HBox.setHgrow(leftPanel, Priority.ALWAYS);

        // Right form panel
        VBox rightPanel = createLoginFormPanel();
        rightPanel.setMinWidth(420);
        rightPanel.setMaxWidth(420);

        layout.getChildren().addAll(leftPanel, rightPanel);
        return layout;
    }

    /* ---------------------------------------------------------------- */
    /* LEFT PANEL — Cinematic Branding                                   */
    /* ---------------------------------------------------------------- */
    private VBox createBrandingPanel() {
        VBox panel = new VBox(24);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(60, 50, 60, 70));

        // Film reel icon (composed with shapes)
        StackPane reelIcon = createFilmReelIcon();

        Label headline = new Label("Your Next Movie\nExperience Starts Here.");
        headline.setStyle("-fx-font-size: 34px; -fx-font-weight: 900; -fx-text-fill: #F8FAFC; -fx-line-spacing: 4px;");
        headline.setWrapText(true);

        Label subtext = new Label("Discover movies. Choose your seats.\nExperience cinema differently.");
        subtext.setStyle("-fx-font-size: 16px; -fx-text-fill: #A1A1AA; -fx-line-spacing: 2px;");
        subtext.setWrapText(true);

        // Feature list
        VBox features = new VBox(12);
        features.getChildren().addAll(
                createFeatureRow("🎬", "Curated movie catalog, live filters"),
                createFeatureRow("🎟", "Interactive 3D seat selection map"),
                createFeatureRow("💳", "Instant UPI, Card & Cash checkout"),
                createFeatureRow("📊", "Real-time analytics dashboard")
        );

        // Floating ticket decoration
        HBox ticketDecor = createHolographicTicket();

        panel.getChildren().addAll(reelIcon, headline, subtext, features, ticketDecor);

        // Entrance animation
        panel.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(900), panel);
        ft.setToValue(1.0);
        ft.setDelay(Duration.millis(200));
        ft.play();

        TranslateTransition tt = new TranslateTransition(Duration.millis(900), panel);
        tt.setFromX(-30);
        tt.setToX(0);
        tt.setDelay(Duration.millis(200));
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.play();

        return panel;
    }

    private StackPane createFilmReelIcon() {
        StackPane reel = new StackPane();
        Circle outerRing = new Circle(38);
        outerRing.setFill(Color.TRANSPARENT);
        outerRing.setStroke(Color.web("#8B5CF6", 0.65));
        outerRing.setStrokeWidth(2.5);

        Circle innerRing = new Circle(22);
        innerRing.setFill(Color.TRANSPARENT);
        innerRing.setStroke(Color.web("#00C2FF", 0.55));
        innerRing.setStrokeWidth(2);

        Circle hub = new Circle(8);
        hub.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#8B5CF6")),
                new Stop(1, Color.web("#00C2FF"))));

        Label reelLabel = new Label("◉");
        reelLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #8B5CF6;");

        reel.getChildren().addAll(outerRing, innerRing, hub);
        reel.setMaxSize(80, 80);

        // Rotate animation
        RotateTransition rot = new RotateTransition(Duration.seconds(12), reel);
        rot.setByAngle(360);
        rot.setCycleCount(Animation.INDEFINITE);
        rot.setInterpolator(Interpolator.LINEAR);
        rot.play();

        return reel;
    }

    private HBox createFeatureRow(String icon, String text) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px;");

        Label textLbl = new Label(text);
        textLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #CBD5E1;");

        row.getChildren().addAll(iconLbl, textLbl);
        return row;
    }

    private HBox createHolographicTicket() {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setPadding(new Insets(12, 0, 0, 0));

        VBox ticket = new VBox(4);
        ticket.setStyle(
            "-fx-background-color: linear-gradient(to right, rgba(139,92,246,0.20), rgba(0,194,255,0.15));" +
            "-fx-border-color: rgba(139,92,246,0.45);" +
            "-fx-border-width: 1px; -fx-border-radius: 10px; -fx-background-radius: 10px;" +
            "-fx-padding: 10px 18px;"
        );
        ticket.setMaxWidth(280);

        Label ticketTitle = new Label("🎫  SAMPLE PASS");
        ticketTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #8B5CF6;");

        Label ticketMovie = new Label("Interstellar • Screen 2 • Row D");
        ticketMovie.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #E2E8F0;");

        Label ticketSeat = new Label("Seat D6, D7  •  ₹ 340.00");
        ticketSeat.setStyle("-fx-font-size: 12px; -fx-text-fill: #A1A1AA;");

        ticket.getChildren().addAll(ticketTitle, ticketMovie, ticketSeat);

        // Float animation
        TranslateTransition ft = new TranslateTransition(Duration.seconds(3), ticket);
        ft.setByY(-6);
        ft.setAutoReverse(true);
        ft.setCycleCount(Animation.INDEFINITE);
        ft.setInterpolator(Interpolator.EASE_BOTH);
        ft.play();

        wrapper.getChildren().add(ticket);
        return wrapper;
    }

    /* ---------------------------------------------------------------- */
    /* RIGHT PANEL — Login Form                                          */
    /* ---------------------------------------------------------------- */
    private VBox createLoginFormPanel() {
        VBox panel = new VBox();
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(40, 50, 40, 30));

        VBox card = new VBox(18);
        card.getStyleClass().add("card");
        card.setMaxWidth(360);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(32, 34, 32, 34));

        // Brand
        Label brandLabel = new Label("CINEFLOW");
        brandLabel.getStyleClass().add("brand-text");

        Label tagline = new Label("Premium Cinema Booking");
        tagline.getStyleClass().add("text-subtitle");
        tagline.setStyle("-fx-font-size: 12px;");

        Label headerPrompt = new Label("Sign in to your account");
        headerPrompt.getStyleClass().add("title-small");
        headerPrompt.setStyle("-fx-padding: 6px 0 2px 0; -fx-font-size: 16px;");

        // Error banner
        errorLabel = new Label();
        errorLabel.getStyleClass().add("badge-danger");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setWrapText(true);
        errorLabel.setAlignment(Pos.CENTER);

        // Username
        VBox usernameBox = new VBox(6);
        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("text-subtitle");
        userLabel.setStyle("-fx-font-weight: 600;");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameBox.getChildren().addAll(userLabel, usernameField);

        // Password
        VBox passwordBox = new VBox(6);
        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("text-subtitle");
        passLabel.setStyle("-fx-font-weight: 600;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordBox.getChildren().addAll(passLabel, passwordField);

        // Login button
        loginButton = new Button("Sign In  →");
        loginButton.getStyleClass().add("button-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle("-fx-font-size: 15px; -fx-padding: 12px;");
        loginButton.setOnAction(e -> handleLogin());

        usernameField.setOnKeyPressed(this::handleKeyPressed);
        passwordField.setOnKeyPressed(this::handleKeyPressed);

        // Register link
        HBox registerBox = new HBox(6);
        registerBox.setAlignment(Pos.CENTER);
        Label noAccountLabel = new Label("New here?");
        noAccountLabel.getStyleClass().add("text-muted");
        Hyperlink registerLink = new Hyperlink("Create an account");
        registerLink.setOnAction(e -> NavigationManager.showRegistrationView());
        registerBox.getChildren().addAll(noAccountLabel, registerLink);

        // Divider
        Region divider = new Region();
        divider.setStyle("-fx-border-color: rgba(139,92,246,0.18); -fx-border-width: 1px 0 0 0; -fx-min-height: 1px;");

        // Demo credentials
        VBox demoBox = createDemoBox();

        card.getChildren().addAll(
                brandLabel, tagline, headerPrompt, errorLabel,
                usernameBox, passwordBox, loginButton,
                registerBox, divider, demoBox
        );

        panel.getChildren().add(card);

        // Entrance animation
        card.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(900), card);
        ft.setToValue(1.0);
        ft.setDelay(Duration.millis(400));
        ft.play();

        TranslateTransition tt = new TranslateTransition(Duration.millis(900), card);
        tt.setFromX(30);
        tt.setToX(0);
        tt.setDelay(Duration.millis(400));
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.play();

        return panel;
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    private void handleLogin() {
        loginButton.setDisable(true);
        loginButton.setText("Signing in...");

        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User authenticatedUser = authService.login(username, password);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            NavigationManager.showDashboard(authenticatedUser);
        } catch (AuthenticationException ex) {
            showError(ex.getMessage());
        } finally {
            loginButton.setDisable(false);
            loginButton.setText("Sign In  →");
        }
    }

    private void showError(String message) {
        errorLabel.setText("⚠  " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Shake animation on error
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), errorLabel);
        shake.setByX(8);
        shake.setAutoReverse(true);
        shake.setCycleCount(6);
        shake.play();
    }

    private VBox createDemoBox() {
        VBox box = new VBox(8);
        box.setStyle(
            "-fx-background-color: rgba(139,92,246,0.06);" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: rgba(139,92,246,0.18);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 10px;"
        );
        box.setAlignment(Pos.CENTER);

        Label demoTitle = new Label("Quick Demo Access");
        demoTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #6B7280;");

        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER);

        Button adminBtn = new Button("Admin Account");
        adminBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 12px;");
        adminBtn.getStyleClass().add("button-secondary");
        adminBtn.setOnAction(e -> {
            usernameField.setText("admin");
            passwordField.setText("Admin@123");
        });

        Button customerBtn = new Button("Customer Account");
        customerBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 12px;");
        customerBtn.getStyleClass().add("button-secondary");
        customerBtn.setOnAction(e -> {
            usernameField.setText("john_doe");
            passwordField.setText("Customer@123");
        });

        btnRow.getChildren().addAll(adminBtn, customerBtn);
        box.getChildren().addAll(demoTitle, btnRow);
        return box;
    }
}

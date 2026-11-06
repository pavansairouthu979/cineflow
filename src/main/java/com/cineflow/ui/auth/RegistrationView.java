package com.cineflow.ui.auth;

import com.cineflow.model.User;
import com.cineflow.service.AuthService;
import com.cineflow.service.ValidationException;
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
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * CineFlow — Premium Customer Registration View.
 * Cinematic glassmorphism registration form with animated background.
 */
public class RegistrationView {

    private final AuthService authService;
    private TextField fullNameField;
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Label errorLabel;
    private Button registerButton;

    public RegistrationView() {
        this.authService = new AuthService();
    }

    public Parent getView() {
        StackPane root = new StackPane();
        root.getStyleClass().add("app-container");

        // Background orbs
        Pane bgLayer = createBackground();
        root.getChildren().add(bgLayer);

        // Center layout
        VBox centerBox = new VBox();
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(24));

        VBox regCard = createRegistrationCard();
        centerBox.getChildren().add(regCard);

        ScrollPane scrollPane = new ScrollPane(centerBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.getStyleClass().add("app-container");

        root.getChildren().add(scrollPane);
        return root;
    }

    private Pane createBackground() {
        Pane bg = new Pane();
        bg.setStyle("-fx-background-color: transparent;");

        Circle orb1 = new Circle(150, 150, 220);
        RadialGradient rg1 = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#8B5CF6", 0.14)),
                new Stop(1, Color.web("#6D28D9", 0.02)));
        orb1.setFill(rg1);

        Circle orb2 = new Circle(800, 600, 180);
        RadialGradient rg2 = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#00C2FF", 0.12)),
                new Stop(1, Color.web("#0066FF", 0.02)));
        orb2.setFill(rg2);

        bg.getChildren().addAll(orb1, orb2);
        return bg;
    }

    private VBox createRegistrationCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setMaxWidth(500);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(32, 38, 32, 38));

        // Brand header
        Label brandLabel = new Label("CINEFLOW");
        brandLabel.getStyleClass().add("brand-text");

        Label title = new Label("Create Your Account");
        title.getStyleClass().add("title-medium");
        title.setStyle("-fx-font-size: 22px;");

        Label subtitle = new Label("Join CineFlow to browse movies, reserve seats and manage your tickets");
        subtitle.getStyleClass().add("text-subtitle");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);

        // Error banner
        errorLabel = new Label();
        errorLabel.getStyleClass().add("badge-danger");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setWrapText(true);
        errorLabel.setAlignment(Pos.CENTER);

        // Divider
        Region topDivider = new Region();
        topDivider.setStyle("-fx-border-color: rgba(139,92,246,0.18); -fx-border-width: 1px 0 0 0; -fx-min-height: 1px;");

        // Full Name
        fullNameField = buildField(null);
        VBox nameBox = buildFieldBox("Full Name", "e.g. Alice Cooper", fullNameField);

        // Username
        usernameField = buildField(null);
        VBox userBox = buildFieldBox("Username", "3-20 characters: letters, digits, underscores", usernameField);

        // Email — use TextField directly
        TextField emailTextField = new TextField();
        emailTextField.setPromptText("e.g. alice@example.com");
        emailField = emailTextField;
        VBox emailBox = buildFieldBox("Email Address", null, emailField);

        // Password
        passwordField = new PasswordField();
        passwordField.setPromptText("Minimum 6 characters");
        VBox passBox = buildFieldBox("Password", null, passwordField);

        // Confirm Password
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter your password");
        VBox confirmBox = buildFieldBox("Confirm Password", null, confirmPasswordField);

        // Two-column layout for some fields
        HBox nameEmailRow = new HBox(16);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        HBox.setHgrow(emailBox, Priority.ALWAYS);
        nameEmailRow.getChildren().addAll(nameBox, emailBox);

        HBox passRow = new HBox(16);
        HBox.setHgrow(passBox, Priority.ALWAYS);
        HBox.setHgrow(confirmBox, Priority.ALWAYS);
        passRow.getChildren().addAll(passBox, confirmBox);

        // Register button
        registerButton = new Button("Create Account  →");
        registerButton.getStyleClass().add("button-primary");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setStyle("-fx-font-size: 15px; -fx-padding: 13px;");
        registerButton.setOnAction(e -> handleRegistration());

        confirmPasswordField.setOnKeyPressed(this::handleKeyPressed);
        passwordField.setOnKeyPressed(this::handleKeyPressed);

        // Back to login
        HBox loginBox = new HBox(6);
        loginBox.setAlignment(Pos.CENTER);
        Label hasAccountLabel = new Label("Already have an account?");
        hasAccountLabel.getStyleClass().add("text-muted");
        Hyperlink loginLink = new Hyperlink("Sign in here");
        loginLink.setOnAction(e -> NavigationManager.showLoginView());
        loginBox.getChildren().addAll(hasAccountLabel, loginLink);

        card.getChildren().addAll(
                brandLabel, title, subtitle, errorLabel,
                topDivider,
                nameEmailRow,
                userBox,
                passRow,
                registerButton,
                loginBox
        );

        // Entrance animation
        card.setOpacity(0);
        card.setTranslateY(20);
        FadeTransition ft = new FadeTransition(Duration.millis(700), card);
        ft.setToValue(1.0);
        ft.setDelay(Duration.millis(150));
        ft.play();

        TranslateTransition tt = new TranslateTransition(Duration.millis(700), card);
        tt.setToY(0);
        tt.setDelay(Duration.millis(150));
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.play();

        return card;
    }

    private TextField buildField(String prompt) {
        TextField tf = new TextField();
        if (prompt != null) tf.setPromptText(prompt);
        return tf;
    }

    private VBox buildFieldBox(String labelText, String prompt, TextField field) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("text-subtitle");
        lbl.setStyle("-fx-font-weight: 600;");
        if (prompt != null) field.setPromptText(prompt);
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private VBox buildFieldBox(String labelText, String prompt, PasswordField field) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("text-subtitle");
        lbl.setStyle("-fx-font-weight: 600;");
        if (prompt != null) field.setPromptText(prompt);
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleRegistration();
        }
    }

    private void handleRegistration() {
        registerButton.setDisable(true);
        registerButton.setText("Creating account...");

        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        try {
            User newUser = authService.register(fullName, username, email, password, confirmPassword);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            AlertHelper.showInfo(
                    "Welcome to CineFlow!",
                    "Account Created Successfully",
                    "Welcome aboard, " + newUser.getFullName() + "! You can now sign in with username: " + newUser.getUsername()
            );
            NavigationManager.showLoginView();

        } catch (ValidationException ex) {
            errorLabel.setText("⚠  " + ex.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);

            // Shake animation
            TranslateTransition shake = new TranslateTransition(Duration.millis(60), errorLabel);
            shake.setByX(8);
            shake.setAutoReverse(true);
            shake.setCycleCount(6);
            shake.play();

        } finally {
            registerButton.setDisable(false);
            registerButton.setText("Create Account  →");
        }
    }
}

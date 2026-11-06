package com.cineflow.ui.auth;

import com.cineflow.model.User;
import com.cineflow.service.AuthService;
import com.cineflow.service.ValidationException;
import com.cineflow.ui.common.NavigationManager;
import com.cineflow.util.AlertHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;

/**
 * JavaFX View for Customer Account Registration.
 * Validates full name, username, email, password strength, and confirmation match.
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
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-container");

        // ScrollPane wrapping the center content for smooth responsive view
        VBox centerBox = new VBox();
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));

        VBox regCard = createRegistrationCard();
        centerBox.getChildren().add(regCard);

        ScrollPane scrollPane = new ScrollPane(centerBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setCenter(scrollPane);
        return root;
    }

    private VBox createRegistrationCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setMaxWidth(480);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(26, 36, 28, 36));

        // Apple macOS Window Controls
        HBox trafficLights = new HBox(6);
        trafficLights.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.shape.Circle redDot = new javafx.scene.shape.Circle(5, javafx.scene.paint.Color.web("#ff5f56"));
        javafx.scene.shape.Circle yellowDot = new javafx.scene.shape.Circle(5, javafx.scene.paint.Color.web("#ffbd2e"));
        javafx.scene.shape.Circle greenDot = new javafx.scene.shape.Circle(5, javafx.scene.paint.Color.web("#27c93f"));
        trafficLights.getChildren().addAll(redDot, yellowDot, greenDot);

        // Brand & Title
        Label brandLabel = new Label("CINEFLOW");
        brandLabel.getStyleClass().add("brand-text");

        Label title = new Label("Create Customer Account");
        title.getStyleClass().add("title-medium");

        Label subtitle = new Label("Register to browse movies, reserve seats and manage tickets");
        subtitle.getStyleClass().add("text-subtitle");

        // Error message banner
        errorLabel = new Label();
        errorLabel.getStyleClass().add("badge-danger");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setAlignment(Pos.CENTER);

        // Full Name Field
        VBox nameBox = new VBox(4);
        Label nameLabel = new Label("Full Name");
        nameLabel.getStyleClass().add("text-subtitle");
        fullNameField = new TextField();
        fullNameField.setPromptText("e.g. Alice Cooper");
        nameBox.getChildren().addAll(nameLabel, fullNameField);

        // Username Field
        VBox userBox = new VBox(4);
        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("text-subtitle");
        usernameField = new TextField();
        usernameField.setPromptText("3-20 letters, digits or underscores");
        userBox.getChildren().addAll(userLabel, usernameField);

        // Email Field
        VBox emailBox = new VBox(4);
        Label emailLabel = new Label("Email Address");
        emailLabel.getStyleClass().add("text-subtitle");
        emailField = new TextField();
        emailField.setPromptText("e.g. alice@example.com");
        emailBox.getChildren().addAll(emailLabel, emailField);

        // Password Field
        VBox passBox = new VBox(4);
        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("text-subtitle");
        passwordField = new PasswordField();
        passwordField.setPromptText("Minimum 6 characters");
        passBox.getChildren().addAll(passLabel, passwordField);

        // Confirm Password Field
        VBox confirmBox = new VBox(4);
        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.getStyleClass().add("text-subtitle");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter password to confirm");
        confirmBox.getChildren().addAll(confirmLabel, confirmPasswordField);

        // Register Action Button
        registerButton = new Button("Register Account");
        registerButton.getStyleClass().add("button-primary");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setStyle("-fx-font-size: 15px; -fx-padding: 12px;");

        // ActionEvent: Handles the registration button click
        registerButton.setOnAction(e -> handleRegistration());

        // KeyEvent: Submits the registration when Enter key is pressed in the fields
        confirmPasswordField.setOnKeyPressed(this::handleKeyPressed);
        passwordField.setOnKeyPressed(this::handleKeyPressed);

        // Back to Login Switcher
        HBox loginBox = new HBox(6);
        loginBox.setAlignment(Pos.CENTER);
        Label hasAccountLabel = new Label("Already have an account?");
        hasAccountLabel.getStyleClass().add("text-muted");

        Hyperlink loginLink = new Hyperlink("Sign In here");
        loginLink.setStyle("-fx-text-fill: #818cf8; -fx-font-weight: bold; -fx-underline: false;");

        // ActionEvent: Navigates back to the login screen
        loginLink.setOnAction(e -> NavigationManager.showLoginView());
        loginBox.getChildren().addAll(hasAccountLabel, loginLink);

        card.getChildren().addAll(
                trafficLights, brandLabel, title, subtitle, errorLabel,
                nameBox, userBox, emailBox, passBox, confirmBox,
                registerButton, loginBox
        );

        return card;
    }

    /**
     * KeyEvent: Submits the registration form when Enter key is pressed.
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleRegistration();
        }
    }

    /**
     * Validates input and delegates registration to AuthService.
     */
    private void handleRegistration() {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        try {
            // Service layer call
            User newUser = authService.register(fullName, username, email, password, confirmPassword);

            // Hide error label on success
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            // ActionEvent: Show success alert and navigate to Login
            AlertHelper.showInfo(
                    "Registration Successful",
                    "Welcome to CineFlow, " + newUser.getFullName() + "!",
                    "Your customer account has been created. You can now log in with username: " + newUser.getUsername()
            );

            NavigationManager.showLoginView();

        } catch (ValidationException ex) {
            errorLabel.setText(ex.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            AlertHelper.showError("Registration Failed", "Validation Error", ex.getMessage());
        }
    }
}


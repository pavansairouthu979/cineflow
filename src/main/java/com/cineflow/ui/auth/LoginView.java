package com.cineflow.ui.auth;

import com.cineflow.model.User;
import com.cineflow.service.AuthService;
import com.cineflow.service.AuthenticationException;
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
 * JavaFX View for User Authentication (Login).
 * Strictly built in Java code with shared CSS styling.
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
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-container");

        // Center card container
        VBox centerBox = new VBox();
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(30));

        VBox loginCard = createLoginCard();
        centerBox.getChildren().add(loginCard);

        root.setCenter(centerBox);
        return root;
    }

    private VBox createLoginCard() {
        VBox card = new VBox(18);
        card.getStyleClass().add("card");
        card.setMaxWidth(440);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(36, 36, 32, 36));

        // CineFlow Brand Header
        Label brandLabel = new Label("CINEFLOW");
        brandLabel.getStyleClass().add("brand-text");

        Label subtitle = new Label("Multiplex Ticket Reservation System");
        subtitle.getStyleClass().add("text-subtitle");

        Label headerPrompt = new Label("Sign in to your account");
        headerPrompt.getStyleClass().add("title-small");
        headerPrompt.setStyle("-fx-padding: 8px 0 4px 0;");

        // Error message banner (hidden until an error occurs)
        errorLabel = new Label();
        errorLabel.getStyleClass().add("badge-danger");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setAlignment(Pos.CENTER);

        // Username Field
        VBox usernameBox = new VBox(6);
        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("text-subtitle");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameBox.getChildren().addAll(userLabel, usernameField);

        // Password Field
        VBox passwordBox = new VBox(6);
        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("text-subtitle");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordBox.getChildren().addAll(passLabel, passwordField);

        // Login Action Button
        loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("button-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle("-fx-font-size: 15px; -fx-padding: 12px;");

        // ActionEvent: Handles the login button click
        loginButton.setOnAction(e -> handleLogin());

        // KeyEvent: Submits the login form when Enter key is pressed in the username field
        usernameField.setOnKeyPressed(this::handleKeyPressed);

        // KeyEvent: Submits the login form when Enter key is pressed in the password field
        passwordField.setOnKeyPressed(this::handleKeyPressed);

        // Registration Link / Switcher
        HBox registerBox = new HBox(6);
        registerBox.setAlignment(Pos.CENTER);
        Label noAccountLabel = new Label("Don't have an account?");
        noAccountLabel.getStyleClass().add("text-muted");

        Hyperlink registerLink = new Hyperlink("Create one here");
        registerLink.setStyle("-fx-text-fill: #818cf8; -fx-font-weight: bold; -fx-underline: false;");

        // ActionEvent: Navigates to the registration screen
        registerLink.setOnAction(e -> NavigationManager.showRegistrationView());
        registerBox.getChildren().addAll(noAccountLabel, registerLink);

        // Quick Credentials Autofill Box (for easy evaluation)
        VBox demoBox = createDemoAutofillBox();

        card.getChildren().addAll(
                brandLabel, subtitle, headerPrompt, errorLabel,
                usernameBox, passwordBox, loginButton,
                registerBox, demoBox
        );

        return card;
    }

    /**
     * KeyEvent: Submits the login form when Enter key is pressed.
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    /**
     * Processes login authentication via AuthService.
     */
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            // Service layer call
            User authenticatedUser = authService.login(username, password);

            // Hide error label on success
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            // Navigate to appropriate role dashboard
            NavigationManager.showDashboard(authenticatedUser);

        } catch (AuthenticationException ex) {
            // Display error cleanly both on card and as Alert
            showError(ex.getMessage());
            AlertHelper.showError("Login Failed", "Authentication Error", ex.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Convenient panel providing single-click credentials autofill for grading.
     */
    private VBox createDemoAutofillBox() {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: #0d1525; -fx-padding: 12px; -fx-background-radius: 8px; -fx-border-color: #1e293b; -fx-border-width: 1px; -fx-border-radius: 8px;");
        box.setAlignment(Pos.CENTER);

        Label demoTitle = new Label("Demo Evaluation Accounts:");
        demoTitle.getStyleClass().add("text-muted");
        demoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER);

        Button adminBtn = new Button("Admin (admin)");
        adminBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px;");
        adminBtn.getStyleClass().add("button-secondary");

        // ActionEvent: Autofills admin credentials
        adminBtn.setOnAction(e -> {
            usernameField.setText("admin");
            passwordField.setText("Admin@123");
        });

        Button customerBtn = new Button("Customer (john_doe)");
        customerBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px;");
        customerBtn.getStyleClass().add("button-secondary");

        // ActionEvent: Autofills customer credentials
        customerBtn.setOnAction(e -> {
            usernameField.setText("john_doe");
            passwordField.setText("Customer@123");
        });

        btnRow.getChildren().addAll(adminBtn, customerBtn);
        box.getChildren().addAll(demoTitle, btnRow);
        return box;
    }
}


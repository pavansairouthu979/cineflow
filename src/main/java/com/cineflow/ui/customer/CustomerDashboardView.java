package com.cineflow.ui.customer;

import com.cineflow.model.User;
import com.cineflow.ui.common.NavigationManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * Customer Landing Dashboard view.
 * Displayed upon successful CUSTOMER authentication.
 */
public class CustomerDashboardView {

    private final User customerUser;

    public CustomerDashboardView(User customerUser) {
        this.customerUser = customerUser;
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-container");

        // Top Navigation Header
        VBox topBox = createTopNav();
        root.setTop(topBox);

        // Center Content
        VBox centerContent = new VBox(24);
        centerContent.setPadding(new Insets(30, 40, 30, 40));
        centerContent.setAlignment(Pos.TOP_CENTER);

        // Welcome Card
        VBox welcomeCard = createWelcomeCard();
        centerContent.getChildren().add(welcomeCard);

        // Customer Feature Preview Cards (Phase 4 & 5 Modules)
        GridPane featuresGrid = createFeaturesGrid();
        centerContent.getChildren().add(featuresGrid);

        ScrollPane scrollPane = new ScrollPane(centerContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setCenter(scrollPane);

        // Footer
        HBox footer = createFooter();
        root.setBottom(footer);

        return root;
    }

    private VBox createTopNav() {
        VBox header = new VBox(6);
        header.setPadding(new Insets(16, 40, 16, 40));
        header.setStyle("-fx-background-color: #111a2e; -fx-border-color: #232d48; -fx-border-width: 0 0 1px 0;");

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        Label brandLabel = new Label("CINEFLOW");
        brandLabel.getStyleClass().add("brand-text");

        Label customerPill = new Label("CUSTOMER PORTAL");
        customerPill.getStyleClass().add("badge-info");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Customer Profile Info
        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER);
        Label userLabel = new Label("Hello, " + customerUser.getFullName());
        userLabel.getStyleClass().add("text-subtitle");

        Button logoutBtn = new Button("Log Out");
        logoutBtn.getStyleClass().add("button-secondary");
        logoutBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 14px;");

        // ActionEvent: Logs out customer and returns to Login screen
        logoutBtn.setOnAction(e -> NavigationManager.logout());

        userBox.getChildren().addAll(userLabel, logoutBtn);
        row.getChildren().addAll(brandLabel, customerPill, spacer, userBox);

        header.getChildren().add(row);
        return header;
    }

    private VBox createWelcomeCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setMaxWidth(920);

        HBox titleRow = new HBox(14);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label welcomeTitle = new Label("Welcome to CineFlow, " + customerUser.getFullName() + "!");
        welcomeTitle.getStyleClass().add("title-large");

        Label roleBadge = new Label("ROLE: " + customerUser.getRole().name());
        roleBadge.getStyleClass().add("badge-info");

        Label activeBadge = new Label(customerUser.isActive() ? "ACTIVE" : "INACTIVE");
        activeBadge.getStyleClass().add(customerUser.isActive() ? "badge-success" : "badge-danger");

        titleRow.getChildren().addAll(welcomeTitle, roleBadge, activeBadge);

        Label desc = new Label("Authentication verified successfully. You can explore currently playing movies, pick upcoming showtimes, reserve seats in real-time, and view your tickets.");
        desc.getStyleClass().add("text-body");

        HBox detailsRow = new HBox(28);
        detailsRow.setStyle("-fx-background-color: #0d1525; -fx-padding: 12px 18px; -fx-background-radius: 8px; -fx-border-color: #1e293b; -fx-border-width: 1px; -fx-border-radius: 8px;");

        Label userHandle = new Label("Username: " + customerUser.getUsername());
        userHandle.getStyleClass().add("text-muted");

        Label emailLbl = new Label("Email: " + customerUser.getEmail());
        emailLbl.getStyleClass().add("text-muted");

        Label securityLbl = new Label("Password Protection: Salted SHA-256");
        securityLbl.getStyleClass().add("text-muted");

        detailsRow.getChildren().addAll(userHandle, emailLbl, securityLbl);

        card.getChildren().addAll(titleRow, desc, detailsRow);
        return card;
    }

    private GridPane createFeaturesGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setMaxWidth(920);

        grid.add(createFeatureCard("🎥 Browse Catalog & Live Search",
                "Explore currently active movies with instant search-as-you-type and genre/language filtering.",
                "Phase 4 Module"), 0, 0);

        grid.add(createFeatureCard("🎟 Real-time Visual Seat Map",
                "Select seats with interactive color coding (Green: available, Blue: selected, Red: booked, Gold: premium).",
                "Phase 4 Module"), 1, 0);

        grid.add(createFeatureCard("⏳ 5-Minute Seat Hold & Timeline",
                "Temporary hold protection preventing other customers from selecting your chosen seats during checkout.",
                "Phase 5 Module"), 0, 1);

        grid.add(createFeatureCard("💳 Simulated Payment & Tickets",
                "ACID booking transactions with SELECT ... FOR UPDATE, instant ticket summary and cancellation.",
                "Phase 5 & 6 Modules"), 1, 1);

        return grid;
    }

    private VBox createFeatureCard(String title, String desc, String badgeText) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(440);
        card.setMinHeight(120);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("title-small");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(badgeText);
        badge.getStyleClass().add("badge-info");

        top.getChildren().addAll(titleLbl, spacer, badge);

        Label descLbl = new Label(desc);
        descLbl.getStyleClass().add("text-body");
        descLbl.setWrapText(true);

        card.getChildren().addAll(top, descLbl);
        return card;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(12, 40, 12, 40));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: #0b111e; -fx-border-color: #1e293b; -fx-border-width: 1px 0 0 0;");

        Label footerLabel = new Label("CineFlow Customer Portal • Phase 2 Authentication Verified");
        footerLabel.getStyleClass().add("text-muted");
        footer.getChildren().add(footerLabel);
        return footer;
    }
}

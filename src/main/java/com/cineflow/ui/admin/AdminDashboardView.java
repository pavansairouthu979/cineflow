package com.cineflow.ui.admin;

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
 * Administrator Landing Dashboard view.
 * Displayed upon successful ADMIN authentication.
 */
public class AdminDashboardView {

    private final User adminUser;

    public AdminDashboardView(User adminUser) {
        this.adminUser = adminUser;
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

        // Administrative Modules Preview Cards (Upcoming in Phase 3 & 7)
        GridPane modulesGrid = createModulesGrid();
        centerContent.getChildren().add(modulesGrid);

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

        Label adminPill = new Label("ADMIN CONSOLE");
        adminPill.getStyleClass().add("badge-warning");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Admin User Profile Info
        HBox userBox = new HBox(8);
        userBox.setAlignment(Pos.CENTER);
        Label userLabel = new Label("Signed in as: " + adminUser.getFullName() + " (" + adminUser.getUsername() + ")");
        userLabel.getStyleClass().add("text-subtitle");

        Button logoutBtn = new Button("Log Out");
        logoutBtn.getStyleClass().add("button-secondary");
        logoutBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 14px;");

        // ActionEvent: Logs out admin and returns to Login screen
        logoutBtn.setOnAction(e -> NavigationManager.logout());

        userBox.getChildren().addAll(userLabel, logoutBtn);
        row.getChildren().addAll(brandLabel, adminPill, spacer, userBox);

        header.getChildren().add(row);
        return header;
    }

    private VBox createWelcomeCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setMaxWidth(920);

        HBox titleRow = new HBox(14);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label welcomeTitle = new Label("Welcome back, " + adminUser.getFullName() + "!");
        welcomeTitle.getStyleClass().add("title-large");

        Label roleBadge = new Label("ROLE: " + adminUser.getRole().name());
        roleBadge.getStyleClass().add("badge-warning");

        Label activeBadge = new Label(adminUser.isActive() ? "ACTIVE ACCOUNT" : "INACTIVE");
        activeBadge.getStyleClass().add(adminUser.isActive() ? "badge-success" : "badge-danger");

        titleRow.getChildren().addAll(welcomeTitle, roleBadge, activeBadge);

        Label desc = new Label("Authentication verified successfully. You have full administrator privileges to configure screens, manage movies and showtimes, supervise bookings, and view financial reports.");
        desc.getStyleClass().add("text-body");

        HBox detailsRow = new HBox(28);
        detailsRow.setStyle("-fx-background-color: #0d1525; -fx-padding: 12px 18px; -fx-background-radius: 8px; -fx-border-color: #1e293b; -fx-border-width: 1px; -fx-border-radius: 8px;");

        Label idLbl = new Label("Admin ID: " + adminUser.getId());
        idLbl.getStyleClass().add("text-muted");

        Label emailLbl = new Label("Email: " + adminUser.getEmail());
        emailLbl.getStyleClass().add("text-muted");

        Label hashInfo = new Label("Security: Salted SHA-256 Verified");
        hashInfo.getStyleClass().add("text-muted");

        detailsRow.getChildren().addAll(idLbl, emailLbl, hashInfo);

        card.getChildren().addAll(titleRow, desc, detailsRow);
        return card;
    }

    private GridPane createModulesGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setMaxWidth(920);

        grid.add(createModuleCard("🎬 Movie Catalog Management",
                "Add, edit, deactivate, and view movies with duration and ratings.",
                "Phase 3 Module"), 0, 0);

        grid.add(createModuleCard("🏛 Auditorium & Screen Layout",
                "Configure screen dimensions and automatically generate seating layouts.",
                "Phase 3 Module"), 1, 0);

        grid.add(createModuleCard("🕒 Showtime Scheduling",
                "Schedule movie screenings with conflict and overlap prevention logic.",
                "Phase 3 Module"), 0, 1);

        grid.add(createModuleCard("📊 Reports & Revenue Analytics",
                "Visual revenue BarCharts, movie ticket PieCharts, and CSV exports.",
                "Phase 7 Module"), 1, 1);

        return grid;
    }

    private VBox createModuleCard(String title, String desc, String badgeText) {
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

        Label footerLabel = new Label("CineFlow Administrator Console • Phase 2 Authentication Verified");
        footerLabel.getStyleClass().add("text-muted");
        footer.getChildren().add(footerLabel);
        return footer;
    }
}

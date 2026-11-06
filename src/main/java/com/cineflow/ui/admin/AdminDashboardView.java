package com.cineflow.ui.admin;

import com.cineflow.model.User;
import com.cineflow.ui.common.NavigationManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Full Administrator Console integrating Movie, Screen, Showtime,
 * Booking management, and Analytics Reports via TabPane.
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

        // Center TabPane hosting all administrative modules
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Tab 1: Overview
        Tab overviewTab = new Tab("Dashboard Overview", createOverviewContent(tabPane));

        // Tab 2: Movie Management
        MovieManagementView movieView = new MovieManagementView();
        Tab movieTab = new Tab("Movies", movieView.getView());

        // Tab 3: Screen Management
        ScreenManagementView screenView = new ScreenManagementView();
        Tab screenTab = new Tab("Screens & Seats", screenView.getView());

        // Tab 4: Showtime Management
        ShowtimeManagementView showtimeView = new ShowtimeManagementView();
        Tab showtimeTab = new Tab("Showtimes", showtimeView.getView());

        // Tab 5: Booking Management
        BookingManagementView bookingView = new BookingManagementView();
        Tab bookingTab = new Tab("All Bookings", bookingView.getView());

        // Tab 6: Reports & CSV Export
        ReportView reportView = new ReportView();
        Tab reportTab = new Tab("Reports & Analytics", reportView.getView());

        // Selection listener: Refresh tabs on select
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == movieTab) movieView.refreshTable();
            if (newTab == screenTab) screenView.refreshTable();
            if (newTab == showtimeTab) showtimeView.refreshTable();
            if (newTab == bookingTab) bookingView.applyFilters();
            if (newTab == reportTab) reportView.loadAllReportData();
        });

        tabPane.getTabs().addAll(overviewTab, movieTab, screenTab, showtimeTab, bookingTab, reportTab);
        root.setCenter(tabPane);

        // Footer
        HBox footer = createFooter();
        root.setBottom(footer);

        return root;
    }

    private VBox createTopNav() {
        VBox header = new VBox(6);
        header.setPadding(new Insets(14, 30, 14, 30));
        header.setStyle("-fx-background-color: #111a2e; -fx-border-color: #232d48; -fx-border-width: 0 0 1px 0;");

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        Label brandLabel = new Label("CINEFLOW");
        brandLabel.getStyleClass().add("brand-text");

        Label adminPill = new Label("ADMIN CONSOLE");
        adminPill.getStyleClass().add("badge-warning");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER);
        Label userLabel = new Label("Admin: " + adminUser.getFullName() + " (" + adminUser.getUsername() + ")");
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

    private Parent createOverviewContent(TabPane tabPane) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setAlignment(Pos.TOP_CENTER);

        VBox welcomeCard = new VBox(12);
        welcomeCard.getStyleClass().add("card");
        welcomeCard.setMaxWidth(920);

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label welcomeTitle = new Label("Welcome back, " + adminUser.getFullName() + "!");
        welcomeTitle.getStyleClass().add("title-large");
        Label roleBadge = new Label("ROLE: ADMIN");
        roleBadge.getStyleClass().add("badge-warning");
        titleRow.getChildren().addAll(welcomeTitle, roleBadge);

        Label desc = new Label("You have full administrative control over the multiplex. Manage movies, auditorium seating layouts, screening schedules, customer reservations, and inspect revenue reports.");
        desc.getStyleClass().add("text-body");
        welcomeCard.getChildren().addAll(titleRow, desc);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setMaxWidth(920);

        grid.add(createActionCard("🎬 Manage Movies", "Add, edit, search, and activate/deactivate catalog titles.", "Open Movies", () -> tabPane.getSelectionModel().select(1)), 0, 0);
        grid.add(createActionCard("🏛 Screens & Seats", "Configure auditoriums and auto-generate seat matrices.", "Open Screens", () -> tabPane.getSelectionModel().select(2)), 1, 0);
        grid.add(createActionCard("🕒 Showtime Scheduling", "Schedule shows with conflict and overlap prevention.", "Open Showtimes", () -> tabPane.getSelectionModel().select(3)), 0, 1);
        grid.add(createActionCard("📊 Reports & CSV Export", "View 7-day revenue, top movies, and download CSVs.", "Open Reports", () -> tabPane.getSelectionModel().select(5)), 1, 1);

        content.getChildren().addAll(welcomeCard, grid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    private VBox createActionCard(String title, String desc, String btnText, Runnable action) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(440);

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("title-small");

        Label descLbl = new Label(desc);
        descLbl.getStyleClass().add("text-body");

        Button actionBtn = new Button(btnText);
        actionBtn.getStyleClass().add("button-primary");
        // ActionEvent: Navigates to the selected management tab
        actionBtn.setOnAction(e -> action.run());

        card.getChildren().addAll(titleLbl, descLbl, actionBtn);
        return card;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 30, 10, 30));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: #0b111e; -fx-border-color: #1e293b; -fx-border-width: 1px 0 0 0;");

        Label label = new Label("CineFlow Administrator Console • Pure JDBC • MySQL 8.x");
        label.getStyleClass().add("text-muted");
        footer.getChildren().add(label);
        return footer;
    }
}

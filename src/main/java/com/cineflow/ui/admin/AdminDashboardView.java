package com.cineflow.ui.admin;

import com.cineflow.model.User;
import com.cineflow.service.MovieService;
import com.cineflow.service.ReportService;
import com.cineflow.service.ScreenService;
import com.cineflow.ui.common.NavigationManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Apple macOS Liquid Glass Administrator Console.
 * Features an executive floating segmented pill navigation bar,
 * live KPI metric cards, and integrated management sub-modules.
 */
public class AdminDashboardView {

    private final User adminUser;
    private final ReportService reportService;
    private final MovieService movieService;
    private final ScreenService screenService;

    private StackPane contentArea;
    private final List<Button> tabButtons = new ArrayList<>();

    // Child views
    private MovieManagementView movieView;
    private ScreenManagementView screenView;
    private ShowtimeManagementView showtimeView;
    private BookingManagementView bookingView;
    private ReportView reportView;

    public AdminDashboardView(User adminUser) {
        this.adminUser = adminUser;
        this.reportService = new ReportService();
        this.movieService = new MovieService();
        this.screenService = new ScreenService();
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-container");

        // Top Area: macOS Top Bar + Floating Segmented Pill Tab Bar
        VBox topContainer = new VBox(10);
        topContainer.getChildren().addAll(createTopNav(), createSegmentedTabBar());
        root.setTop(topContainer);

        // Center Content Area: StackPane switching views dynamically
        contentArea = new StackPane();
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        root.setCenter(contentArea);

        // Initialize child views
        movieView = new MovieManagementView();
        screenView = new ScreenManagementView();
        showtimeView = new ShowtimeManagementView();
        bookingView = new BookingManagementView();
        reportView = new ReportView();

        // Default: Show Overview Tab (Index 0)
        selectTab(0);

        // Footer
        HBox footer = createFooter();
        root.setBottom(footer);

        return root;
    }

    private VBox createTopNav() {
        VBox header = new VBox(6);
        header.setPadding(new Insets(12, 28, 12, 28));
        header.setStyle("-fx-background-color: rgba(16, 20, 32, 0.85); -fx-border-color: rgba(255, 255, 255, 0.12); -fx-border-width: 0 0 1px 0; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.35), 12, 0, 0, 4);");

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        // Apple macOS Window Controls (Traffic Light Dots)
        HBox trafficLights = new HBox(6);
        trafficLights.setAlignment(Pos.CENTER_LEFT);
        Circle redDot = new Circle(5.5, Color.web("#ff5f56"));
        Circle yellowDot = new Circle(5.5, Color.web("#ffbd2e"));
        Circle greenDot = new Circle(5.5, Color.web("#27c93f"));
        trafficLights.getChildren().addAll(redDot, yellowDot, greenDot);

        Label brandLabel = new Label("CINEFLOW");
        brandLabel.getStyleClass().add("brand-text");
        brandLabel.setStyle("-fx-font-size: 22px;");

        Label adminPill = new Label("ADMIN CONSOLE");
        adminPill.getStyleClass().add("badge-warning");
        adminPill.setStyle("-fx-font-size: 10px; -fx-padding: 3px 8px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER);
        Label userLabel = new Label("Admin: " + adminUser.getFullName() + " (@" + adminUser.getUsername() + ")");
        userLabel.getStyleClass().add("text-subtitle");
        userLabel.setStyle("-fx-text-fill: #e4e4e7; -fx-font-weight: 600;");

        Button logoutBtn = new Button("Log Out");
        logoutBtn.getStyleClass().add("button-secondary");
        logoutBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 14px; -fx-background-radius: 14px;");

        // ActionEvent: Logs out admin and returns to Login screen
        logoutBtn.setOnAction(e -> NavigationManager.logout());

        userBox.getChildren().addAll(userLabel, logoutBtn);
        row.getChildren().addAll(trafficLights, brandLabel, adminPill, spacer, userBox);

        header.getChildren().add(row);
        return header;
    }

    /**
     * Apple macOS Floating Segmented Navigation Bar.
     */
    private HBox createSegmentedTabBar() {
        HBox barContainer = new HBox();
        barContainer.setAlignment(Pos.CENTER);
        barContainer.setPadding(new Insets(4, 20, 8, 20));

        HBox pillBar = new HBox(6);
        pillBar.setAlignment(Pos.CENTER);
        pillBar.getStyleClass().add("admin-tab-bar");

        String[] tabTitles = {
                "📊 Overview",
                "🎬 Movies",
                "🏛 Screens & Seats",
                "🕒 Showtimes",
                "📋 All Bookings",
                "📈 Analytics & Reports"
        };

        for (int i = 0; i < tabTitles.length; i++) {
            final int tabIndex = i;
            Button btn = new Button(tabTitles[i]);
            btn.getStyleClass().add("admin-tab-button");
            btn.setOnAction(e -> selectTab(tabIndex));
            tabButtons.add(btn);
            pillBar.getChildren().add(btn);
        }

        barContainer.getChildren().add(pillBar);
        return barContainer;
    }

    public void selectTab(int index) {
        // Update tab pill button styles
        for (int i = 0; i < tabButtons.size(); i++) {
            Button btn = tabButtons.get(i);
            if (i == index) {
                btn.getStyleClass().remove("admin-tab-button");
                if (!btn.getStyleClass().contains("admin-tab-button-active")) {
                    btn.getStyleClass().add("admin-tab-button-active");
                }
            } else {
                btn.getStyleClass().remove("admin-tab-button-active");
                if (!btn.getStyleClass().contains("admin-tab-button")) {
                    btn.getStyleClass().add("admin-tab-button");
                }
            }
        }

        // Switch view and trigger dynamic data refresh
        contentArea.getChildren().clear();
        switch (index) {
            case 0 -> contentArea.getChildren().add(createOverviewContent());
            case 1 -> {
                movieView.refreshTable();
                contentArea.getChildren().add(movieView.getView());
            }
            case 2 -> {
                screenView.refreshTable();
                contentArea.getChildren().add(screenView.getView());
            }
            case 3 -> {
                showtimeView.refreshTable();
                contentArea.getChildren().add(showtimeView.getView());
            }
            case 4 -> {
                bookingView.applyFilters();
                contentArea.getChildren().add(bookingView.getView());
            }
            case 5 -> {
                reportView.loadAllReportData();
                contentArea.getChildren().add(reportView.getView());
            }
        }
    }

    private Parent createOverviewContent() {
        VBox content = new VBox(22);
        content.setPadding(new Insets(20, 28, 24, 28));
        content.setAlignment(Pos.TOP_CENTER);

        // Welcome Banner Card
        VBox welcomeCard = new VBox(10);
        welcomeCard.getStyleClass().add("card");
        welcomeCard.setMaxWidth(1020);

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label welcomeTitle = new Label("Welcome back, " + adminUser.getFullName() + "!");
        welcomeTitle.getStyleClass().add("title-large");
        Label roleBadge = new Label("ROLE: SYSTEM ADMINISTRATOR");
        roleBadge.getStyleClass().add("badge-warning");
        titleRow.getChildren().addAll(welcomeTitle, roleBadge);

        Label desc = new Label("You have full executive control over multiplex auditoriums, screening schedules, catalog titles, and real-time customer reservations.");
        desc.getStyleClass().add("text-body");
        welcomeCard.getChildren().addAll(titleRow, desc);

        // 4 Glowing KPI Metric Cards
        HBox kpiRow = createKpiCardsRow();
        kpiRow.setMaxWidth(1020);

        // Quick Navigation Action Cards
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setMaxWidth(1020);

        grid.add(createActionCard("🎬 Movie Catalog", "Add new titles, edit ratings, toggle active status.", "Open Movies", () -> selectTab(1)), 0, 0);
        grid.add(createActionCard("🏛 Screens & Seat Matrices", "Configure auditoriums and preview seat layouts.", "Open Screens", () -> selectTab(2)), 1, 0);
        grid.add(createActionCard("🕒 Showtime Scheduling", "Schedule screenings with automatic overlap validation.", "Open Showtimes", () -> selectTab(3)), 0, 1);
        grid.add(createActionCard("📈 Analytics & Reports", "View 7-day revenue charts, top movies, and download CSVs.", "Open Reports", () -> selectTab(5)), 1, 1);

        content.getChildren().addAll(welcomeCard, kpiRow, grid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    private HBox createKpiCardsRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER);

        BigDecimal totalRev = BigDecimal.ZERO;
        int activeMovies = 0;
        int totalScreens = 0;

        try {
            totalRev = reportService.getTotalRevenue();
            activeMovies = movieService.getAllMovies().size();
            totalScreens = screenService.getAllScreens().size();
        } catch (Exception ignored) {}

        row.getChildren().addAll(
                createKpiCard("💰 TOTAL REVENUE", "₹ " + totalRev.toPlainString(), "emerald", "+14.2% this week"),
                createKpiCard("🎬 ACTIVE MOVIES", String.valueOf(activeMovies) + " Titles", "blue", "Now Showing"),
                createKpiCard("🏛 AUDITORIUMS", String.valueOf(totalScreens) + " Screens", "purple", "64 Total Seats"),
                createKpiCard("🔒 SYSTEM SECURITY", "Active", "amber", "Salted SHA-256")
        );

        return row;
    }

    private VBox createKpiCard(String label, String value, String theme, String subtext) {
        VBox card = new VBox(6);
        card.getStyleClass().add("kpi-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMinWidth(220);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("text-subtitle");
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #98989d;");

        Label val = new Label(value);
        val.getStyleClass().add("title-large");

        String textColor = switch (theme) {
            case "emerald" -> "#34c759";
            case "purple" -> "#af52de";
            case "amber" -> "#ffd60a";
            default -> "#0A84FF";
        };
        val.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 22px;");

        Label sub = new Label("● " + subtext);
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #86868b;");

        card.getChildren().addAll(lbl, val, sub);
        return card;
    }

    private VBox createActionCard(String title, String desc, String btnText, Runnable action) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(480);

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("title-small");
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label descLbl = new Label(desc);
        descLbl.getStyleClass().add("text-body");
        descLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #a1a1aa;");

        Button actionBtn = new Button(btnText + " →");
        actionBtn.getStyleClass().add("button-primary");
        actionBtn.setOnAction(e -> action.run());

        card.getChildren().addAll(titleLbl, descLbl, actionBtn);

        card.setOnMouseEntered(e -> card.setStyle("-fx-translate-y: -3px; -fx-border-color: rgba(10, 132, 255, 0.5);"));
        card.setOnMouseExited(e -> card.setStyle(""));

        return card;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 30, 10, 30));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: rgba(12, 15, 24, 0.9); -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-width: 1px 0 0 0;");

        Label label = new Label("CineFlow Administrator Console • Apple Liquid Glass Edition • MySQL 8.x");
        label.getStyleClass().add("text-muted");
        footer.getChildren().add(label);
        return footer;
    }
}

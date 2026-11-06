package com.cineflow.ui.admin;

import com.cineflow.model.User;
import com.cineflow.service.ReportService;
import com.cineflow.ui.common.NavigationManager;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * CineFlow — Premium Admin Console.
 * Cinematic navigation bar, animated KPI stat cards, and full management TabPane.
 */
public class AdminDashboardView {

    private final User adminUser;

    public AdminDashboardView(User adminUser) {
        this.adminUser = adminUser;
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-container");

        // Top navigation
        VBox topNav = createTopNav();
        root.setTop(topNav);

        // Center TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("card");

        Tab overviewTab  = new Tab("  Dashboard  ", createOverviewContent(tabPane));
        Tab movieTab     = new Tab("  Movies  ");
        Tab screenTab    = new Tab("  Screens & Seats  ");
        Tab showtimeTab  = new Tab("  Showtimes  ");
        Tab bookingTab   = new Tab("  All Bookings  ");
        Tab reportTab    = new Tab("  Analytics  ");

        MovieManagementView movieView       = new MovieManagementView();
        ScreenManagementView screenView     = new ScreenManagementView();
        ShowtimeManagementView showtimeView = new ShowtimeManagementView();
        BookingManagementView bookingView   = new BookingManagementView();
        ReportView reportView               = new ReportView();

        movieTab.setContent(movieView.getView());
        screenTab.setContent(screenView.getView());
        showtimeTab.setContent(showtimeView.getView());
        bookingTab.setContent(bookingView.getView());
        reportTab.setContent(reportView.getView());

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == movieTab)    movieView.refreshTable();
            if (newTab == screenTab)   screenView.refreshTable();
            if (newTab == showtimeTab) showtimeView.refreshTable();
            if (newTab == bookingTab)  bookingView.applyFilters();
            if (newTab == reportTab)   reportView.loadAllReportData();
        });

        tabPane.getTabs().addAll(overviewTab, movieTab, screenTab, showtimeTab, bookingTab, reportTab);
        root.setCenter(tabPane);

        // Footer
        root.setBottom(createFooter());
        return root;
    }

    /* ---------------------------------------------------------------- */
    /* TOP NAV                                                            */
    /* ---------------------------------------------------------------- */
    private VBox createTopNav() {
        VBox header = new VBox();
        header.getStyleClass().add("nav-bar");
        header.setPadding(new Insets(0, 28, 0, 28));

        HBox row = new HBox(18);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 0, 14, 0));

        // macOS traffic lights
        HBox trafficLights = new HBox(7);
        trafficLights.setAlignment(Pos.CENTER_LEFT);
        Circle redDot    = new Circle(5.5, Color.web("#FF5F56"));
        Circle yellowDot = new Circle(5.5, Color.web("#FFBD2E"));
        Circle greenDot  = new Circle(5.5, Color.web("#27C93F"));
        trafficLights.getChildren().addAll(redDot, yellowDot, greenDot);

        Label brandLabel = new Label("CINEFLOW");
        brandLabel.getStyleClass().add("brand-text");
        brandLabel.setStyle("-fx-font-size: 22px;");

        Label adminPill = new Label("ADMIN CONSOLE");
        adminPill.getStyleClass().add("badge-warning");
        adminPill.setStyle("-fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Admin user info
        HBox userBox = new HBox(12);
        userBox.setAlignment(Pos.CENTER);

        Circle avatarBg = new Circle(17);
        avatarBg.setFill(Color.web("#F59E0B", 0.22));
        avatarBg.setStroke(Color.web("#F59E0B", 0.50));
        avatarBg.setStrokeWidth(1.5);
        Label avatarInit = new Label(adminUser.getFullName().substring(0, 1).toUpperCase());
        avatarInit.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: #FCD34D;");
        StackPane avatar = new StackPane(avatarBg, avatarInit);
        avatar.setMaxSize(34, 34);

        VBox userMeta = new VBox(1);
        Label nameLbl = new Label(adminUser.getFullName());
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #E2E8F0;");
        Label roleLbl = new Label("@" + adminUser.getUsername() + "  •  ADMIN");
        roleLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        userMeta.getChildren().addAll(nameLbl, roleLbl);

        Button logoutBtn = new Button("Log Out");
        logoutBtn.getStyleClass().add("button-secondary");
        logoutBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 14px;");
        logoutBtn.setOnAction(e -> NavigationManager.logout());

        userBox.getChildren().addAll(avatar, userMeta, logoutBtn);
        row.getChildren().addAll(trafficLights, brandLabel, adminPill, spacer, userBox);
        header.getChildren().add(row);
        return header;
    }

    /* ---------------------------------------------------------------- */
    /* OVERVIEW CONTENT                                                   */
    /* ---------------------------------------------------------------- */
    private Parent createOverviewContent(TabPane tabPane) {
        VBox content = new VBox(28);
        content.setPadding(new Insets(28));

        // Welcome banner
        VBox welcomeBanner = createWelcomeBanner();
        content.getChildren().add(welcomeBanner);

        // KPI stat cards row
        HBox statsRow = createKpiStatsRow();
        content.getChildren().add(statsRow);

        // Quick action cards
        Label quickActionsTitle = new Label("Quick Actions");
        quickActionsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #F8FAFC;");
        content.getChildren().add(quickActionsTitle);

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setMaxWidth(Double.MAX_VALUE);

        grid.add(createActionCard("🎬", "Movie Catalog",
                "Add, edit, search and manage movie titles with genre, language and rating details.",
                "Manage Movies", () -> tabPane.getSelectionModel().select(1)), 0, 0);

        grid.add(createActionCard("🏛", "Screens & Seating",
                "Configure multiplex auditoriums and auto-generate seat row matrices.",
                "Manage Screens", () -> tabPane.getSelectionModel().select(2)), 1, 0);

        grid.add(createActionCard("🕒", "Showtime Scheduling",
                "Schedule screenings with real-time overlap detection and conflict prevention.",
                "Manage Showtimes", () -> tabPane.getSelectionModel().select(3)), 0, 1);

        grid.add(createActionCard("📋", "Booking Records",
                "View and filter all customer bookings with date, movie, and status filters.",
                "View Bookings", () -> tabPane.getSelectionModel().select(4)), 1, 1);

        grid.add(createActionCard("📊", "Analytics & Reports",
                "Revenue charts, top movie breakdowns, occupancy rates, and CSV export.",
                "Open Analytics", () -> tabPane.getSelectionModel().select(5)), 0, 2);

        grid.getColumnConstraints().addAll(
                constraintPercent(50),
                constraintPercent(50)
        );

        content.getChildren().add(grid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        return scroll;
    }

    private ColumnConstraints constraintPercent(double pct) {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(pct);
        return cc;
    }

    /* ---------------------------------------------------------------- */
    /* WELCOME BANNER                                                     */
    /* ---------------------------------------------------------------- */
    private VBox createWelcomeBanner() {
        VBox banner = new VBox(8);
        banner.setStyle(
            "-fx-background-color: linear-gradient(to right, rgba(139,92,246,0.18), rgba(0,194,255,0.08), transparent);" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(139,92,246,0.30);" +
            "-fx-border-radius: 16px; -fx-border-width: 1px;" +
            "-fx-padding: 22px 28px;"
        );

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label welcomeTitle = new Label("Welcome back, " + adminUser.getFullName() + "!");
        welcomeTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #F8FAFC;");

        Label roleBadge = new Label("ADMIN");
        roleBadge.getStyleClass().add("badge-warning");
        roleBadge.setStyle("-fx-font-size: 12px;");

        titleRow.getChildren().addAll(welcomeTitle, roleBadge);

        Label desc = new Label(
            "Full administrative control — manage the movie catalog, configure auditorium seating, " +
            "schedule screenings, review customer reservations, and analyze revenue performance."
        );
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #A1A1AA; -fx-line-spacing: 2px;");
        desc.setWrapText(true);

        banner.getChildren().addAll(titleRow, desc);

        // Animate entrance
        banner.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), banner);
        ft.setToValue(1.0);
        ft.setDelay(Duration.millis(100));
        ft.play();

        return banner;
    }

    /* ---------------------------------------------------------------- */
    /* KPI STAT CARDS                                                     */
    /* ---------------------------------------------------------------- */
    private HBox createKpiStatsRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        // Try to load real stats
        ReportService reportService = new ReportService();
        String totalRevenue = "—";
        String todayBookings = "—";

        try {
            BigDecimal rev = reportService.getTotalRevenue();
            totalRevenue = "₹ " + rev.toPlainString();
            int todayCount = reportService.getTodayBookingsCount();
            todayBookings = String.valueOf(todayCount);
        } catch (SQLException ignored) {}

        VBox revCard = createStatCard("Total Revenue", totalRevenue, "#22C55E", "💰");
        VBox todayCard = createStatCard("Today's Bookings", todayBookings, "#00C2FF", "🎟");
        VBox moviesCard = createStatCard("Active Platform", "CineFlow", "#8B5CF6", "🎬");
        VBox roleCard = createStatCard("Session Role", "Administrator", "#F59E0B", "🔐");

        HBox.setHgrow(revCard, Priority.ALWAYS);
        HBox.setHgrow(todayCard, Priority.ALWAYS);
        HBox.setHgrow(moviesCard, Priority.ALWAYS);
        HBox.setHgrow(roleCard, Priority.ALWAYS);

        row.getChildren().addAll(revCard, todayCard, moviesCard, roleCard);

        // Stagger entrance
        List<VBox> cards = Arrays.asList(revCard, todayCard, moviesCard, roleCard);
        for (int i = 0; i < cards.size(); i++) {
            VBox card = cards.get(i);
            card.setOpacity(0);
            card.setTranslateY(14);
            FadeTransition ft = new FadeTransition(Duration.millis(450), card);
            ft.setToValue(1.0);
            ft.setDelay(Duration.millis(200 + i * 80L));
            ft.play();
            TranslateTransition tt = new TranslateTransition(Duration.millis(450), card);
            tt.setToY(0);
            tt.setDelay(Duration.millis(200 + i * 80L));
            tt.setInterpolator(Interpolator.EASE_OUT);
            tt.play();
        }

        return row;
    }

    private VBox createStatCard(String label, String value, String accentColor, String icon) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 22px;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: " + accentColor + ";" +
            "-fx-effect: dropshadow(gaussian, " + accentColor + "44, 8, 0, 0, 0);"
        );

        Label labelLbl = new Label(label.toUpperCase());
        labelLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #6B7280; -fx-letter-spacing: 0.5px;");

        card.getChildren().addAll(iconLbl, valueLbl, labelLbl);
        return card;
    }

    /* ---------------------------------------------------------------- */
    /* ACTION CARD                                                        */
    /* ---------------------------------------------------------------- */
    private VBox createActionCard(String icon, String title, String desc, String btnText, Runnable action) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 20px;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #F8FAFC;");
        titleRow.getChildren().addAll(iconLbl, titleLbl);

        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #A1A1AA; -fx-line-spacing: 2px;");
        descLbl.setWrapText(true);

        Button btn = new Button(btnText + "  →");
        btn.getStyleClass().add("button-primary");
        btn.setStyle("-fx-font-size: 13px; -fx-padding: 9px 18px;");
        btn.setOnAction(e -> action.run());

        card.getChildren().addAll(titleRow, descLbl, btn);

        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: rgba(21,19,35,0.95);" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(139,92,246,0.60);" +
            "-fx-border-width: 1px; -fx-border-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.35), 22, 0, 0, 8);" +
            "-fx-padding: 20px; -fx-translate-y: -2px;"
        ));
        card.setOnMouseExited(e -> card.setStyle(""));

        return card;
    }

    /* ---------------------------------------------------------------- */
    /* FOOTER                                                             */
    /* ---------------------------------------------------------------- */
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 30, 10, 30));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle(
            "-fx-background-color: rgba(9,9,15,0.92);" +
            "-fx-border-color: rgba(139,92,246,0.10);" +
            "-fx-border-width: 1px 0 0 0;"
        );
        Label label = new Label("CineFlow Admin Console  ✦  JavaFX 21  ✦  MySQL 8.x  ✦  Pure JDBC");
        label.getStyleClass().add("text-muted");
        footer.getChildren().add(label);
        return footer;
    }
}

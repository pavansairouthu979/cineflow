package com.cineflow.ui.customer;

import com.cineflow.model.Booking;
import com.cineflow.model.Movie;
import com.cineflow.model.Showtime;
import com.cineflow.model.User;
import com.cineflow.ui.common.NavigationManager;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.util.Duration;

/**
 * CineFlow — Premium Customer Dashboard.
 *
 * Enhancements:
 *  - Cinematic sticky navigation bar
 *  - Clickable avatar → profile popup (name, email, username, role, logout)
 *  - Smooth fade-in transitions between views
 *  - "← Home" link back to landing page
 */
public class CustomerDashboardView {

    private final User customerUser;
    private BorderPane root;
    private StackPane contentArea;

    private MovieCatalogView catalogView;
    private CustomerBookingsView bookingsView;

    private Button browseBtn;
    private Button myBookingsBtn;

    public CustomerDashboardView(User customerUser) {
        this.customerUser = customerUser;
    }

    public Parent getView() {
        root = new BorderPane();
        root.getStyleClass().add("app-container");

        root.setTop(createTopNav());

        contentArea = new StackPane();
        root.setCenter(contentArea);

        catalogView  = new MovieCatalogView(this::showShowtimeSelection);
        bookingsView = new CustomerBookingsView(this::showTicketSummary);

        showCatalog();

        root.setBottom(createFooter());
        return root;
    }

    /* ================================================================ */
    /* TOP NAVIGATION BAR                                                 */
    /* ================================================================ */
    private VBox createTopNav() {
        VBox header = new VBox();
        header.getStyleClass().add("nav-bar");
        header.setPadding(new Insets(0, 28, 0, 28));

        HBox row = new HBox(18);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 0, 13, 0));

        // macOS traffic lights
        HBox dots = new HBox(7);
        dots.setAlignment(Pos.CENTER_LEFT);
        dots.getChildren().addAll(
                new Circle(5.5, Color.web("#FF5F56")),
                new Circle(5.5, Color.web("#FFBD2E")),
                new Circle(5.5, Color.web("#27C93F"))
        );

        // Home link
        Button homeBtn = new Button("← Home");
        homeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #6B7280; -fx-font-size: 12px;" +
            "-fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 4px 6px;"
        );
        homeBtn.setOnAction(e -> NavigationManager.logout()); // logout clears session + lands on landing
        homeBtn.setOnMouseEntered(e -> homeBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #C4B5FD;" +
            "-fx-font-size: 12px; -fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 4px 6px;"
        ));
        homeBtn.setOnMouseExited(e -> homeBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #6B7280;" +
            "-fx-font-size: 12px; -fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 4px 6px;"
        ));

        // Vertical sep
        Region vsep0 = vSep();

        // Brand
        Label brandLabel = new Label("CINEFLOW");
        brandLabel.setStyle(
            "-fx-font-size: 21px; -fx-font-weight: 900;" +
            "-fx-text-fill: linear-gradient(to right, #8B5CF6, #00C2FF);" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.45), 12, 0, 0, 1);"
        );

        Label portalPill = new Label("CINEMA");
        portalPill.setStyle(
            "-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #8B5CF6;" +
            "-fx-background-color: rgba(139,92,246,0.12);" +
            "-fx-border-color: rgba(139,92,246,0.30);" +
            "-fx-border-width: 1px; -fx-border-radius: 20px; -fx-background-radius: 20px;" +
            "-fx-padding: 2px 9px;"
        );

        Region vsep1 = vSep();

        // Nav buttons
        browseBtn = navBtn("🎬  Browse Movies", true);
        browseBtn.setOnAction(e -> showCatalog());

        myBookingsBtn = navBtn("🎟  My Bookings", false);
        myBookingsBtn.setOnAction(e -> showMyBookings());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Profile avatar — clickable → popup
        HBox avatarArea = createAvatarArea();

        row.getChildren().addAll(
                dots, homeBtn, vsep0, brandLabel, portalPill,
                vsep1, browseBtn, myBookingsBtn,
                spacer, avatarArea
        );
        header.getChildren().add(row);
        return header;
    }

    private Region vSep() {
        Region r = new Region();
        r.setStyle("-fx-border-color: rgba(139,92,246,0.20); -fx-border-width: 0 1px 0 0;");
        r.setMinWidth(1);
        r.setMinHeight(26);
        return r;
    }

    private Button navBtn(String text, boolean primary) {
        Button b = new Button(text);
        if (primary) {
            b.setStyle(
                "-fx-background-color: linear-gradient(to right, #8B5CF6, #6D28D9);" +
                "-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 13px;" +
                "-fx-background-radius: 9px; -fx-padding: 7px 16px; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.40), 10, 0, 0, 2);"
            );
        } else {
            b.setStyle(
                "-fx-background-color: rgba(139,92,246,0.08);" +
                "-fx-text-fill: #C4B5FD; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-border-color: rgba(139,92,246,0.28);" +
                "-fx-border-width: 1px; -fx-background-radius: 9px; -fx-border-radius: 9px;" +
                "-fx-padding: 7px 16px; -fx-cursor: hand;"
            );
        }
        return b;
    }

    /* ================================================================ */
    /* AVATAR + PROFILE POPUP                                             */
    /* ================================================================ */
    private HBox createAvatarArea() {
        HBox area = new HBox(10);
        area.setAlignment(Pos.CENTER);

        // Avatar circle with initial
        StackPane avatar = buildAvatar();
        avatar.setStyle("-fx-cursor: hand;");

        // Name + username
        VBox nameMeta = new VBox(1);
        Label nameLbl = new Label(customerUser.getFullName());
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #E2E8F0;");
        Label userLbl = new Label("@" + customerUser.getUsername());
        userLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        nameMeta.getChildren().addAll(nameLbl, userLbl);
        nameMeta.setStyle("-fx-cursor: hand;");

        Label chevron = new Label("⌄");
        chevron.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280; -fx-cursor: hand;");

        // Popup
        Popup popup = buildProfilePopup(avatar);

        // Toggle popup on click of avatar area
        area.setOnMouseClicked(e -> {
            if (popup.isShowing()) {
                popup.hide();
            } else {
                // Position below avatar
                javafx.geometry.Bounds bounds = area.localToScreen(area.getBoundsInLocal());
                popup.show(NavigationManager.getPrimaryStage(),
                        bounds.getMaxX() - 240,
                        bounds.getMaxY() + 6);
            }
        });

        // Highlight on hover
        area.setOnMouseEntered(e -> nameLbl.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #C4B5FD;"
        ));
        area.setOnMouseExited(e -> nameLbl.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #E2E8F0;"
        ));

        area.getChildren().addAll(avatar, nameMeta, chevron);
        return area;
    }

    private StackPane buildAvatar() {
        String initial = customerUser.getFullName() != null && !customerUser.getFullName().isBlank()
                ? customerUser.getFullName().substring(0, 1).toUpperCase() : "?";

        Circle bg = new Circle(18);
        bg.setFill(Color.web("#8B5CF6", 0.22));
        bg.setStroke(Color.web("#8B5CF6", 0.60));
        bg.setStrokeWidth(2);

        Label initLbl = new Label(initial);
        initLbl.setStyle(
            "-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: #C4B5FD;"
        );

        StackPane sp = new StackPane(bg, initLbl);
        sp.setMaxSize(36, 36);
        return sp;
    }

    private Popup buildProfilePopup(StackPane avatarAnchor) {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);

        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: rgba(18,15,30,0.98);" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(139,92,246,0.45);" +
            "-fx-border-radius: 16px; -fx-border-width: 1px;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.35), 28, 0, 0, 10);"
        );
        card.setPrefWidth(240);

        // ── Profile header ────────────────────────────────────────────
        VBox profileHeader = new VBox(6);
        profileHeader.setPadding(new Insets(18, 18, 14, 18));
        profileHeader.setStyle(
            "-fx-background-color: rgba(139,92,246,0.10);" +
            "-fx-background-radius: 16px 16px 0 0;"
        );

        HBox avatarRow = new HBox(12);
        avatarRow.setAlignment(Pos.CENTER_LEFT);

        // Large avatar in popup
        StackPane bigAvatar = new StackPane();
        Circle bigBg = new Circle(24);
        bigBg.setFill(Color.web("#8B5CF6", 0.28));
        bigBg.setStroke(Color.web("#8B5CF6", 0.70));
        bigBg.setStrokeWidth(2.5);
        String initial = customerUser.getFullName() != null && !customerUser.getFullName().isBlank()
                ? customerUser.getFullName().substring(0, 1).toUpperCase() : "?";
        Label bigInit = new Label(initial);
        bigInit.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #C4B5FD;");
        bigAvatar.getChildren().addAll(bigBg, bigInit);
        bigAvatar.setMaxSize(48, 48);

        VBox nameBlock = new VBox(3);
        Label fullNameLbl = new Label(customerUser.getFullName());
        fullNameLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #F8FAFC;");

        Label roleLbl = new Label("Customer Account");
        roleLbl.setStyle(
            "-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #8B5CF6;" +
            "-fx-background-color: rgba(139,92,246,0.15);" +
            "-fx-border-color: rgba(139,92,246,0.35); -fx-border-width: 1px;" +
            "-fx-border-radius: 20px; -fx-background-radius: 20px; -fx-padding: 2px 8px;"
        );
        nameBlock.getChildren().addAll(fullNameLbl, roleLbl);
        avatarRow.getChildren().addAll(bigAvatar, nameBlock);
        profileHeader.getChildren().add(avatarRow);

        // ── Divider ───────────────────────────────────────────────────
        Region div1 = new Region();
        div1.setMinHeight(1);
        div1.setStyle("-fx-background-color: rgba(139,92,246,0.20);");

        // ── Info rows ─────────────────────────────────────────────────
        VBox infoSection = new VBox(0);
        infoSection.setPadding(new Insets(8, 0, 8, 0));

        infoSection.getChildren().addAll(
                profileInfoRow("✉", "Email",    customerUser.getEmail()    != null ? customerUser.getEmail()    : "—"),
                profileInfoRow("@", "Username", customerUser.getUsername() != null ? customerUser.getUsername() : "—"),
                profileInfoRow("🆔", "User ID",  "#" + customerUser.getId())
        );

        // ── Divider ───────────────────────────────────────────────────
        Region div2 = new Region();
        div2.setMinHeight(1);
        div2.setStyle("-fx-background-color: rgba(139,92,246,0.15);");

        // ── Action buttons ────────────────────────────────────────────
        VBox actionsSection = new VBox(2);
        actionsSection.setPadding(new Insets(8, 10, 12, 10));

        Button myBookingsAction = popupActionBtn("🎟  My Bookings", false);
        myBookingsAction.setOnAction(e -> { popup.hide(); showMyBookings(); });

        Button browsAction = popupActionBtn("🎬  Browse Movies", false);
        browsAction.setOnAction(e -> { popup.hide(); showCatalog(); });

        // Divider inside actions
        Region div3 = new Region();
        div3.setMinHeight(1);
        div3.setStyle("-fx-background-color: rgba(139,92,246,0.12); -fx-margin: 4px 0;");

        Button logoutAction = popupActionBtn("⏻  Sign Out", true);
        logoutAction.setOnAction(e -> { popup.hide(); NavigationManager.logout(); });

        actionsSection.getChildren().addAll(myBookingsAction, browsAction, div3, logoutAction);

        card.getChildren().addAll(profileHeader, div1, infoSection, div2, actionsSection);

        // Entrance animation for popup
        card.setOpacity(0);
        card.setTranslateY(-8);
        popup.getContent().add(card);

        popup.setOnShowing(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(180), card);
            ft.setToValue(1.0);
            ft.play();
            TranslateTransition tt = new TranslateTransition(Duration.millis(180), card);
            tt.setToY(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            tt.play();
        });
        popup.setOnHiding(e -> {
            card.setOpacity(0);
            card.setTranslateY(-8);
        });

        return popup;
    }

    private HBox profileInfoRow(String icon, String key, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 18, 9, 18));
        row.setStyle("-fx-cursor: default;");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 14px; -fx-min-width: 20px;");

        VBox textBlock = new VBox(1);
        Label keyLbl = new Label(key);
        keyLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #52525B;");
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #E2E8F0;");
        textBlock.getChildren().addAll(keyLbl, valLbl);

        row.getChildren().addAll(iconLbl, textBlock);

        row.setOnMouseEntered(e -> row.setStyle(
            "-fx-background-color: rgba(139,92,246,0.08); -fx-cursor: default;"
        ));
        row.setOnMouseExited(e -> row.setStyle("-fx-cursor: default;"));
        return row;
    }

    private Button popupActionBtn(String text, boolean danger) {
        Button btn = new Button(text);
        String baseStyle =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + (danger ? "#EF4444" : "#CBD5E1") + ";" +
            "-fx-font-size: 13px; -fx-font-weight: 600;" +
            "-fx-background-radius: 9px;" +
            "-fx-padding: 9px 14px; -fx-cursor: hand;" +
            "-fx-alignment: center-left;" +
            "-fx-max-width: Infinity;";
        String hoverStyle =
            "-fx-background-color: " + (danger ? "rgba(239,68,68,0.10)" : "rgba(139,92,246,0.10)") + ";" +
            "-fx-text-fill: " + (danger ? "#F87171" : "#DDD6FE") + ";" +
            "-fx-font-size: 13px; -fx-font-weight: 600;" +
            "-fx-background-radius: 9px;" +
            "-fx-padding: 9px 14px; -fx-cursor: hand;" +
            "-fx-alignment: center-left;" +
            "-fx-max-width: Infinity;";
        btn.setStyle(baseStyle);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        return btn;
    }

    /* ================================================================ */
    /* VIEW SWITCHERS                                                     */
    /* ================================================================ */
    public void showCatalog() {
        switchContent(catalogView.getView());
        setNavActive(browseBtn, myBookingsBtn);
    }

    public void showMyBookings() {
        switchContent(bookingsView.getView());
        setNavActive(myBookingsBtn, browseBtn);
    }

    public void showShowtimeSelection(Movie movie) {
        ShowtimeSelectionView stView = new ShowtimeSelectionView(
                movie, this::showSeatMap, this::showCatalog);
        switchContent(stView.getView());
    }

    public void showSeatMap(Showtime showtime) {
        SeatMapView seatMapView = new SeatMapView(
                showtime,
                seats -> new PaymentDialog(showtime, seats, this::showTicketSummary).show(),
                () -> {
                    Movie m = new Movie();
                    m.setId(showtime.getMovieId());
                    m.setTitle(showtime.getMovieTitle());
                    showShowtimeSelection(m);
                }
        );
        switchContent(seatMapView.getView());
    }

    public void showTicketSummary(Booking booking) {
        switchContent(new TicketSummaryView(booking, this::showCatalog, this::showMyBookings).getView());
    }

    /* ================================================================ */
    /* HELPERS                                                            */
    /* ================================================================ */
    private void switchContent(Parent newContent) {
        contentArea.getChildren().clear();
        newContent.setOpacity(0);
        contentArea.getChildren().add(newContent);

        FadeTransition ft = new FadeTransition(Duration.millis(260), newContent);
        ft.setToValue(1.0);
        ft.setInterpolator(Interpolator.EASE_IN);
        ft.play();
    }

    private void setNavActive(Button active, Button inactive) {
        // Active: purple gradient
        active.setStyle(
            "-fx-background-color: linear-gradient(to right, #8B5CF6, #6D28D9);" +
            "-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 13px;" +
            "-fx-background-radius: 9px; -fx-padding: 7px 16px; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.40), 10, 0, 0, 2);"
        );
        // Inactive: ghost
        inactive.setStyle(
            "-fx-background-color: rgba(139,92,246,0.08);" +
            "-fx-text-fill: #C4B5FD; -fx-font-weight: 600; -fx-font-size: 13px;" +
            "-fx-border-color: rgba(139,92,246,0.28);" +
            "-fx-border-width: 1px; -fx-background-radius: 9px; -fx-border-radius: 9px;" +
            "-fx-padding: 7px 16px; -fx-cursor: hand;"
        );
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 30, 10, 30));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle(
            "-fx-background-color: rgba(9,9,15,0.95);" +
            "-fx-border-color: rgba(139,92,246,0.10);" +
            "-fx-border-width: 1px 0 0 0;"
        );
        Label label = new Label(
            "CineFlow  ✦  Premium Cinema Platform  ✦  JavaFX 21  ✦  MySQL 8.x"
        );
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #374151;");
        footer.getChildren().add(label);
        return footer;
    }
}

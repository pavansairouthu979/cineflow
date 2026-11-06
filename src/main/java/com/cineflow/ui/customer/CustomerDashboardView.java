package com.cineflow.ui.customer;

import com.cineflow.model.Booking;
import com.cineflow.model.Movie;
import com.cineflow.model.Showtime;
import com.cineflow.model.User;
import com.cineflow.ui.common.NavigationManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

/**
 * Main Customer Dashboard coordinator styled with Apple Liquid Glass aesthetic,
 * macOS navigation bar, and seamless customer booking lifecycle.
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

        // Top Navigation Header (Apple macOS Liquid Glass Topbar)
        VBox topNav = createTopNav();
        root.setTop(topNav);

        // Center Content Container
        contentArea = new StackPane();
        root.setCenter(contentArea);

        // Initialize child views
        catalogView = new MovieCatalogView(this::showShowtimeSelection);
        bookingsView = new CustomerBookingsView(this::showTicketSummary);

        // Start by displaying the Movie Catalog
        showCatalog();

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
        Circle redDot = new Circle(5.5, javafx.scene.paint.Color.web("#ff5f56"));
        Circle yellowDot = new Circle(5.5, javafx.scene.paint.Color.web("#ffbd2e"));
        Circle greenDot = new Circle(5.5, javafx.scene.paint.Color.web("#27c93f"));
        trafficLights.getChildren().addAll(redDot, yellowDot, greenDot);

        Label brandLabel = new Label("CINEFLOW");
        brandLabel.getStyleClass().add("brand-text");
        brandLabel.setStyle("-fx-font-size: 22px;");

        Label customerPill = new Label("CINEMA PORTAL");
        customerPill.getStyleClass().add("badge-info");
        customerPill.setStyle("-fx-font-size: 10px; -fx-padding: 3px 8px;");

        // Navigation Tabs / Buttons
        browseBtn = new Button("🎬 Browse Movies");
        browseBtn.getStyleClass().add("button-primary");
        browseBtn.setOnAction(e -> showCatalog());

        myBookingsBtn = new Button("🎟 My Bookings");
        myBookingsBtn.getStyleClass().add("button-secondary");
        myBookingsBtn.setOnAction(e -> showMyBookings());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox userBox = new HBox(12);
        userBox.setAlignment(Pos.CENTER);

        Label userLabel = new Label("👋 " + customerUser.getFullName());
        userLabel.getStyleClass().add("text-subtitle");
        userLabel.setStyle("-fx-text-fill: #e4e4e7; -fx-font-weight: 600;");

        Button logoutBtn = new Button("Log Out");
        logoutBtn.getStyleClass().add("button-secondary");
        logoutBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 14px; -fx-background-radius: 14px;");
        logoutBtn.setOnAction(e -> NavigationManager.logout());

        userBox.getChildren().addAll(userLabel, logoutBtn);
        row.getChildren().addAll(trafficLights, brandLabel, customerPill, browseBtn, myBookingsBtn, spacer, userBox);

        header.getChildren().add(row);
        return header;
    }

    public void showCatalog() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(catalogView.getView());
        if (browseBtn != null && myBookingsBtn != null) {
            browseBtn.getStyleClass().remove("button-secondary");
            if (!browseBtn.getStyleClass().contains("button-primary")) browseBtn.getStyleClass().add("button-primary");
            myBookingsBtn.getStyleClass().remove("button-primary");
            if (!myBookingsBtn.getStyleClass().contains("button-secondary")) myBookingsBtn.getStyleClass().add("button-secondary");
        }
    }

    public void showMyBookings() {
        contentArea.getChildren().clear();
        bookingsView.refreshBookings();
        contentArea.getChildren().add(bookingsView.getView());
        if (browseBtn != null && myBookingsBtn != null) {
            myBookingsBtn.getStyleClass().remove("button-secondary");
            if (!myBookingsBtn.getStyleClass().contains("button-primary")) myBookingsBtn.getStyleClass().add("button-primary");
            browseBtn.getStyleClass().remove("button-primary");
            if (!browseBtn.getStyleClass().contains("button-secondary")) browseBtn.getStyleClass().add("button-secondary");
        }
    }

    public void showShowtimeSelection(Movie movie) {
        ShowtimeSelectionView stView = new ShowtimeSelectionView(
                movie,
                this::showSeatMap,
                this::showCatalog
        );
        contentArea.getChildren().clear();
        contentArea.getChildren().add(stView.getView());
    }

    public void showSeatMap(Showtime showtime) {
        SeatMapView seatMapView = new SeatMapView(
                showtime,
                seats -> {
                    // Open Apple Liquid Glass Payment Dialog
                    PaymentDialog paymentDialog = new PaymentDialog(
                            showtime,
                            seats,
                            this::showTicketSummary
                    );
                    paymentDialog.show();
                },
                () -> {
                    Movie m = new Movie();
                    m.setId(showtime.getMovieId());
                    m.setTitle(showtime.getMovieTitle());
                    showShowtimeSelection(m);
                }
        );
        contentArea.getChildren().clear();
        contentArea.getChildren().add(seatMapView.getView());
    }

    public void showTicketSummary(Booking booking) {
        TicketSummaryView ticketView = new TicketSummaryView(
                booking,
                this::showCatalog,
                this::showMyBookings
        );
        contentArea.getChildren().clear();
        contentArea.getChildren().add(ticketView.getView());
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 30, 10, 30));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: rgba(12, 15, 24, 0.9); -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-width: 1px 0 0 0;");

        Label label = new Label("CineFlow • Apple Liquid Glass Edition • Powered by Pure JavaFX & MySQL");
        label.getStyleClass().add("text-muted");
        footer.getChildren().add(label);
        return footer;
    }
}

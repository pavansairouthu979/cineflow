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

/**
 * Main Customer Dashboard coordinator handling movie browsing,
 * showtime selection, seat map visualization, checkout, and bookings.
 */
public class CustomerDashboardView {

    private final User customerUser;
    private BorderPane root;
    private StackPane contentArea;

    private MovieCatalogView catalogView;
    private CustomerBookingsView bookingsView;

    public CustomerDashboardView(User customerUser) {
        this.customerUser = customerUser;
    }

    public Parent getView() {
        root = new BorderPane();
        root.getStyleClass().add("app-container");

        // Top Navigation Header
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
        header.setPadding(new Insets(14, 30, 14, 30));
        header.setStyle("-fx-background-color: #111a2e; -fx-border-color: #232d48; -fx-border-width: 0 0 1px 0;");

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        Label brandLabel = new Label("CINEFLOW");
        brandLabel.getStyleClass().add("brand-text");

        Label customerPill = new Label("CUSTOMER PORTAL");
        customerPill.getStyleClass().add("badge-info");

        // Navigation Tabs / Buttons
        Button browseBtn = new Button("🎬 Browse Movies");
        browseBtn.getStyleClass().add("button-secondary");
        // ActionEvent: Navigates to movie catalog
        browseBtn.setOnAction(e -> showCatalog());

        Button myBookingsBtn = new Button("🎟 My Bookings");
        myBookingsBtn.getStyleClass().add("button-secondary");
        // ActionEvent: Navigates to customer's booking history
        myBookingsBtn.setOnAction(e -> showMyBookings());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

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
        row.getChildren().addAll(brandLabel, customerPill, browseBtn, myBookingsBtn, spacer, userBox);

        header.getChildren().add(row);
        return header;
    }

    public void showCatalog() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(catalogView.getView());
    }

    public void showMyBookings() {
        contentArea.getChildren().clear();
        bookingsView.refreshBookings();
        contentArea.getChildren().add(bookingsView.getView());
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
                    // Open modal Payment Dialog
                    PaymentDialog paymentDialog = new PaymentDialog(
                            showtime,
                            seats,
                            this::showTicketSummary
                    );
                    paymentDialog.show();
                },
                () -> {
                    // Back to showtime selection for this movie
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
        footer.setStyle("-fx-background-color: #0b111e; -fx-border-color: #1e293b; -fx-border-width: 1px 0 0 0;");

        Label label = new Label("CineFlow Multiplex • Pure JavaFX & MySQL • Live Real-Time Ticketing");
        label.getStyleClass().add("text-muted");
        footer.getChildren().add(label);
        return footer;
    }
}

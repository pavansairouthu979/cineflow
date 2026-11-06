package com.cineflow.ui.customer;

import com.cineflow.model.Booking;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Visual cinema ticket confirmation view displayed after a successful booking transaction.
 */
public class TicketSummaryView {

    private final Booking booking;
    private final Runnable onBookAnother;
    private final Runnable onViewBookings;

    public TicketSummaryView(Booking booking, Runnable onBookAnother, Runnable onViewBookings) {
        this.booking = booking;
        this.onBookAnother = onBookAnother;
        this.onViewBookings = onViewBookings;
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-container");

        VBox centerBox = new VBox(24);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(30));

        // Success Icon & Title
        Label checkIcon = new Label("🎉 RESERVATION CONFIRMED!");
        checkIcon.getStyleClass().add("brand-text");
        checkIcon.setStyle("-fx-font-size: 24px;");

        // Cinema Ticket Card
        VBox ticketCard = createTicketCard();
        centerBox.getChildren().addAll(checkIcon, ticketCard);

        // Action Buttons Row
        HBox actionsRow = new HBox(16);
        actionsRow.setAlignment(Pos.CENTER);

        Button bookAnotherBtn = new Button("Book Another Movie");
        bookAnotherBtn.getStyleClass().add("button-secondary");
        // ActionEvent: Returns to the catalog
        bookAnotherBtn.setOnAction(e -> onBookAnother.run());

        Button myBookingsBtn = new Button("View My Bookings →");
        myBookingsBtn.getStyleClass().add("button-primary");
        // ActionEvent: Navigates to customer's booking list
        myBookingsBtn.setOnAction(e -> onViewBookings.run());

        actionsRow.getChildren().addAll(bookAnotherBtn, myBookingsBtn);
        centerBox.getChildren().add(actionsRow);

        root.setCenter(centerBox);
        return root;
    }

    private VBox createTicketCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setMaxWidth(520);
        card.setPadding(new Insets(26));
        card.setStyle("-fx-background-color: #151d30; -fx-border-color: #6366f1; -fx-border-width: 2px; -fx-border-radius: 14px; -fx-background-radius: 14px;");

        // Top Banner
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label cinemaBrand = new Label("CINEFLOW MULTIPLEX");
        cinemaBrand.getStyleClass().add("title-medium");
        cinemaBrand.setStyle("-fx-text-fill: #818cf8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label ticketPill = new Label("BOOKING #" + booking.getId());
        ticketPill.getStyleClass().add("badge-success");

        topRow.getChildren().addAll(cinemaBrand, spacer, ticketPill);

        // Divider
        Region div1 = new Region();
        div1.setStyle("-fx-border-color: #2a3556; -fx-border-width: 0 0 1px 0; -fx-min-height: 1px;");

        // Ticket Details Grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 10, 0));

        addRow(grid, 0, "Movie Title:", booking.getMovieTitle());
        addRow(grid, 1, "Auditorium:", booking.getScreenName());
        addRow(grid, 2, "Showtime:", booking.getFormattedShowtimeStart());
        addRow(grid, 3, "Customer Name:", booking.getCustomerName() + " (@" + booking.getUsername() + ")");
        addRow(grid, 4, "Reserved Seats:", booking.getSeatNumbers());
        addRow(grid, 5, "Total Amount Paid:", "₹ " + booking.getTotalAmount().toPlainString());
        addRow(grid, 6, "Payment Method:", booking.getPaymentMethod() != null ? booking.getPaymentMethod().name() : "PAID");
        addRow(grid, 7, "Booking Status:", booking.getStatus().name());

        // Barcode decorative line
        Label barcode = new Label("||| | ||||| || |||||| | |||| ||||| || ||| |||||| | ||||");
        barcode.setStyle("-fx-font-family: monospace; -fx-font-size: 16px; -fx-text-fill: #64748b; -fx-letter-spacing: 3px;");
        barcode.setMaxWidth(Double.MAX_VALUE);
        barcode.setAlignment(Pos.CENTER);

        card.getChildren().addAll(topRow, div1, grid, barcode);
        return card;
    }

    private void addRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("text-subtitle");
        lbl.setStyle("-fx-font-weight: bold;");

        Label val = new Label(value != null ? value : "N/A");
        val.getStyleClass().add("text-body");
        val.setWrapText(true);

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }
}

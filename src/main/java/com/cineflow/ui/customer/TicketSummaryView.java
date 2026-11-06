package com.cineflow.ui.customer;

import com.cineflow.model.Booking;
import com.cineflow.util.PosterHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

/**
 * Apple Wallet / Liquid Glass Ticket Confirmation Pass view displayed after a successful booking.
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

        VBox centerBox = new VBox(22);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(24));

        // Success Icon & Title
        Label checkIcon = new Label("🎉 RESERVATION CONFIRMED");
        checkIcon.getStyleClass().add("brand-text");
        checkIcon.setStyle("-fx-font-size: 24px;");

        // Cinema Ticket Pass Card (Apple Wallet Glass Style)
        VBox ticketCard = createTicketCard();
        centerBox.getChildren().addAll(checkIcon, ticketCard);

        // Action Buttons Row
        HBox actionsRow = new HBox(16);
        actionsRow.setAlignment(Pos.CENTER);

        Button bookAnotherBtn = new Button("Book Another Movie");
        bookAnotherBtn.getStyleClass().add("button-secondary");
        bookAnotherBtn.setOnAction(e -> onBookAnother.run());

        Button myBookingsBtn = new Button("View My Bookings →");
        myBookingsBtn.getStyleClass().add("button-primary");
        myBookingsBtn.setOnAction(e -> onViewBookings.run());

        actionsRow.getChildren().addAll(bookAnotherBtn, myBookingsBtn);
        centerBox.getChildren().add(actionsRow);

        root.setCenter(centerBox);
        return root;
    }

    private VBox createTicketCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setMaxWidth(560);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: rgba(22, 28, 48, 0.88); -fx-border-color: rgba(10, 132, 255, 0.5); -fx-border-width: 1.5px; -fx-border-radius: 18px; -fx-background-radius: 18px; -fx-effect: dropshadow(gaussian, rgba(10, 132, 255, 0.25), 24, 0, 0, 8);");

        // Top Banner
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label cinemaBrand = new Label("CINEFLOW DIGITAL PASS");
        cinemaBrand.getStyleClass().add("title-medium");
        cinemaBrand.setStyle("-fx-text-fill: #0A84FF; -fx-font-size: 16px; -fx-font-weight: 800; -fx-letter-spacing: 1px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label ticketPill = new Label("BOOKING #" + booking.getId());
        ticketPill.getStyleClass().add("badge-success");
        ticketPill.setStyle("-fx-font-size: 12px;");

        topRow.getChildren().addAll(cinemaBrand, spacer, ticketPill);

        // Specular Divider
        Region div1 = new Region();
        div1.setStyle("-fx-border-color: rgba(255, 255, 255, 0.12); -fx-border-width: 0 0 1px 0; -fx-min-height: 1px;");

        // Pass Body: Poster on Left, Details on Right
        HBox bodyRow = new HBox(16);
        bodyRow.setAlignment(Pos.CENTER_LEFT);

        ImageView poster = PosterHelper.createPosterImageView(booking.getMovieTitle(), 110, 155, 12);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(8);
        HBox.setHgrow(grid, Priority.ALWAYS);

        addRow(grid, 0, "Movie:", booking.getMovieTitle());
        addRow(grid, 1, "Screen:", booking.getScreenName());
        addRow(grid, 2, "Showtime:", booking.getFormattedShowtimeStart());
        addRow(grid, 3, "Customer:", booking.getCustomerName() + " (@" + booking.getUsername() + ")");
        addRow(grid, 4, "Seats:", booking.getSeatNumbers());
        addRow(grid, 5, "Total Paid:", "₹ " + (booking.getTotalAmount() != null ? booking.getTotalAmount().toPlainString() : "0.00"));
        addRow(grid, 6, "Payment:", booking.getPaymentMethod() != null ? booking.getPaymentMethod().name() : "PAID");
        addRow(grid, 7, "Status:", booking.getStatus() != null ? booking.getStatus().name() : "CONFIRMED");

        bodyRow.getChildren().addAll(poster, grid);

        // Perforated Divider
        Region div2 = new Region();
        div2.setStyle("-fx-border-color: rgba(255, 255, 255, 0.12); -fx-border-style: dashed; -fx-border-width: 1px 0 0 0; -fx-min-height: 1px;");

        // Apple Barcode & Scan Notice
        VBox barcodeBox = new VBox(4);
        barcodeBox.setAlignment(Pos.CENTER);

        Label barcode = new Label("||| | ||||| || |||||| | |||| ||||| || ||| |||||| | |||| |||");
        barcode.setStyle("-fx-font-family: monospace; -fx-font-size: 16px; -fx-text-fill: #98989d; -fx-letter-spacing: 3px;");

        Label scanNotice = new Label("Scan this digital pass at the auditorium entrance gate");
        scanNotice.getStyleClass().add("text-muted");
        scanNotice.setStyle("-fx-font-size: 11px;");

        barcodeBox.getChildren().addAll(barcode, scanNotice);

        card.getChildren().addAll(topRow, div1, bodyRow, div2, barcodeBox);
        return card;
    }

    private void addRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("text-subtitle");
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Label val = new Label(value != null ? value : "N/A");
        val.getStyleClass().add("text-body");
        val.setStyle("-fx-font-size: 13px;");
        val.setWrapText(true);

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }
}

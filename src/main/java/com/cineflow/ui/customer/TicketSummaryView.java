package com.cineflow.ui.customer;

import com.cineflow.model.Booking;
import com.cineflow.util.PosterHelper;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * CineFlow — Premium Digital Cinema Ticket Pass.
 * Animated confirmation with Apple Wallet-style holographic ticket card.
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

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        VBox centerBox = new VBox(24);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(40, 40, 40, 40));

        // Success header
        VBox successHeader = createSuccessHeader();
        centerBox.getChildren().add(successHeader);

        // Premium ticket card
        VBox ticketCard = createTicketCard();
        centerBox.getChildren().add(ticketCard);

        // Action buttons
        HBox actionsRow = createActionsRow();
        centerBox.getChildren().add(actionsRow);

        scroll.setContent(centerBox);
        root.setCenter(scroll);

        // Entrance animation
        centerBox.setOpacity(0);
        centerBox.setTranslateY(24);
        FadeTransition ft = new FadeTransition(Duration.millis(550), centerBox);
        ft.setToValue(1.0);
        ft.play();
        TranslateTransition tt = new TranslateTransition(Duration.millis(550), centerBox);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.play();

        return root;
    }

    /* ---------------------------------------------------------------- */
    /* SUCCESS HEADER                                                     */
    /* ---------------------------------------------------------------- */
    private VBox createSuccessHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);

        // Animated success circle
        StackPane checkCircle = new StackPane();
        Circle bg = new Circle(32);
        bg.setFill(Color.web("#22C55E", 0.18));
        bg.setStroke(Color.web("#22C55E", 0.55));
        bg.setStrokeWidth(2);
        Label checkMark = new Label("✓");
        checkMark.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #22C55E;");
        checkCircle.getChildren().addAll(bg, checkMark);
        checkCircle.setMaxSize(64, 64);

        ScaleTransition pulse = new ScaleTransition(Duration.millis(600), checkCircle);
        pulse.setFromX(0.6);
        pulse.setToX(1.0);
        pulse.setFromY(0.6);
        pulse.setToY(1.0);
        pulse.setInterpolator(Interpolator.EASE_OUT);
        pulse.play();

        Label title = new Label("Booking Confirmed!");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #F8FAFC;");

        Label subtitle = new Label("Your seats are reserved. Show this pass at the cinema entrance.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #A1A1AA;");

        header.getChildren().addAll(checkCircle, title, subtitle);
        return header;
    }

    /* ---------------------------------------------------------------- */
    /* TICKET CARD                                                        */
    /* ---------------------------------------------------------------- */
    private VBox createTicketCard() {
        VBox card = new VBox(0);
        card.setMaxWidth(600);
        card.setMinWidth(550);
        card.getStyleClass().add("ticket-card");

        // Top strip — CineFlow branding
        HBox topStrip = new HBox(12);
        topStrip.setAlignment(Pos.CENTER_LEFT);
        topStrip.setPadding(new Insets(0, 0, 18, 0));

        Label brand = new Label("CINEFLOW DIGITAL PASS");
        brand.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: 900; -fx-text-fill: #8B5CF6;" +
            "-fx-letter-spacing: 1.5px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label bookingId = new Label("# " + booking.getId());
        bookingId.getStyleClass().add("badge-success");
        bookingId.setStyle("-fx-font-size: 13px; -fx-font-weight: 900;");

        topStrip.getChildren().addAll(brand, spacer, bookingId);

        // Divider
        Region div1 = makeDivider(false);

        // Body — poster + details
        HBox body = new HBox(22);
        body.setAlignment(Pos.TOP_LEFT);
        body.setPadding(new Insets(18, 0, 18, 0));

        // Poster
        ImageView poster = PosterHelper.createPosterImageView(booking.getMovieTitle(), 115, 160, 12);

        // Details grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        HBox.setHgrow(grid, Priority.ALWAYS);

        int r = 0;
        addRow(grid, r++, "🎬  Movie",     safeStr(booking.getMovieTitle()));
        addRow(grid, r++, "🏛  Screen",    safeStr(booking.getScreenName()));
        addRow(grid, r++, "🕒  Showtime",  safeStr(booking.getFormattedShowtimeStart()));
        addRow(grid, r++, "👤  Customer",  buildCustomerDisplay());
        addRow(grid, r++, "🎟  Seats",     safeStr(booking.getSeatNumbers()));

        // Total paid in accent green
        Label totalKeyLbl = new Label("💳  Total Paid");
        totalKeyLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #6B7280;");
        Label totalValLbl = new Label("₹  " + (booking.getTotalAmount() != null ? booking.getTotalAmount().toPlainString() : "0.00"));
        totalValLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: #22C55E;");
        grid.add(totalKeyLbl, 0, r);
        grid.add(totalValLbl, 1, r++);

        addRow(grid, r++, "💰  Payment",
                booking.getPaymentMethod() != null ? booking.getPaymentMethod().name() : "—");

        // Booking time (when available)
        String bookedOn = booking.getFormattedBookingTime();
        if (bookedOn != null && !bookedOn.isBlank()) {
            addRow(grid, r++, "📅  Booked On", bookedOn);
        }

        // Status badge
        Label statusKey = new Label("✅  Status");
        statusKey.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #6B7280;");
        Label statusVal = new Label(booking.getStatus() != null ? booking.getStatus().name() : "CONFIRMED");
        statusVal.getStyleClass().add("badge-success");
        statusVal.setStyle("-fx-font-size: 13px;");
        grid.add(statusKey, 0, r);
        grid.add(statusVal, 1, r);

        body.getChildren().addAll(poster, grid);

        // Perforated divider
        Region div2 = makeDivider(true);

        // Barcode
        VBox barcodeBox = new VBox(6);
        barcodeBox.setAlignment(Pos.CENTER);
        barcodeBox.setPadding(new Insets(14, 0, 0, 0));

        Label barcode = new Label("| ||| | |||| ||| || ||||| | ||||| || ||| | |||| |||| | ||| |||||");
        barcode.setStyle(
            "-fx-font-family: monospace; -fx-font-size: 18px;" +
            "-fx-text-fill: rgba(139,92,246,0.50); -fx-letter-spacing: 2px;"
        );

        Label scanNotice = new Label("Present this pass at the auditorium entrance  •  Booking #" + booking.getId());
        scanNotice.setStyle("-fx-font-size: 11px; -fx-text-fill: #4B5563; -fx-letter-spacing: 0.5px;");

        barcodeBox.getChildren().addAll(barcode, scanNotice);

        card.getChildren().addAll(topStrip, div1, body, div2, barcodeBox);
        return card;
    }

    /** Returns value or "N/A" if null/blank. */
    private String safeStr(String value) {
        return (value != null && !value.isBlank()) ? value : "N/A";
    }

    /** Formats customer display name including username if available. */
    private String buildCustomerDisplay() {
        String name = booking.getCustomerName();
        String uname = booking.getUsername();
        if (name != null && !name.isBlank() && uname != null && !uname.isBlank()) {
            return name + "  (@" + uname + ")";
        } else if (name != null && !name.isBlank()) {
            return name;
        } else if (uname != null && !uname.isBlank()) {
            return "@" + uname;
        }
        return "N/A";
    }

    private Region makeDivider(boolean dashed) {
        Region div = new Region();
        String dashStyle = dashed ? "-fx-border-style: dashed;" : "";
        div.setStyle(
            "-fx-border-color: rgba(139,92,246,0.22);" +
            dashStyle +
            "-fx-border-width: 1px 0 0 0; -fx-min-height: 1px;"
        );
        return div;
    }

    private void addRow(GridPane grid, int row, String key, String value) {
        Label keyLbl = new Label(key);
        keyLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #6B7280;");

        Label valLbl = new Label(safeStr(value));
        valLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #E2E8F0;");
        valLbl.setWrapText(true);
        valLbl.setMaxWidth(280);

        grid.add(keyLbl, 0, row);
        grid.add(valLbl, 1, row);
    }

    /* ---------------------------------------------------------------- */
    /* ACTION BUTTONS                                                     */
    /* ---------------------------------------------------------------- */
    private HBox createActionsRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(8, 0, 0, 0));

        Button bookAnotherBtn = new Button("Browse More Movies");
        bookAnotherBtn.getStyleClass().add("button-secondary");
        bookAnotherBtn.setStyle("-fx-font-size: 14px; -fx-padding: 11px 24px;");
        bookAnotherBtn.setOnAction(e -> onBookAnother.run());

        Button myBookingsBtn = new Button("View My Bookings  →");
        myBookingsBtn.getStyleClass().add("button-primary");
        myBookingsBtn.setStyle("-fx-font-size: 14px; -fx-padding: 11px 24px;");
        myBookingsBtn.setOnAction(e -> onViewBookings.run());

        row.getChildren().addAll(bookAnotherBtn, myBookingsBtn);
        return row;
    }
}

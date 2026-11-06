package com.cineflow.ui.admin;

import com.cineflow.model.Booking;
import com.cineflow.model.OccupancyReportItem;
import com.cineflow.service.BookingService;
import com.cineflow.service.ReportService;
import com.cineflow.util.AlertHelper;
import com.cineflow.util.CsvExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * CineFlow — Premium Analytics & Reports View.
 * KPI cards, 7-day revenue BarChart, top movies PieChart,
 * occupancy TableView, and CSV export actions.
 */
public class ReportView {

    private final ReportService reportService;
    private final BookingService bookingService;

    private Label totalRevLabel;
    private Label todayBookingsLabel;
    private BarChart<String, Number> barChart;
    private PieChart pieChart;
    private TableView<OccupancyReportItem> occupancyTable;

    public ReportView() {
        this.reportService = new ReportService();
        this.bookingService = new BookingService();
    }

    public Parent getView() {
        VBox root = new VBox(0);

        // Section header
        HBox sectionHeader = createSectionHeader();
        root.getChildren().add(sectionHeader);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox inner = new VBox(22);
        inner.setPadding(new Insets(22, 26, 26, 26));

        // KPI metric cards + export row
        HBox topRow = createMetricsAndExportRow();
        inner.getChildren().add(topRow);

        // Charts row
        HBox chartsRow = createChartsRow();
        inner.getChildren().add(chartsRow);

        // Occupancy table
        VBox occupancyBox = createOccupancySection();
        inner.getChildren().add(occupancyBox);

        scroll.setContent(inner);
        root.getChildren().add(scroll);

        loadAllReportData();
        return root;
    }

    /* ---------------------------------------------------------------- */
    /* SECTION HEADER                                                     */
    /* ---------------------------------------------------------------- */
    private HBox createSectionHeader() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(24, 28, 16, 28));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, rgba(139,92,246,0.10), transparent);" +
            "-fx-border-color: rgba(139,92,246,0.12);" +
            "-fx-border-width: 0 0 1px 0;"
        );

        Label title = new Label("Analytics & Reports");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #F8FAFC;");

        Label dotSep = new Label("✦");
        dotSep.setStyle("-fx-text-fill: #8B5CF6;");

        Label subtitle = new Label("Revenue, ticket sales, and occupancy performance");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #A1A1AA;");

        header.getChildren().addAll(title, dotSep, subtitle);
        return header;
    }

    /* ---------------------------------------------------------------- */
    /* KPI CARDS + EXPORT                                                 */
    /* ---------------------------------------------------------------- */
    private HBox createMetricsAndExportRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        // Revenue card
        VBox revCard = new VBox(6);
        revCard.getStyleClass().add("stat-card");
        revCard.setPrefWidth(210);
        Label revIcon = new Label("💰");
        revIcon.setStyle("-fx-font-size: 20px;");
        totalRevLabel = new Label("₹  —");
        totalRevLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #22C55E;");
        Label revLabel = new Label("TOTAL REVENUE");
        revLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #6B7280;");
        revCard.getChildren().addAll(revIcon, totalRevLabel, revLabel);

        // Today bookings card
        VBox todayCard = new VBox(6);
        todayCard.getStyleClass().add("stat-card");
        todayCard.setPrefWidth(210);
        Label todayIcon = new Label("🎟");
        todayIcon.setStyle("-fx-font-size: 20px;");
        todayBookingsLabel = new Label("—");
        todayBookingsLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #00C2FF;");
        Label todayLabel = new Label("TODAY'S BOOKINGS");
        todayLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #6B7280;");
        todayCard.getChildren().addAll(todayIcon, todayBookingsLabel, todayLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Export panel
        VBox exportBox = new VBox(8);
        exportBox.setAlignment(Pos.CENTER_RIGHT);

        Label exportTitle = new Label("Export Reports  (RFC-4180 CSV)");
        exportTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #6B7280;");

        HBox btns = new HBox(8);
        btns.setAlignment(Pos.CENTER_RIGHT);

        Button bBtn = createExportBtn("Bookings",  "📋", e -> exportBookings());
        Button rBtn = createExportBtn("Revenue",   "💰", e -> exportRevenue());
        Button sBtn = createExportBtn("Ticket Sales","🎬", e -> exportMovieSales());
        Button oBtn = createExportBtn("Occupancy", "🏛", e -> exportOccupancy());

        btns.getChildren().addAll(bBtn, rBtn, sBtn, oBtn);
        exportBox.getChildren().addAll(exportTitle, btns);

        row.getChildren().addAll(revCard, todayCard, spacer, exportBox);
        return row;
    }

    private Button createExportBtn(String label, String icon, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(icon + "  " + label);
        btn.getStyleClass().add("button-secondary");
        btn.setStyle("-fx-font-size: 12px; -fx-padding: 7px 14px;");
        btn.setOnAction(handler);
        return btn;
    }

    /* ---------------------------------------------------------------- */
    /* CHARTS                                                             */
    /* ---------------------------------------------------------------- */
    private HBox createChartsRow() {
        HBox row = new HBox(18);
        row.setAlignment(Pos.CENTER);

        // Bar chart — 7-Day Revenue
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue (₹)");

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("7-Day Revenue Trend");
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(310);
        barChart.setPrefWidth(490);
        barChart.getStyleClass().add("card");
        HBox.setHgrow(barChart, Priority.ALWAYS);

        // Pie chart — Top Movies
        pieChart = new PieChart();
        pieChart.setTitle("Top Movies by Tickets Sold");
        pieChart.setPrefHeight(310);
        pieChart.setPrefWidth(450);
        pieChart.setLegendVisible(true);
        pieChart.getStyleClass().add("card");

        row.getChildren().addAll(barChart, pieChart);
        return row;
    }

    /* ---------------------------------------------------------------- */
    /* OCCUPANCY TABLE                                                    */
    /* ---------------------------------------------------------------- */
    private VBox createOccupancySection() {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(18));

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Showtime Occupancy Analysis");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: #F8FAFC;");

        Label info = new Label("Capacity = total_rows × seats_per_row per auditorium");
        info.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");

        titleRow.getChildren().addAll(title, info);

        occupancyTable = new TableView<>();
        occupancyTable.setPrefHeight(250);
        occupancyTable.setStyle("-fx-background-color: transparent;");

        TableColumn<OccupancyReportItem, Integer> idCol = new TableColumn<>("Show ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getShowtimeId()).asObject());
        idCol.setPrefWidth(70);

        TableColumn<OccupancyReportItem, String> movieCol = new TableColumn<>("Movie");
        movieCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMovieTitle()));
        movieCol.setPrefWidth(200);

        TableColumn<OccupancyReportItem, String> screenCol = new TableColumn<>("Auditorium");
        screenCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getScreenName()));
        screenCol.setPrefWidth(160);

        TableColumn<OccupancyReportItem, String> timeCol = new TableColumn<>("Showtime");
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedStartTime()));
        timeCol.setPrefWidth(150);

        TableColumn<OccupancyReportItem, Integer> bookedCol = new TableColumn<>("Booked");
        bookedCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getBookedSeats()).asObject());
        bookedCol.setPrefWidth(80);

        TableColumn<OccupancyReportItem, Integer> capCol = new TableColumn<>("Capacity");
        capCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTotalSeats()).asObject());
        capCol.setPrefWidth(80);

        TableColumn<OccupancyReportItem, String> occCol = new TableColumn<>("Occupancy %");
        occCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedOccupancy()));
        occCol.setPrefWidth(120);
        occCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }

                Label badge = new Label(item);
                try {
                    double pct = Double.parseDouble(item.replace("%","").trim());
                    if (pct >= 75) badge.getStyleClass().add("badge-success");
                    else if (pct >= 40) badge.getStyleClass().add("badge-warning");
                    else badge.getStyleClass().add("badge-info");
                } catch (NumberFormatException ex) {
                    badge.getStyleClass().add("badge-info");
                }
                badge.setStyle("-fx-font-size: 11px;");
                setGraphic(badge);
                setText(null);
            }
        });

        occupancyTable.getColumns().addAll(idCol, movieCol, screenCol, timeCol, bookedCol, capCol, occCol);
        box.getChildren().addAll(titleRow, occupancyTable);
        return box;
    }

    /* ---------------------------------------------------------------- */
    /* DATA LOADING                                                       */
    /* ---------------------------------------------------------------- */
    public void loadAllReportData() {
        try {
            // KPI values
            BigDecimal totalRev = reportService.getTotalRevenue();
            totalRevLabel.setText("₹  " + totalRev.toPlainString());

            int todayCount = reportService.getTodayBookingsCount();
            todayBookingsLabel.setText(String.valueOf(todayCount));

            // 7-Day Revenue BarChart
            Map<LocalDate, BigDecimal> weeklyRev = reportService.getSevenDayRevenue();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
            for (Map.Entry<LocalDate, BigDecimal> entry : weeklyRev.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey().format(fmt), entry.getValue()));
            }
            barChart.getData().clear();
            barChart.getData().add(series);

            // Top Movies PieChart
            Map<String, Integer> movieSales = reportService.getTopMoviesByTicketSales();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : movieSales.entrySet()) {
                pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
            pieChart.setData(pieData);

            // Occupancy Table
            List<OccupancyReportItem> occupancyItems = reportService.getShowtimeOccupancy();
            occupancyTable.setItems(FXCollections.observableArrayList(occupancyItems));

        } catch (SQLException e) {
            AlertHelper.showError("Analytics Error", "Failed to Load Reports", e.getMessage());
        }
    }

    /* ---------------------------------------------------------------- */
    /* CSV EXPORT ACTIONS                                                 */
    /* ---------------------------------------------------------------- */
    private void exportBookings() {
        File file = chooseSaveFile("cineflow_bookings.csv");
        if (file == null) return;
        try {
            List<Booking> list = bookingService.getFilteredAdminBookings(null, null, null, null, null);
            CsvExporter.exportBookingsToCsv(file, list);
            AlertHelper.showInfo("Export Complete", "CSV Saved", file.getAbsolutePath());
        } catch (IOException | SQLException e) {
            AlertHelper.showError("Export Failed", "Error Writing CSV", e.getMessage());
        }
    }

    private void exportRevenue() {
        File file = chooseSaveFile("cineflow_revenue.csv");
        if (file == null) return;
        try {
            Map<LocalDate, BigDecimal> daily = reportService.getSevenDayRevenue();
            BigDecimal total = reportService.getTotalRevenue();
            CsvExporter.exportRevenueToCsv(file, daily, total);
            AlertHelper.showInfo("Export Complete", "CSV Saved", file.getAbsolutePath());
        } catch (IOException | SQLException e) {
            AlertHelper.showError("Export Failed", "Error Writing CSV", e.getMessage());
        }
    }

    private void exportMovieSales() {
        File file = chooseSaveFile("cineflow_ticket_sales.csv");
        if (file == null) return;
        try {
            Map<String, Integer> sales = reportService.getTopMoviesByTicketSales();
            CsvExporter.exportMovieSalesToCsv(file, sales);
            AlertHelper.showInfo("Export Complete", "CSV Saved", file.getAbsolutePath());
        } catch (IOException | SQLException e) {
            AlertHelper.showError("Export Failed", "Error Writing CSV", e.getMessage());
        }
    }

    private void exportOccupancy() {
        File file = chooseSaveFile("cineflow_occupancy.csv");
        if (file == null) return;
        try {
            List<OccupancyReportItem> list = reportService.getShowtimeOccupancy();
            CsvExporter.exportOccupancyToCsv(file, list);
            AlertHelper.showInfo("Export Complete", "CSV Saved", file.getAbsolutePath());
        } catch (IOException | SQLException e) {
            AlertHelper.showError("Export Failed", "Error Writing CSV", e.getMessage());
        }
    }

    private File chooseSaveFile(String defaultName) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save CSV Report");
        chooser.setInitialFileName(defaultName);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        return chooser.showSaveDialog(null);
    }
}

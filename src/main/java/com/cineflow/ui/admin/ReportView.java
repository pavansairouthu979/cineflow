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
 * Admin view providing analytical reports, JavaFX BarChart, JavaFX PieChart,
 * Showtime occupancy TableView, and RFC-4180 CSV Export actions.
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
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));

        // Top Summary Metric Cards & CSV Export Actions
        HBox topRow = createMetricsAndExportRow();
        root.getChildren().add(topRow);

        // Charts Row: BarChart & PieChart side by side
        HBox chartsRow = createChartsRow();
        root.getChildren().add(chartsRow);

        // Occupancy Table
        VBox occupancyBox = createOccupancyBox();
        VBox.setVgrow(occupancyBox, Priority.ALWAYS);
        root.getChildren().add(occupancyBox);

        // Initial Data Load
        loadAllReportData();

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    private HBox createMetricsAndExportRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        // Card 1: Total Revenue
        VBox revCard = new VBox(4);
        revCard.getStyleClass().add("card");
        revCard.setPrefWidth(220);
        Label revTitle = new Label("TOTAL REVENUE (PAID)");
        revTitle.getStyleClass().add("text-subtitle");
        totalRevLabel = new Label("₹ 0.00");
        totalRevLabel.getStyleClass().add("title-large");
        totalRevLabel.setStyle("-fx-text-fill: #34d399;");
        revCard.getChildren().addAll(revTitle, totalRevLabel);

        // Card 2: Today's Bookings
        VBox todayCard = new VBox(4);
        todayCard.getStyleClass().add("card");
        todayCard.setPrefWidth(220);
        Label todayTitle = new Label("TODAY'S BOOKINGS");
        todayTitle.getStyleClass().add("text-subtitle");
        todayBookingsLabel = new Label("0");
        todayBookingsLabel.getStyleClass().add("title-large");
        todayBookingsLabel.setStyle("-fx-text-fill: #60a5fa;");
        todayCard.getChildren().addAll(todayTitle, todayBookingsLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // CSV Export Actions
        VBox exportBox = new VBox(6);
        exportBox.setAlignment(Pos.CENTER_RIGHT);
        Label exportTitle = new Label("Export CSV Reports (RFC-4180):");
        exportTitle.getStyleClass().add("text-subtitle");

        HBox exportButtons = new HBox(8);
        Button expBookingsBtn = new Button("Bookings CSV");
        expBookingsBtn.getStyleClass().add("button-secondary");
        // ActionEvent: Export bookings CSV
        expBookingsBtn.setOnAction(e -> exportBookings());

        Button expRevenueBtn = new Button("Revenue CSV");
        expRevenueBtn.getStyleClass().add("button-secondary");
        // ActionEvent: Export revenue CSV
        expRevenueBtn.setOnAction(e -> exportRevenue());

        Button expSalesBtn = new Button("Ticket Sales CSV");
        expSalesBtn.getStyleClass().add("button-secondary");
        // ActionEvent: Export ticket sales CSV
        expSalesBtn.setOnAction(e -> exportMovieSales());

        Button expOccupancyBtn = new Button("Occupancy CSV");
        expOccupancyBtn.getStyleClass().add("button-secondary");
        // ActionEvent: Export occupancy CSV
        expOccupancyBtn.setOnAction(e -> exportOccupancy());

        exportButtons.getChildren().addAll(expBookingsBtn, expRevenueBtn, expSalesBtn, expOccupancyBtn);
        exportBox.getChildren().addAll(exportTitle, exportButtons);

        row.getChildren().addAll(revCard, todayCard, spacer, exportBox);
        return row;
    }

    private HBox createChartsRow() {
        HBox chartsRow = new HBox(16);
        chartsRow.setAlignment(Pos.CENTER);

        // 7-Day Revenue BarChart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue (INR)");

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("7-Day Revenue Trend (PAID Only)");
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(320);
        barChart.setPrefWidth(480);
        barChart.getStyleClass().add("card");

        // Top Movies PieChart
        pieChart = new PieChart();
        pieChart.setTitle("Top Movies by Tickets Sold");
        pieChart.setPrefHeight(320);
        pieChart.setPrefWidth(440);
        pieChart.getStyleClass().add("card");

        chartsRow.getChildren().addAll(barChart, pieChart);
        return chartsRow;
    }

    private VBox createOccupancyBox() {
        VBox box = new VBox(8);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(16));

        Label title = new Label("Showtime Occupancy Analysis");
        title.getStyleClass().add("title-medium");

        Label denominatorInfo = new Label("Occupancy Denominator: Screen Capacity (Total Rows × Seats Per Row)");
        denominatorInfo.getStyleClass().add("text-muted");

        occupancyTable = new TableView<>();
        occupancyTable.setPrefHeight(240);

        TableColumn<OccupancyReportItem, Integer> idCol = new TableColumn<>("Show ID");
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getShowtimeId()).asObject());
        idCol.setPrefWidth(70);

        TableColumn<OccupancyReportItem, String> movieCol = new TableColumn<>("Movie");
        movieCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMovieTitle()));
        movieCol.setPrefWidth(200);

        TableColumn<OccupancyReportItem, String> screenCol = new TableColumn<>("Auditorium");
        screenCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getScreenName()));
        screenCol.setPrefWidth(160);

        TableColumn<OccupancyReportItem, String> timeCol = new TableColumn<>("Showtime");
        timeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedStartTime()));
        timeCol.setPrefWidth(140);

        TableColumn<OccupancyReportItem, Integer> bookedCol = new TableColumn<>("Booked");
        bookedCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getBookedSeats()).asObject());
        bookedCol.setPrefWidth(80);

        TableColumn<OccupancyReportItem, Integer> capCol = new TableColumn<>("Capacity");
        capCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getTotalSeats()).asObject());
        capCol.setPrefWidth(80);

        TableColumn<OccupancyReportItem, String> occCol = new TableColumn<>("Occupancy %");
        occCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedOccupancy()));
        occCol.setPrefWidth(120);

        occupancyTable.getColumns().addAll(idCol, movieCol, screenCol, timeCol, bookedCol, capCol, occCol);

        box.getChildren().addAll(title, denominatorInfo, occupancyTable);
        return box;
    }

    public void loadAllReportData() {
        try {
            // 1. Total Revenue (PAID payments only)
            BigDecimal totalRev = reportService.getTotalRevenue();
            totalRevLabel.setText("₹ " + totalRev.toPlainString());

            // 2. Today's Bookings
            int todayCount = reportService.getTodayBookingsCount();
            todayBookingsLabel.setText(String.valueOf(todayCount));

            // 3. 7-Day Revenue BarChart
            Map<LocalDate, BigDecimal> weeklyRev = reportService.getSevenDayRevenue();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

            for (Map.Entry<LocalDate, BigDecimal> entry : weeklyRev.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey().format(formatter), entry.getValue()));
            }
            barChart.getData().clear();
            barChart.getData().add(series);

            // 4. Top Movies PieChart
            Map<String, Integer> movieSales = reportService.getTopMoviesByTicketSales();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : movieSales.entrySet()) {
                pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
            pieChart.setData(pieData);

            // 5. Showtime Occupancy TableView
            List<OccupancyReportItem> occupancyItems = reportService.getShowtimeOccupancy();
            occupancyTable.setItems(FXCollections.observableArrayList(occupancyItems));

        } catch (SQLException e) {
            AlertHelper.showError("Reporting Error", "Failed to load reports", e.getMessage());
        }
    }

    private void exportBookings() {
        File file = chooseSaveFile("bookings_report.csv");
        if (file == null) return;
        try {
            List<Booking> list = bookingService.getFilteredAdminBookings(null, null, null, null, null);
            CsvExporter.exportBookingsToCsv(file, list);
            AlertHelper.showInfo("Export Complete", "CSV Exported", "Bookings saved to: " + file.getAbsolutePath());
        } catch (IOException | SQLException e) {
            AlertHelper.showError("Export Failed", "Error writing CSV", e.getMessage());
        }
    }

    private void exportRevenue() {
        File file = chooseSaveFile("revenue_report.csv");
        if (file == null) return;
        try {
            Map<LocalDate, BigDecimal> daily = reportService.getSevenDayRevenue();
            BigDecimal total = reportService.getTotalRevenue();
            CsvExporter.exportRevenueToCsv(file, daily, total);
            AlertHelper.showInfo("Export Complete", "CSV Exported", "Revenue report saved to: " + file.getAbsolutePath());
        } catch (IOException | SQLException e) {
            AlertHelper.showError("Export Failed", "Error writing CSV", e.getMessage());
        }
    }

    private void exportMovieSales() {
        File file = chooseSaveFile("movie_ticket_sales.csv");
        if (file == null) return;
        try {
            Map<String, Integer> sales = reportService.getTopMoviesByTicketSales();
            CsvExporter.exportMovieSalesToCsv(file, sales);
            AlertHelper.showInfo("Export Complete", "CSV Exported", "Ticket sales saved to: " + file.getAbsolutePath());
        } catch (IOException | SQLException e) {
            AlertHelper.showError("Export Failed", "Error writing CSV", e.getMessage());
        }
    }

    private void exportOccupancy() {
        File file = chooseSaveFile("showtime_occupancy.csv");
        if (file == null) return;
        try {
            List<OccupancyReportItem> list = reportService.getShowtimeOccupancy();
            CsvExporter.exportOccupancyToCsv(file, list);
            AlertHelper.showInfo("Export Complete", "CSV Exported", "Occupancy report saved to: " + file.getAbsolutePath());
        } catch (IOException | SQLException e) {
            AlertHelper.showError("Export Failed", "Error writing CSV", e.getMessage());
        }
    }

    private File chooseSaveFile(String defaultName) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save CSV Report");
        chooser.setInitialFileName(defaultName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        return chooser.showSaveDialog(null);
    }
}


package com.cineflow.ui.admin;

import com.cineflow.model.Screen;
import com.cineflow.model.Seat;
import com.cineflow.service.ScreenService;
import com.cineflow.service.ValidationException;
import com.cineflow.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Admin view for Screen/Auditorium configuration and seat layout generation.
 */
public class ScreenManagementView {

    private final ScreenService screenService;
    private TableView<Screen> tableView;
    private ObservableList<Screen> screenData;
    private FlowPane seatPreviewPane;
    private Label previewLabel;

    public ScreenManagementView() {
        this.screenService = new ScreenService();
    }

    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        // Top Toolbar
        HBox topBar = createTopBar();
        root.getChildren().add(topBar);

        // Split Layout: Left Table, Right Seat Preview
        SplitPane splitPane = new SplitPane();
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // Left Container: TableView
        VBox leftBox = new VBox(10);
        leftBox.setPadding(new Insets(10));
        tableView = createTableView();
        VBox.setVgrow(tableView, Priority.ALWAYS);
        leftBox.getChildren().addAll(new Label("Auditorium Screens:"), tableView);

        // Right Container: Seating Matrix Preview
        VBox rightBox = new VBox(12);
        rightBox.setPadding(new Insets(10));
        previewLabel = new Label("Select a screen to view its physical seat layout");
        previewLabel.getStyleClass().add("title-small");

        seatPreviewPane = new FlowPane(6, 6);
        seatPreviewPane.setPadding(new Insets(12));
        seatPreviewPane.setStyle("-fx-background-color: #0d1525; -fx-background-radius: 8px; -fx-border-color: #1e293b; -fx-border-width: 1px; -fx-border-radius: 8px;");

        ScrollPane scroll = new ScrollPane(seatPreviewPane);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        rightBox.getChildren().addAll(previewLabel, scroll);

        splitPane.getItems().addAll(leftBox, rightBox);
        splitPane.setDividerPositions(0.45);

        root.getChildren().add(splitPane);

        // Load initial data
        refreshTable();

        return root;
    }

    private HBox createTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("🏛 Auditorium Screens & Seating Layouts");
        title.getStyleClass().add("title-medium");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕ Add Screen");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> openAddScreenDialog());

        Button refreshBtn = new Button("🔁 Refresh");
        refreshBtn.getStyleClass().add("button-secondary");
        refreshBtn.setOnAction(e -> refreshTable());

        bar.getChildren().addAll(title, spacer, addBtn, refreshBtn);
        return bar;
    }

    private TableView<Screen> createTableView() {
        TableView<Screen> table = new TableView<>();
        screenData = FXCollections.observableArrayList();
        table.setItems(screenData);

        TableColumn<Screen, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Screen, String> nameCol = new TableColumn<>("🏛 Screen Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(160);

        TableColumn<Screen, Integer> rowsCol = new TableColumn<>("Rows");
        rowsCol.setCellValueFactory(new PropertyValueFactory<>("totalRows"));
        rowsCol.setPrefWidth(70);

        TableColumn<Screen, Integer> colsCol = new TableColumn<>("Seats/Row");
        colsCol.setCellValueFactory(new PropertyValueFactory<>("seatsPerRow"));
        colsCol.setPrefWidth(90);

        TableColumn<Screen, Integer> totalCol = new TableColumn<>("💺 Capacity");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalSeats"));
        totalCol.setPrefWidth(90);

        table.getColumns().addAll(idCol, nameCol, rowsCol, colsCol, totalCol);

        // Selection listener: TableView selected item preview seats
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayScreenSeats(newVal);
            }
        });

        return table;
    }

    private void displayScreenSeats(Screen screen) {
        previewLabel.setText("Layout for " + screen.getName() + " (" + screen.getTotalSeats() + " Seats)");
        seatPreviewPane.getChildren().clear();

        try {
            List<Seat> seats = screenService.getSeatsForScreen(screen.getId());
            for (Seat s : seats) {
                Button btn = new Button(s.getSeatCode());
                btn.getStyleClass().add("seat-button");
                btn.getStyleClass().add("seat-available");
                if (s.getSeatType().name().equals("PREMIUM")) {
                    btn.getStyleClass().add("seat-premium");
                }
                btn.setTooltip(new Tooltip(s.getSeatCode() + " • " + s.getSeatType()));
                seatPreviewPane.getChildren().add(btn);
            }
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to load seats", e.getMessage());
        }
    }

    public void refreshTable() {
        try {
            List<Screen> list = screenService.getAllScreens();
            screenData.setAll(list);
            if (!list.isEmpty() && tableView.getSelectionModel().getSelectedItem() == null) {
                tableView.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to Load Screens", e.getMessage());
        }
    }

    private void openAddScreenDialog() {
        Dialog<Screen> dialog = new Dialog<>();
        dialog.setTitle("Create Screen & Auto-Generate Seats");
        dialog.setHeaderText("Specify dimensions to automatically construct auditorium seating");

        ButtonType createType = new ButtonType("Generate Layout", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Audi 3 - Atmos 4K");

        Spinner<Integer> rowsSpinner = new Spinner<>(1, 26, 5);
        Spinner<Integer> colsSpinner = new Spinner<>(1, 30, 8);

        grid.add(new Label("Auditorium Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Number of Rows (A-Z):"), 0, 1); grid.add(rowsSpinner, 1, 1);
        grid.add(new Label("Seats per Row:"), 0, 2); grid.add(colsSpinner, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createType) {
                try {
                    return screenService.createScreenWithSeats(
                            nameField.getText(),
                            rowsSpinner.getValue(),
                            colsSpinner.getValue()
                    );
                } catch (ValidationException | SQLException e) {
                    AlertHelper.showError("Creation Failed", "Could not create screen", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(sc -> {
            refreshTable();
            tableView.getSelectionModel().select(sc);
        });
    }
}


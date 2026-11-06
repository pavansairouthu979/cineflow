package com.cineflow;

import com.cineflow.ui.common.NavigationManager;
import com.cineflow.util.AlertHelper;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * CineFlow - Movie Ticket Reservation Desktop Application
 * Application entry point launching the JavaFX User Interface.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Register the primary stage with the centralized navigation manager
        NavigationManager.setPrimaryStage(primaryStage);

        primaryStage.setMinWidth(920);
        primaryStage.setMinHeight(680);

        // WindowEvent: Handles confirmation when the user attempts to close the window
        primaryStage.setOnCloseRequest(event -> {
            boolean confirm = AlertHelper.showConfirmation(
                    "Exit CineFlow",
                    "Close Application?",
                    "Are you sure you want to exit CineFlow?"
            );
            if (!confirm) {
                event.consume(); // Cancels the close request
            }
        });

        // Launch into the SaaS Landing Page
        NavigationManager.showLandingView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

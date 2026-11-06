package com.cineflow.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Optional;

/**
 * Reusable utility class for displaying standardized JavaFX Alert dialogs.
 */
public class AlertHelper {

    public static void showInfo(String title, String header, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, header, content);
    }

    public static void showWarning(String title, String header, String content) {
        showAlert(Alert.AlertType.WARNING, title, header, content);
    }

    public static void showError(String title, String header, String content) {
        showAlert(Alert.AlertType.ERROR, title, header, content);
    }

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialogPane(alert.getDialogPane());

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialogPane(alert.getDialogPane());
        alert.showAndWait();
    }

    private static void styleDialogPane(DialogPane pane) {
        try {
            String cssPath = AlertHelper.class.getResource("/css/styles.css") != null
                    ? AlertHelper.class.getResource("/css/styles.css").toExternalForm()
                    : null;
            if (cssPath != null) {
                pane.getStylesheets().add(cssPath);
            }
        } catch (Exception ignored) {
            // Fallback to standard styling if CSS not found
        }
    }
}


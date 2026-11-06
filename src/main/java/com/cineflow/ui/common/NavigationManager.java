package com.cineflow.ui.common;

import com.cineflow.model.User;
import com.cineflow.service.AuthService;
import com.cineflow.ui.admin.AdminDashboardView;
import com.cineflow.ui.auth.LoginView;
import com.cineflow.ui.auth.RegistrationView;
import com.cineflow.ui.customer.CustomerDashboardView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Manages screen transitions and role-based navigation across the application.
 */
public class NavigationManager {

    private static Stage primaryStage;
    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 700;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Navigates to the Login screen.
     */
    public static void showLoginView() {
        LoginView loginView = new LoginView();
        switchScene(loginView.getView(), "CineFlow - Sign In", 920, 680);
    }

    /**
     * Navigates to the Customer Registration screen.
     */
    public static void showRegistrationView() {
        RegistrationView registrationView = new RegistrationView();
        switchScene(registrationView.getView(), "CineFlow - Customer Registration", 920, 720);
    }

    /**
     * Navigates to the appropriate dashboard based on user role.
     *
     * @param user authenticated user
     */
    public static void showDashboard(User user) {
        if (user == null) {
            showLoginView();
            return;
        }

        if (user.isAdmin()) {
            showAdminDashboard(user);
        } else {
            showCustomerDashboard(user);
        }
    }

    /**
     * Navigates to the Admin Dashboard.
     */
    public static void showAdminDashboard(User adminUser) {
        AdminDashboardView adminView = new AdminDashboardView(adminUser);
        switchScene(adminView.getView(), "CineFlow - Admin Console (" + adminUser.getFullName() + ")", DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Navigates to the Customer Dashboard.
     */
    public static void showCustomerDashboard(User customerUser) {
        CustomerDashboardView customerView = new CustomerDashboardView(customerUser);
        switchScene(customerView.getView(), "CineFlow - Customer Portal (" + customerUser.getFullName() + ")", DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Logs out the current user and returns to the Login view.
     */
    public static void logout() {
        AuthService.logout();
        showLoginView();
    }

    /**
     * Helper to set root on active scene or create a new scene with stylesheet applied.
     */
    private static void switchScene(Parent root, String title, int width, int height) {
        if (primaryStage == null) {
            return;
        }

        primaryStage.setTitle(title);
        Scene scene = primaryStage.getScene();

        if (scene == null) {
            scene = new Scene(root, width, height);
            applyStyles(scene);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private static void applyStyles(Scene scene) {
        try {
            String css = NavigationManager.class.getResource("/css/styles.css") != null
                    ? NavigationManager.class.getResource("/css/styles.css").toExternalForm()
                    : null;
            if (css != null && !scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }
        } catch (Exception e) {
            System.err.println("Could not load styles.css: " + e.getMessage());
        }
    }
}


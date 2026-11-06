package com.cineflow.ui.common;

import com.cineflow.model.User;
import com.cineflow.service.AuthService;
import com.cineflow.ui.admin.AdminDashboardView;
import com.cineflow.ui.auth.LandingView;
import com.cineflow.ui.auth.LoginView;
import com.cineflow.ui.auth.RegistrationView;
import com.cineflow.ui.customer.CustomerDashboardView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Manages screen transitions and role-based navigation across the application.
 *
 * Entry order:
 *   LandingView  →  LoginView / RegistrationView  →  Dashboard
 */
public class NavigationManager {

    private static Stage primaryStage;
    private static final int LANDING_W  = 1100;
    private static final int LANDING_H  = 760;
    private static final int AUTH_W     = 960;
    private static final int AUTH_H     = 700;
    private static final int DEFAULT_W  = 1060;
    private static final int DEFAULT_H  = 720;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /** Application entry point — SaaS landing page. */
    public static void showLandingView() {
        LandingView landingView = new LandingView();
        switchScene(landingView.getView(), "CineFlow — Premium Cinema Booking", LANDING_W, LANDING_H);
    }

    /** Navigate to login. */
    public static void showLoginView() {
        LoginView loginView = new LoginView();
        switchScene(loginView.getView(), "CineFlow — Sign In", AUTH_W, AUTH_H);
    }

    /** Navigate to registration. */
    public static void showRegistrationView() {
        RegistrationView registrationView = new RegistrationView();
        switchScene(registrationView.getView(), "CineFlow — Create Account", AUTH_W, 740);
    }

    /** Route after successful login based on role. */
    public static void showDashboard(User user) {
        if (user == null) { showLandingView(); return; }
        if (user.isAdmin()) showAdminDashboard(user);
        else                showCustomerDashboard(user);
    }

    public static void showAdminDashboard(User adminUser) {
        AdminDashboardView adminView = new AdminDashboardView(adminUser);
        switchScene(adminView.getView(),
                "CineFlow — Admin Console  (" + adminUser.getFullName() + ")",
                DEFAULT_W, DEFAULT_H);
    }

    public static void showCustomerDashboard(User customerUser) {
        CustomerDashboardView customerView = new CustomerDashboardView(customerUser);
        switchScene(customerView.getView(),
                "CineFlow — " + customerUser.getFullName(),
                DEFAULT_W, DEFAULT_H);
    }

    /** Logout → back to landing page. */
    public static void logout() {
        AuthService.logout();
        showLandingView();
    }

    /** Resize window and swap the scene root. */
    private static void switchScene(Parent root, String title, int width, int height) {
        if (primaryStage == null) return;

        primaryStage.setTitle(title);
        Scene scene = primaryStage.getScene();

        if (scene == null) {
            scene = new Scene(root, width, height);
            applyStyles(scene);
            primaryStage.setScene(scene);
        } else {
            // Resize then swap root
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
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

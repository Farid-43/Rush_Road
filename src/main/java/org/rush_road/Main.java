package org.rush_road;

import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        CompletableFuture.runAsync(() -> {
            try {
                F1Service.preloadData();
            } catch (Exception e) {
                System.err.println("Error preloading F1 data: " + e.getMessage());
            }
        });

        try {
            FirebaseService.getInstance();
            System.out.println("Firebase initialized successfully!");
        } catch (Exception e) {
            System.err.println("Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (UserSession.getInstance().isLoggedIn()) {
                HomePage homePage = new HomePage(
                        UserSession.getInstance().getUsername(),
                        UserSession.getInstance().getEmail());
                homePage.setVisible(true);
            } else {
                LoginPage loginPage = new LoginPage();
                loginPage.setVisible(true);
            }
        });
    }
}

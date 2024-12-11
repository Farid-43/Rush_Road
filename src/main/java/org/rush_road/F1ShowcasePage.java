package org.rush_road;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class F1ShowcasePage extends JPanel {
    private final HomePage homePage;
    private final Dimension originalHomeSize;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private int currentIndex = 0;
    private List<F1Service.F1CarData> cars;
    private Timer slideTimer;
    private static final ConcurrentHashMap<String, ImageIcon> imageCache = new ConcurrentHashMap<>();

    public F1ShowcasePage(HomePage homePage) {
        this.homePage = homePage;
        this.originalHomeSize = homePage.getSize();
        setLayout(new BorderLayout());

        homePage.setSize(800, 600);
        homePage.setLocationRelativeTo(null);

        setBackground(new Color(30, 30, 30));

        // Create loading panel
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(new Color(30, 30, 30));
        JLabel loadingLabel = new JLabel("Loading F1 cars...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.BOLD, 20));
        loadingLabel.setForeground(Color.WHITE);
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);

        cardPanel = new JPanel(new CardLayout());
        cardPanel.add(loadingPanel, "loading");
        cardLayout = (CardLayout) cardPanel.getLayout();
        add(cardPanel);

        setVisible(true);

        // Load data in background
        F1Service.getF1Cars().thenAccept(carList -> {
            cars = carList;
            SwingUtilities.invokeLater(() -> {
                setupMainUI();
                setupCarCards();
                startSlideShow();
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Error loading F1 cars: " + ex.getMessage());
            });
            return null;
        });

        // Setup navigation panel
        JPanel navigationPanel = new JPanel(new FlowLayout());
        navigationPanel.setBackground(new Color(30, 30, 30));

        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JButton backButton = new JButton("Back to Home");

        // Style buttons
        for (JButton button : Arrays.asList(prevButton, nextButton, backButton)) {
            styleButton(button);
        }

        prevButton.addActionListener(e -> showPrevious());
        nextButton.addActionListener(e -> showNext());
        backButton.addActionListener(e -> {
            if (slideTimer != null) {
                slideTimer.stop();
            }
            // Restore original size before showing home
            homePage.setSize(originalHomeSize);
            homePage.setLocationRelativeTo(null);
            homePage.showHome();
        });

        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);
        navigationPanel.add(backButton);

        add(navigationPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(50, 50, 150));
        button.setFocusPainted(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 70, 170));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 50, 150));
            }
        });
    }

    private void setupMainUI() {
        // Remove loading panel
        removeAll();

        // Setup main UI components
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        cardPanel.setBackground(new Color(30, 30, 30));
        add(cardPanel);

        // Create navigation panel
        JPanel navigationPanel = new JPanel(new FlowLayout());
        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JButton backButton = new JButton("Back to Home");

        // Style buttons
        for (JButton button : Arrays.asList(prevButton, nextButton, backButton)) {
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(50, 50, 150));
            button.setFocusPainted(false);
        }

        prevButton.addActionListener(e -> showPrevious());
        nextButton.addActionListener(e -> showNext());
        backButton.addActionListener(e -> {
            stopTimer(); // Stop any running timers
            homePage.setSize(originalHomeSize); // Restore original size before showing home
            homePage.setLocationRelativeTo(null);
            homePage.showHome(); // Return to home page
        });

        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);
        navigationPanel.add(backButton);
        navigationPanel.setBackground(new Color(30, 30, 30));
        add(navigationPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private void preloadImages() {
        if (cars == null)
            return;

        for (F1Service.F1CarData car : cars) {
            try {
                if (!imageCache.containsKey(car.image_url)) {
                    URL url = new URL(car.image_url);
                    ImageIcon icon = new ImageIcon(url);
                    Image scaledImage = icon.getImage().getScaledInstance(600, 300, Image.SCALE_SMOOTH);
                    imageCache.put(car.image_url, new ImageIcon(scaledImage));
                }
            } catch (Exception e) {
                System.err.println("Error preloading image: " + e.getMessage());
            }
        }

        // Once all images are loaded, update the UI
        SwingUtilities.invokeLater(() -> {
            setupCarCards();
            startSlideShow();
        });
    }

    private void setupCarCards() {
        cardPanel.removeAll();

        for (int i = 0; i < cars.size(); i++) {
            F1Service.F1CarData car = cars.get(i);
            JPanel carPanel = createCarPanel(car);
            cardPanel.add(carPanel, String.valueOf(i));
        }
    }

    private JPanel createCarPanel(F1Service.F1CarData car) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(30, 30, 30));

        // Create image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(30, 30, 30));

        // Use preloaded image
        ImageIcon cachedImage = F1Service.getPreloadedImage(car.image_url);
        if (cachedImage != null) {
            Image scaledImage = cachedImage.getImage().getScaledInstance(800, 400, Image.SCALE_SMOOTH);
            imagePanel.add(new JLabel(new ImageIcon(scaledImage)), BorderLayout.CENTER);
        } else {

            // Use cached image if available
            ImageIcon cachedImageFallback = imageCache.get(car.image_url);
            if (cachedImageFallback != null) {
                imagePanel.add(new JLabel(cachedImageFallback), BorderLayout.CENTER);
            } else {
                try {
                    // Load and cache new image
                    new Thread(() -> {
                        try {
                            ImageIcon imageIcon = new ImageIcon(new URL(car.image_url));
                            Image scaledImage = imageIcon.getImage().getScaledInstance(600, 300, Image.SCALE_SMOOTH);
                            ImageIcon scaledIcon = new ImageIcon(scaledImage);
                            imageCache.put(car.image_url, scaledIcon);
                            SwingUtilities.invokeLater(() -> {
                                imagePanel.removeAll();
                                imagePanel.add(new JLabel(scaledIcon), BorderLayout.CENTER);
                                imagePanel.revalidate();
                                imagePanel.repaint();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    // Show loading text while image loads
                    imagePanel.add(new JLabel("Loading image..."));
                } catch (Exception e) {
                    imagePanel.add(new JLabel("Image not available"));
                }
            }
        }

        // Create details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(new Color(30, 30, 30));

        // Add details
        addDetail(detailsPanel, "Model: ", car.model_name);
        addDetail(detailsPanel, "Manufacturer: ", car.manufacturer);
        addDetail(detailsPanel, "Scale: ", car.scale);
        addDetail(detailsPanel, "Price: ", "£" + car.price_gbp);
        addDetail(detailsPanel, "Championship: ", car.highlights.championship_rank);
        if (car.highlights.wins != null) {
            addDetail(detailsPanel, "Wins: ", car.highlights.wins.toString());
        }
        addDetail(detailsPanel, "Description: ", car.description);

        panel.add(imagePanel, BorderLayout.CENTER);
        panel.add(detailsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addDetail(JPanel panel, String label, String value) {
        JLabel detailLabel = new JLabel(label + value);
        detailLabel.setForeground(Color.WHITE);
        detailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(detailLabel);
        panel.add(Box.createVerticalStrut(5));
    }

    private void showNext() {
        if (cars == null)
            return;
        currentIndex = (currentIndex + 1) % cars.size();
        cardLayout.show(cardPanel, String.valueOf(currentIndex));
    }

    private void showPrevious() {
        if (cars == null)
            return;
        currentIndex = (currentIndex - 1 + cars.size()) % cars.size();
        cardLayout.show(cardPanel, String.valueOf(currentIndex));
    }

    private void startSlideShow() {
        if (slideTimer != null) {
            slideTimer.stop();
        }
        slideTimer = new Timer(4000, e -> showNext());
        slideTimer.start();
    }

    private void stopTimer() {
        if (slideTimer != null) {
            slideTimer.stop();
        }
    }
}
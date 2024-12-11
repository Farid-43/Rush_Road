package org.rush_road;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class HomePage extends JFrame {
    private final String username;
    private final String email;
    private ImageIcon backgroundImage;
    private GridBagConstraints gbc;
    private final JPanel mainContentPanel;
    private final CardLayout cardLayout;

    public HomePage(String username, String email) {
        this.username = username;
        this.email = email;

        setTitle("Rush Road - Home");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Load background image
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/images/Background.png"));
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            backgroundImage = null;
        }

        // Create a panel for cards
        mainContentPanel = new JPanel(new CardLayout());
        cardLayout = (CardLayout) mainContentPanel.getLayout();

        // Create home panel
        JPanel homePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gradient = new GradientPaint(0, 0, new Color(32, 32, 32),
                            0, getHeight(), new Color(60, 60, 60));
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        homePanel.setLayout(new GridBagLayout());
        homePanel.setOpaque(false);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 32));
        welcomeLabel.setForeground(Color.WHITE);

        // Create buttons
        JButton raceButton = createStyledButton("Race", new Color(0, 250, 0));
        JButton updateButton = createStyledButton("Update Profile", new Color(0, 10, 250));
        JButton logoutButton = createStyledButton("Logout", new Color(150, 100, 0));
        JButton deleteButton = createStyledButton("Delete Account", new Color(200, 0, 0));
        JButton quitButton = createStyledButton("Quit Game", new Color(100, 0, 0));
        JButton f1Button = createStyledButton("F1 Cars Showcase", Color.YELLOW);

        // Animation setup
        Timer animationTimer = new Timer(16, null);
        float[] slideProgress = { 0.0f };
        float[] glowIntensity = { 0.0f };
        boolean[] glowIncreasing = { true };

        animationTimer.addActionListener(e -> {

            if (slideProgress[0] < 1.0f) {
                slideProgress[0] += 0.02f;
                if (slideProgress[0] >= 1.0f) {
                    slideProgress[0] = 1.0f;
                }
                // Smooth easing function
                float easedProgress = (float) (1 - Math.pow(1 - slideProgress[0], 3));
                welcomeLabel.putClientProperty("translate", -300 * (1 - easedProgress));
                welcomePanel.revalidate();
                welcomePanel.repaint();
            }

            // Update glow effect
            if (glowIncreasing[0]) {
                glowIntensity[0] += 0.03f;
                if (glowIntensity[0] >= 1.0f) {
                    glowIntensity[0] = 1.0f;
                    glowIncreasing[0] = false;
                }
            } else {
                glowIntensity[0] -= 0.03f;
                if (glowIntensity[0] <= 0.0f) {
                    glowIntensity[0] = 0.0f;
                    glowIncreasing[0] = true;
                }
            }

            float hue = 0.16f;
            float brightness = 0.9f + (0.1f * glowIntensity[0]);
            welcomeLabel.setForeground(Color.getHSBColor(hue, 0.3f * glowIntensity[0], brightness));
        });

        // Start animation
        animationTimer.start();

        // Add cleanup
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                animationTimer.stop();
            }
        });

        // Layout components
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(welcomePanel, gbc);
        welcomePanel.add(welcomeLabel);

        gbc.gridy++;
        contentPanel.add(Box.createVerticalStrut(20), gbc);

        gbc.gridy++;
        contentPanel.add(raceButton, gbc);

        gbc.gridy++;
        contentPanel.add(updateButton, gbc);

        gbc.gridy++;
        contentPanel.add(logoutButton, gbc);

        gbc.gridy++;
        contentPanel.add(deleteButton, gbc);

        gbc.gridy++;
        contentPanel.add(f1Button, gbc);

        gbc.gridy++;
        contentPanel.add(quitButton, gbc);

        homePanel.add(contentPanel);

        mainContentPanel.add(homePanel, "HOME");
        setContentPane(mainContentPanel);

        // Add button actions
        raceButton.addActionListener(e -> {
            dispose();
            new StartGamePage(username).setVisible(true);
        });

        updateButton.addActionListener(e -> showUpdateDialog());
        logoutButton.addActionListener(e -> handleLogout());
        deleteButton.addActionListener(e -> showDeleteDialog());
        quitButton.addActionListener(e -> System.exit(0));

        f1Button.addActionListener(e -> {
            F1ShowcasePage f1Page = new F1ShowcasePage(this);
            mainContentPanel.add(f1Page, "F1");
            cardLayout.show(mainContentPanel, "F1");
        });

        // Add animations
        SwingUtilities.invokeLater(() -> {
            AnimationUtil.fadeIn(raceButton, 1000);
            AnimationUtil.fadeIn(updateButton, 1200);
            AnimationUtil.fadeIn(logoutButton, 1400);
            AnimationUtil.fadeIn(deleteButton, 1600);
            AnimationUtil.fadeIn(f1Button, 1800);
            AnimationUtil.fadeIn(quitButton, 2000);
        });
    }

    public void showHome() {
        cardLayout.show(mainContentPanel, "HOME");
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(baseColor);
                button.setFont(button.getFont().deriveFont(20.0f));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.WHITE);
                button.setFont(button.getFont().deriveFont(18.0f));
            }
        });

        return button;
    }

    private void showUpdateDialog() {
        JDialog dialog = new JDialog(this, "Update Profile", true);
        dialog.setSize(300, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBackground(new Color(32, 32, 32));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField newUsernameField = new JTextField();
        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField oldPasswordField = new JPasswordField();

        // Style labels
        JLabel[] labels = {
                new JLabel("New Username:"),
                new JLabel("New Password:"),
                new JLabel("Current Password:")
        };
        for (JLabel label : labels) {
            label.setForeground(Color.WHITE);
        }

        panel.add(labels[0]);
        panel.add(newUsernameField);
        panel.add(labels[1]);
        panel.add(newPasswordField);
        panel.add(labels[2]);
        panel.add(oldPasswordField);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            String newUsername = newUsernameField.getText();
            String newPassword = new String(newPasswordField.getPassword());
            String oldPassword = new String(oldPasswordField.getPassword());

            FirebaseService.getInstance()
                    .updateUserProfile(email, oldPassword, newUsername, newPassword)
                    .thenRun(() -> {
                        JOptionPane.showMessageDialog(dialog, "Profile updated successfully!");
                        dialog.dispose();
                        // Logout and return to login page after update
                        handleLogout();
                    })
                    .exceptionally(ex -> {
                        JOptionPane.showMessageDialog(dialog, "Update failed: " + ex.getMessage());
                        return null;
                    });
        });

        panel.add(new JLabel(""));
        panel.add(updateButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showDeleteDialog() {
        JDialog dialog = new JDialog(this, "Delete Account", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBackground(new Color(32, 32, 32));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField emailField = new JTextField(email);
        emailField.setEditable(false);
        JPasswordField passwordField = new JPasswordField();

        JLabel emailLabel = new JLabel("Email:");
        JLabel passwordLabel = new JLabel("Password:");
        emailLabel.setForeground(Color.WHITE);
        passwordLabel.setForeground(Color.WHITE);

        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);

        JButton deleteButton = new JButton("Delete Account");
        deleteButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to delete your account? This action cannot be undone.",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                FirebaseService.getInstance()
                        .deleteUser(email, password)
                        .thenRun(() -> {
                            JOptionPane.showMessageDialog(dialog, "Account deleted successfully!");
                            dialog.dispose();
                            handleLogout();
                        })
                        .exceptionally(ex -> {
                            JOptionPane.showMessageDialog(dialog, "Delete failed: " + ex.getMessage());
                            return null;
                        });
            }
        });

        panel.add(new JLabel(""));
        panel.add(deleteButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleLogout() {
        UserSession.getInstance().clearSession();
        dispose();
        new LoginPage().setVisible(true);
    }
}

package org.rush_road;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginPage extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField usernameField;
    private String currentUser;
    private String currentUsername;
    private ImageIcon backgroundImage;

    public LoginPage() {
        setTitle("Car Racing Game - Login");
        setSize(410, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        try {
            backgroundImage = new ImageIcon(getClass().getResource("/images/Background.png"));
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            backgroundImage = null;
        }

        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        panel.setOpaque(false);

        // Add sliding text label
        JLabel slidingText = new JLabel("Sign in or Sign up to start your journey!");
        slidingText.setForeground(Color.ORANGE);
        slidingText.setFont(new Font("Arial", Font.BOLD, 15));
        slidingText.setBounds(400, 85, 300, 30); // Start position off-screen
        panel.add(slidingText);

        JButton signinButton = new JButton("Sign In");
        signinButton.addActionListener(e -> showSignInDialog());
        styleButton(signinButton);
        signinButton.setBounds(160, 180, 75, 28);

        JButton signupButton = new JButton("Sign Up");
        signupButton.addActionListener(e -> showSignUpDialog());
        styleButton(signupButton);
        signupButton.setBounds(160, 230, 75, 28);

        panel.add(signinButton);
        panel.add(signupButton);

        setContentPane(panel);

        // Add animations
        SwingUtilities.invokeLater(() -> {
            // Slide in text from right to left
            AnimationUtil.slideIn(slidingText, 400, 50, 1500);
            AnimationUtil.pulseComponent(signinButton, 2000);
            AnimationUtil.pulseComponent(signupButton, 2000);
        });
    }

    private void styleButton(JButton button) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);

        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(BorderFactory.createRaisedBevelBorder());

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 120, 215));
                button.setBorder(BorderFactory.createLoweredBevelBorder());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(30, 144, 255));
                button.setBorder(BorderFactory.createRaisedBevelBorder());
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 90, 190));
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 120, 215));
            }
        });
    }

    private void showSignInDialog() {
        JDialog dialog = new JDialog(this, "Sign In", true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBackground(new Color(32, 32, 32));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField loginField = new JTextField();
        passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        styleButton(loginButton);

        JLabel usernameLabel = new JLabel("Username/Email:");
        JLabel passwordLabel = new JLabel("Password:");
        usernameLabel.setForeground(Color.WHITE);
        passwordLabel.setForeground(Color.WHITE);

        panel.add(usernameLabel);
        panel.add(loginField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(new JLabel(""));
        panel.add(loginButton);

        loginButton.addActionListener(e -> {
            String loginInput = loginField.getText().trim();
            String password = new String(passwordField.getPassword());

            FirebaseService.getInstance()
                    .signIn(loginInput, password)
                    .thenAccept(userData -> {
                        currentUser = (String) userData.get("gmail");
                        currentUsername = (String) userData.get("userId");
                        // Save session after successful login
                        UserSession.getInstance().saveSession(currentUsername, currentUser);
                        SwingUtilities.invokeLater(() -> {
                            dialog.dispose();
                            this.dispose();
                            HomePage homePage = new HomePage(currentUsername, currentUser);
                            homePage.setVisible(true);
                        });
                    })
                    .exceptionally(ex -> {
                        SwingUtilities.invokeLater(
                                () -> JOptionPane.showMessageDialog(dialog, "Login error: " + ex.getMessage()));
                        return null;
                    });
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showSignUpDialog() {
        JDialog dialog = new JDialog(this, "Sign Up", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBackground(new Color(32, 32, 32));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        emailField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        JButton registerButton = new JButton("Register");
        styleButton(registerButton);

        JLabel emailLabel = new JLabel("Email:");
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        emailLabel.setForeground(Color.WHITE);
        usernameLabel.setForeground(Color.WHITE);
        passwordLabel.setForeground(Color.WHITE);

        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(new JLabel(""));
        panel.add(registerButton);

        registerButton.addActionListener(e -> {
            String email = emailField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (registerUser(email, username, password)) {
                currentUser = email;
                currentUsername = username;
                dialog.dispose();
                this.dispose();
                showHomePage();
            }
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private boolean registerUser(String email, String username, String password) {
        try {
            FirebaseService.getInstance()
                    .registerUserWithEmailVerification(email, password, username)
                    .thenAccept(message -> {
                        JOptionPane.showMessageDialog(this, message);
                        currentUser = email;
                        currentUsername = username;
                        UserSession.getInstance().saveSession(currentUsername, currentUser);
                    })
                    .exceptionally(ex -> {
                        JOptionPane.showMessageDialog(this, "Registration error: " + ex.getMessage());
                        return null;
                    });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error registering user: " + e.getMessage());
            return false;
        }
    }

    private void showHomePage() {
        HomePage homePage = new HomePage(currentUsername, currentUser);
        homePage.setVisible(true);
    }
}

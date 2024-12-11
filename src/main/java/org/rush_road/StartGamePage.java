package org.rush_road;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class StartGamePage extends JFrame {
    private String username;
    private ImageIcon backgroundImage;

    public StartGamePage(String username) {
        this.username = username;
        setTitle("Rush Road - Racing Game");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/images/Background2.png"));
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            backgroundImage = null;
        }

        JPanel mainPanel = new JPanel(new BorderLayout()) {
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

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel welcomeLabel = createGlowingLabel("Welcome, " + username + "!", 32);

        String[] difficulties = { "Easy", "Medium", "Hard" };
        JComboBox<String> difficultySelect = new JComboBox<>(difficulties);
        difficultySelect.setSelectedIndex(1); // Set default to Medium (index 1)
        styleDifficultyComboBox(difficultySelect);

        JButton startButton = createAnimatedButton("Start Game", new Color(0, 150, 0));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(welcomeLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 2;
        contentPanel.add(Box.createVerticalStrut(30), gbc);

        gbc.gridy = 2;
        contentPanel.add(difficultySelect, gbc);

        gbc.gridy = 3;
        contentPanel.add(Box.createVerticalStrut(20), gbc);

        gbc.gridy = 4;
        contentPanel.add(startButton, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        startButton.addActionListener(e -> {
            animateTransition(() -> {
                dispose();
                startGame(difficultySelect.getSelectedIndex() + 1);
            });
        });

        SwingUtilities.invokeLater(() -> {
            AnimationUtil.slideIn(welcomeLabel, -300, welcomeLabel.getX(), 1500);

            Timer delayTimer = new Timer(800, e -> {
                AnimationUtil.fadeIn(difficultySelect, 1000);
                AnimationUtil.fadeIn(startButton, 1000);
                ((Timer) e.getSource()).stop();
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        });
    }

    private JLabel createGlowingLabel(String text, int fontSize) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, fontSize));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Timer glowTimer = new Timer(50, null);
        float[] glowIntensity = { 0.0f };
        boolean[] increasing = { true };

        glowTimer.addActionListener(e -> {
            if (increasing[0]) {
                glowIntensity[0] += 0.05f;
                if (glowIntensity[0] >= 1.0f) {
                    glowIntensity[0] = 1.0f;
                    increasing[0] = false;
                }
            } else {
                glowIntensity[0] -= 0.05f;
                if (glowIntensity[0] <= 0.0f) {
                    glowIntensity[0] = 0.0f;
                    increasing[0] = true;
                }
            }

            label.setForeground(new Color(
                    255,
                    255,
                    255,
                    200 + (int) (55 * glowIntensity[0])));
        });

        glowTimer.start();
        return label;
    }

    private void styleDifficultyComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Arial", Font.BOLD, 16));
        comboBox.setForeground(Color.WHITE);
        comboBox.setBackground(new Color(40, 40, 40));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));

        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value);
            label.setOpaque(true);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            if (isSelected) {
                label.setBackground(new Color(60, 60, 60));
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(new Color(40, 40, 40));
                label.setForeground(Color.WHITE);
            }
            return label;
        });
    }

    private JButton createAnimatedButton(String text, Color baseColor) {
        JButton button = new JButton(text);

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        // Style the text
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 18));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(new Color(0, 255, 0)); // Glow green on hover
                button.setFont(new Font("Arial", Font.BOLD, 20));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.WHITE);
                button.setFont(new Font("Arial", Font.BOLD, 18));
            }
        });

        return button;
    }
    /*
     * private void animateTransition(Runnable onComplete) {
     * // Remove the opacity animation and just start the game directly
     * dispose();
     * onComplete.run();
     * }
     */
    // Alternative animation if you want a smooth transition:

    private void animateTransition(Runnable onComplete) {
        Timer timer = new Timer(16, null);
        AtomicInteger alpha = new AtomicInteger(0);
        JPanel fadePanel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, alpha.get()));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        fadePanel.setOpaque(false);
        fadePanel.setBounds(0, 0, getWidth(), getHeight());
        getLayeredPane().add(fadePanel, JLayeredPane.DRAG_LAYER);

        timer.addActionListener(e -> {
            alpha.addAndGet(10);
            if (alpha.get() >= 255) {
                timer.stop();
                dispose();
                onComplete.run();
            }
            fadePanel.repaint();
        });

        timer.start();
    }

    private void startGame(int difficulty) {
        JFrame frame = new JFrame("Car Racing Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        Game game = new Game();
        game.setUsername(username);
        game.setDifficulty(difficulty);

        frame.add(game);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        frame.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            game.requestFocusInWindow();
            game.startBackgroundMusic();
            game.startGameLoop();
        });
    }
}

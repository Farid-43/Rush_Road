package org.rush_road;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GameOverPage extends JFrame {
    private final String username;
    private final int score;
    private final int highScore;
    private final String topScores;
    private final boolean isNewHighScore;
    private Timer animationTimer;
    private int currentScore = 0;
    private float glowValue = 0;
    private boolean glowIncreasing = true;
    private ImageIcon backgroundImage;

    public GameOverPage(String username, int score, int highScore, String topScores, boolean isNewHighScore) {
        this.username = username;
        this.score = score;
        this.highScore = highScore;
        this.topScores = topScores;
        this.isNewHighScore = isNewHighScore;

        // Load background image before setupUI
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/images/Background5.png"));
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            backgroundImage = null;
        }

        setupUI();
        startAnimations();
    }

    private void setupUI() {
        setTitle("Game Over");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    setBackground(new Color(32, 32, 32));
                }
            }
        };
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 20, 20));

        JLabel gameOverLabel = new JLabel("Game Over!");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 28));
        gameOverLabel.setForeground(new Color(255, 80, 80));
        gameOverLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.insets = new Insets(2, 10, 2, 10);

        JLabel scoreLabel = new JLabel("Your Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        scoreLabel.setForeground(new Color(255, 50, 50));
        JLabel highScoreLabel = new JLabel("All-Time High Score: " + highScore);
        highScoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        highScoreLabel.setForeground(new Color(255, 215, 0));

        // High score animation setup
        final JLabel highScoreAnimLabel;
        Timer bounceTimer = null;

        if (isNewHighScore) {
            highScoreAnimLabel = new JLabel("NEW HIGH SCORE!");
            highScoreAnimLabel.setFont(new Font("Arial", Font.BOLD, 28));
            highScoreAnimLabel.setForeground(Color.GREEN);

            bounceTimer = new Timer(100, new ActionListener() {
                private int bounceCount = 0;
                private boolean goingUp = true;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (goingUp) {
                        highScoreAnimLabel.setFont(new Font("Arial", Font.BOLD, 32));
                        goingUp = false;
                    } else {
                        highScoreAnimLabel.setFont(new Font("Arial", Font.BOLD, 28));
                        goingUp = true;
                        bounceCount++;
                    }

                    if (bounceCount > 5) {
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
            bounceTimer.start();

            // Add animations for labels
            SwingUtilities.invokeLater(() -> {
                if (isNewHighScore) {
                    AnimationUtil.pulseComponent(highScoreAnimLabel, 3000);
                }
            });

            gbc.gridy = 3;
            panel.add(highScoreAnimLabel, gbc);
        } else {
            highScoreAnimLabel = null;
        }

        SwingUtilities.invokeLater(() -> {
            AnimationUtil.fadeIn(scoreLabel, 1500);
            AnimationUtil.fadeIn(highScoreLabel, 2000);
        });

        JTextArea topScoresArea = new JTextArea(topScores);
        topScoresArea.setEditable(false);
        topScoresArea.setOpaque(false); // Make background transparent
        topScoresArea.setForeground(new Color(0, 255, 200));
        topScoresArea.setFont(new Font("Arial", Font.BOLD, 16));
        // Style buttons
        JButton playAgainButton = new JButton("Play Again");
        playAgainButton.setFont(new Font("Arial", Font.BOLD, 20));
        styleButton(playAgainButton);

        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Arial", Font.BOLD, 20));
        styleButton(homeButton);

        // Layout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 10, 2, 10);
        panel.add(gameOverLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(2, 10, 2, 10);
        panel.add(scoreLabel, gbc);

        gbc.gridy = 2;
        panel.add(highScoreLabel, gbc);

        if (isNewHighScore && highScoreAnimLabel != null) {
            gbc.gridy = 3;
            gbc.insets = new Insets(2, 10, 2, 10);
            panel.add(highScoreAnimLabel, gbc);
        }

        gbc.gridy = 4;
        gbc.insets = new Insets(5, 10, 10, 10);
        panel.add(topScoresArea, gbc);

        // Layout for buttons - modify these sections
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.4;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.ipadx = 20;
        gbc.insets = new Insets(10, 40, 10, 5);
        panel.add(playAgainButton, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(10, 5, 10, 40);
        panel.add(homeButton, gbc);

        add(panel);

        animationTimer = new Timer(50, e -> {
            if (currentScore < score) {
                currentScore += Math.max(1, score / 50);
                if (currentScore > score)
                    currentScore = score;
                scoreLabel.setText("Your Score: " + currentScore);
            }

            if (glowIncreasing) {
                glowValue += 0.1f;
                if (glowValue >= 1.0f) {
                    glowValue = 1.0f;
                    glowIncreasing = false;
                }
            } else {
                glowValue -= 0.1f;
                if (glowValue <= 0.0f) {
                    glowValue = 0.0f;
                    glowIncreasing = true;
                }
            }

            highScoreLabel.setForeground(new Color(
                    255,
                    (int) (215 + (40 * glowValue)),
                    (int) (0 + (100 * glowValue))));

            if (currentScore == score && !isNewHighScore) {
                ((Timer) e.getSource()).stop();
            }
        });

        playAgainButton.addActionListener(e -> {
            dispose(); // Close the current window
            SwingUtilities.invokeLater(() -> {
                StartGamePage startPage = new StartGamePage(username);
                startPage.setVisible(true);
            });
        });

        homeButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                HomePage homePage = new HomePage(username, UserSession.getInstance().getEmail());
                homePage.setVisible(true);
            });
        });
    }

    private void styleButton(JButton button) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);

        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255)); // Dodger Blue
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(BorderFactory.createRaisedBevelBorder());

        // Add hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.getText().equals("Play Again")) {
                    button.setBackground(new Color(0, 180, 0));
                } else {
                    button.setBackground(new Color(0, 120, 215));
                }
                button.setBorder(BorderFactory.createLoweredBevelBorder());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(30, 144, 255));
                button.setBorder(BorderFactory.createRaisedBevelBorder());
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getText().equals("Play Again") ? new Color(0, 150, 0) :

                        new Color(0, 90, 190));
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getText().equals("Play Again") ? new Color(0, 180, 0) :

                        new Color(0, 120, 215));
            }
        });
    }

    private void startAnimations() {
        animationTimer.start();
    }

    // Add this method to stop animations when closing
    @Override
    public void dispose() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        super.dispose();
    }
}

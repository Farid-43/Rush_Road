package org.rush_road;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

//Instance of the game
public class Game extends JPanel {

    int crx, cry; // crossing
    int car_x, car_y;
    int speedX, speedY;
    int nOpponent;
    String imageLoc[];
    int lx[], ly[]; // integer arrays used to store the x and y values of the oncoming vehicles
    int score;
    int highScore;
    int speedOpponent[];
    boolean isFinished;
    boolean isUp, isDown, isRight, isLeft;
    private Font scoreFont;
    private Clip backgroundMusic;
    private Clip crashSound;
    private int difficulty = Constants.MEDIUM;
    private int maxOpponents;
    private int spawnRate;
    private int[] speedRange;
    private boolean hasShield = false;
    private int shieldScoreThreshold;
    private int shieldX = -999;
    private int shieldY = -999;
    private int shieldMessageTimer = 0;
    private String username;
    private Thread gameThread;
    private volatile boolean running = true;

    private ImageIcon roadImage;
    private ImageIcon crossRoadImage;
    private ImageIcon carSelfImage;
    private ImageIcon boomImage;
    private ImageIcon[] opponentCarImages;
    private ImageIcon shieldImage;

    private static final int MOVE_SPEED = 1;

    private ImageIcon[] pedestrianImages;
    private int[] pedestrianX;
    private int[] pedestrianY;
    private int[] pedestrianType;
    private int nPedestrians;

    private ImageIcon scorePowerupImage;
    private int scorePowerupX = -999;
    private int scorePowerupY = -999;

    public Game() {
        crx = cry = -999; // initialing setting the location of the crossing to (-999,-999)
        addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                stopCar(e); // stop movement of car
            }

            public void keyPressed(KeyEvent e) {
                moveCar(e);
            }
        });
        setFocusable(true);
        car_x = 40;
        car_y = 300; // Keep vertical position the same
        isUp = isDown = isLeft = isRight = false;
        speedX = speedY = 0;
        nOpponent = 0;
        lx = new int[20];
        ly = new int[20];
        imageLoc = new String[20];
        speedOpponent = new int[20];
        isFinished = false; // when false, game is running, when true, game has ended
        score = highScore = 0;

        scoreFont = new Font("Arial", Font.BOLD, 20);

        loadSounds();
        loadImages();

        // Initialize pedestrian arrays
        pedestrianX = new int[Constants.MAX_PEDESTRIANS];
        pedestrianY = new int[Constants.MAX_PEDESTRIANS];
        pedestrianType = new int[Constants.MAX_PEDESTRIANS];
        nPedestrians = 0;

        loadPedestrianImages();
    }

    private void loadImages() {
        try {
            roadImage = new ImageIcon(getClass().getResource("/images/st_road.png"));
            crossRoadImage = new ImageIcon(getClass().getResource("/images/cross_road.png"));
            carSelfImage = new ImageIcon(getClass().getResource("/images/car_self.png"));
            boomImage = new ImageIcon(getClass().getResource("/images/boom.png"));
            shieldImage = new ImageIcon(getClass().getResource(Constants.POWERUP_IMAGE));
            scorePowerupImage = new ImageIcon(getClass().getResource(Constants.SCORE_POWERUP_IMAGE));

            opponentCarImages = new ImageIcon[5];
            for (int i = 0; i < 5; i++) {
                opponentCarImages[i] = new ImageIcon(getClass().getResource("/images/car_left_" + (i + 1) + ".png"));
            }
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPedestrianImages() {
        try {
            pedestrianImages = new ImageIcon[8];
            for (int i = 0; i < pedestrianImages.length; i++) {
                String imagePath = "/images/man_" + (i + 1) + ".png";
                java.net.URL imageUrl = getClass().getResource(imagePath);
                if (imageUrl != null) {
                    pedestrianImages[i] = new ImageIcon(imageUrl);
                    // System.out.println("Successfully loaded pedestrian image " + (i + 1));
                } else {
                    // System.err.println("Could not find pedestrian image at: " + imagePath);
                    pedestrianImages[i] = null;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading pedestrian images: " + e.getMessage());
            e.printStackTrace();
            pedestrianImages = new ImageIcon[0];
        }
    }

    public void startBackgroundMusic() {
        try {
            // Load background music
            InputStream audioSrc = getClass().getResourceAsStream("/sounds/Bgmusic.wav");
            if (audioSrc != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(audioSrc);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioIn);
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("Error playing background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }

    private void loadSounds() {
        try {
            InputStream crashSrc = getClass().getResourceAsStream("/sounds/crash.wav");
            if (crashSrc != null) {
                AudioInputStream crashIn = AudioSystem.getAudioInputStream(crashSrc);
                crashSound = AudioSystem.getClip();
                crashSound.open(crashIn);
            }
        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // scene is repainted everytime the scene settings change
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D obj = (Graphics2D) g;
        obj.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            obj.drawImage(roadImage.getImage(), 0, 0, this);
            if (cry >= -499 && crx >= -499)
                obj.drawImage(crossRoadImage.getImage(), crx, cry, this);

            obj.drawImage(carSelfImage.getImage(), car_x, car_y, this);

            if (isFinished) {
                obj.drawImage(boomImage.getImage(), car_x - 30, car_y - 30, this);
            }

            if (this.nOpponent > 0) {
                for (int i = 0; i < this.nOpponent; i++) {
                    obj.drawImage(opponentCarImages[Integer
                            .parseInt(imageLoc[i].substring(imageLoc[i].length() - 5, imageLoc[i].length() - 4)) - 1]
                            .getImage(), this.lx[i], this.ly[i], this);
                }
            }

            if (shieldX != -999 && shieldY != -999) {
                obj.drawImage(shieldImage.getImage(), shieldX, shieldY, Constants.POWERUP_WIDTH,
                        Constants.POWERUP_HEIGHT, this);
            }

            // Draw pedestrians with specified dimensions
            for (int i = 0; i < nPedestrians; i++) {
                if (pedestrianType[i] >= 0 && pedestrianType[i] < pedestrianImages.length
                        && pedestrianImages[pedestrianType[i]] != null) {
                    obj.drawImage(pedestrianImages[pedestrianType[i]].getImage(),
                            pedestrianX[i], pedestrianY[i],
                            Constants.PEDESTRIAN_WIDTH, Constants.PEDESTRIAN_HEIGHT, this);
                }
            }

            // Draw score powerup
            if (scorePowerupX != -999 && scorePowerupY != -999 && scorePowerupImage != null) {
                obj.drawImage(scorePowerupImage.getImage(), scorePowerupX, scorePowerupY,
                        Constants.SCORE_POWERUP_WIDTH, Constants.SCORE_POWERUP_HEIGHT, this);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        obj.setFont(scoreFont);
        obj.setColor(Color.WHITE);
        obj.drawString("Score: " + score, 20, 30);

        if (hasShield) {
            obj.setColor(Color.GREEN);
            obj.drawString("Shield Active", 20, 60);
        } else if (shieldMessageTimer > 0) {
            obj.setColor(Color.RED);
            obj.drawString("Shield Used!", 20, 60);
            shieldMessageTimer--;
        }
    }

    // car is driving
    void moveRoad(int count) {
        if (crx == -999 && cry == -999) {
            if (count % 10 == 0) { // after a certain time
                crx = 499; // send crossing location back at the beginning
                cry = 0;
            }
        } else {
            crx--;
        }
        if (crx == -499 && cry == 0) {
            crx = cry = -999; // reset opponent car position to beginning to start over
        }

        if (car_x < 0) // if the car has reached or gone under its min x axis value
            car_x = 0;

        // restrict car from going outside the right side of the screen
        if (car_x + 93 >= 500)
            car_x = 500 - 93;

        // restrict car from going outside the right side of the road
        if (car_y <= 124)
            car_y = 124;

        // restrict car from going outside the left side of the road
        if (car_y >= 364 - 50)
            car_y = 364 - 50;

        if (car_y < Constants.ROAD_TOP) {
            car_y = Constants.ROAD_TOP;
        }

        for (int i = 0; i < this.nOpponent; i++) { // for all opponent cars
            this.lx[i] -= speedOpponent[i];
        }
        // reset position
        int index[] = new int[nOpponent];
        for (int i = 0; i < nOpponent; i++) {
            if (lx[i] >= -127) {
                index[i] = 1;
            }
        }
        int c = 0;
        for (int i = 0; i < nOpponent; i++) {
            if (index[i] == 1) {
                imageLoc[c] = imageLoc[i];
                lx[c] = lx[i];
                ly[c] = ly[i];
                speedOpponent[c] = speedOpponent[i];
                c++;
            }
        }

        score += nOpponent - c;
        nOpponent = c;

        if (shieldX != -999 && shieldY != -999) {
            shieldX--;
            if (shieldX < -Constants.POWERUP_WIDTH) {
                shieldX = -999;
                shieldY = -999;
            }
        }

        // Check for collision
        for (int i = 0; i < nOpponent; i++) {
            if ((ly[i] >= car_y && ly[i] <= car_y + 46) || (ly[i] + 46 >= car_y && ly[i] + 46 <= car_y + 46)) {
                if (car_x + 87 >= lx[i] && !(car_x >= lx[i] + 87)) {
                    System.out.println("My car : " + car_x + ", " + car_y);
                    System.out.println("Colliding car : " + lx[i] + ", " + ly[i]);
                    if (hasShield) {
                        hasShield = false;
                        shieldMessageTimer = 100;
                        // Remove the collided opponent car
                        for (int j = i; j < nOpponent - 1; j++) {
                            lx[j] = lx[j + 1];
                            ly[j] = ly[j + 1];
                            imageLoc[j] = imageLoc[j + 1];
                            speedOpponent[j] = speedOpponent[j + 1];
                        }
                        nOpponent--;
                        return;
                    } else {
                        this.finish();
                    }
                }
            }
        }

        // Check for shield power-up collision
        if (shieldX != -999 && shieldY != -999) {
            if ((car_x < shieldX + Constants.POWERUP_WIDTH && car_x + Constants.CAR_WIDTH > shieldX)
                    && (car_y < shieldY + Constants.POWERUP_HEIGHT && car_y + Constants.CAR_HEIGHT > shieldY)) {
                hasShield = true;
                shieldX = -999;
                shieldY = -999;
            }
        }

        // Check for shield power-up spawn
        if (shieldX == -999 && shieldY == -999 && !hasShield) {
            if (score >= shieldScoreThreshold) {
                shieldX = 499; // Spawn at right side of screen
                shieldY = (int) (Math.random() * (364 - 124 - Constants.POWERUP_HEIGHT)) + 124;
                // Set next threshold
                shieldScoreThreshold += getShieldScoreThreshold();
            }
        }

        // Move pedestrians
        for (int i = 0; i < nPedestrians; i++) {
            pedestrianX[i]--;
            if (pedestrianX[i] < -Constants.PEDESTRIAN_WIDTH) {
                // Remove this pedestrian by shifting others left
                for (int j = i; j < nPedestrians - 1; j++) {
                    pedestrianX[j] = pedestrianX[j + 1];
                    pedestrianY[j] = pedestrianY[j + 1];
                    pedestrianType[j] = pedestrianType[j + 1];
                }
                nPedestrians--;
                i--;
            }
        }

        // Spawn new pedestrians
        if (nPedestrians < Constants.MAX_PEDESTRIANS && count % 200 == 0) { // Adjust 200 to control spawn rate
            spawnPedestrian();
        }

        if ((isRight || isLeft || isUp || isDown) && !isFinished) {
            car_x += speedX;
            car_y += speedY;

            // Boundary checks
            if (car_x < 0)
                car_x = 0;
            if (car_x + Constants.CAR_WIDTH > Constants.WINDOW_WIDTH)
                car_x = Constants.WINDOW_WIDTH - Constants.CAR_WIDTH;
            if (car_y < Constants.ROAD_TOP)
                car_y = Constants.ROAD_TOP;
            if (car_y + Constants.CAR_HEIGHT > Constants.ROAD_BOTTOM)
                car_y = Constants.ROAD_BOTTOM - Constants.CAR_HEIGHT;
        }

        // Move score powerup
        if (scorePowerupImage != null) {
            if (scorePowerupX != -999) {
                scorePowerupX--;
                if (scorePowerupX < -Constants.SCORE_POWERUP_WIDTH) {
                    scorePowerupX = -999;
                    scorePowerupY = -999;
                }
            }

            // Add additional randomness to spawn
            if (scorePowerupX == -999 && count % Constants.SCORE_POWERUP_SPAWN_RATE == 0
                    && Math.random() < 0.5) { // 50% chance to spawn when conditions are met
                scorePowerupX = Constants.WINDOW_WIDTH;
                scorePowerupY = Constants.ROAD_TOP + 20 +
                        (int) (Math.random()
                                * (Constants.ROAD_BOTTOM - Constants.ROAD_TOP - Constants.SCORE_POWERUP_HEIGHT - 40));
            }

            // Check collision
            if (scorePowerupX != -999 && !isFinished) {
                if ((car_x < scorePowerupX + Constants.SCORE_POWERUP_WIDTH &&
                        car_x + Constants.CAR_WIDTH > scorePowerupX) &&
                        (car_y < scorePowerupY + Constants.SCORE_POWERUP_HEIGHT &&
                                car_y + Constants.CAR_HEIGHT > scorePowerupY)) {

                    score += Constants.SCORE_POWERUP_VALUE;
                    scorePowerupX = -999;
                    scorePowerupY = -999;
                }
            }
        }
    }

    private void spawnPedestrian() {
        if (pedestrianImages == null || nPedestrians >= Constants.MAX_PEDESTRIANS) {
            return;
        }

        // Find available valid images
        List<Integer> validImageIndices = new ArrayList<>();
        for (int i = 0; i < pedestrianImages.length; i++) {
            if (pedestrianImages[i] != null) {
                validImageIndices.add(i);
            }
        }

        if (validImageIndices.isEmpty()) {
            return;
        }

        pedestrianX[nPedestrians] = Constants.WINDOW_WIDTH;
        boolean isTop = Math.random() < 0.5;
        pedestrianY[nPedestrians] = isTop ? Constants.ROADSIDE_TOP : Constants.ROADSIDE_BOTTOM;

        // Randomly select from valid images
        int randomIndex = (int) (Math.random() * validImageIndices.size());
        pedestrianType[nPedestrians] = validImageIndices.get(randomIndex);
        nPedestrians++;
    }

    void finish() {
        if (isFinished)
            return;

        if (crashSound != null) {
            crashSound.setFramePosition(0);
            crashSound.start();
        }

        stopBackgroundMusic();
        stopGameLoop();
        isFinished = true;
        this.repaint();

        new Thread(() -> {
            try {
                String difficultyKey = getDifficultyKey();

                // Save current score first
                FirebaseService.getInstance().saveScore(username, difficultyKey, score).get();
                System.out.println("Score saved successfully: " + score + " for user: " + username);

                // Get high score and top scores
                final int previousHigh = FirebaseService.getInstance().getHighScore(difficultyKey).get();
                final int newHighScore = Math.max(previousHigh, score);
                final boolean isNewHighScore = score > previousHigh;
                final String topScores = FirebaseService.getInstance().getTopScores(difficultyKey).get();

                SwingUtilities.invokeLater(() -> {
                    try {
                        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                        currentFrame.setVisible(false); // Hide current frame
                        currentFrame.dispose(); // Dispose current frame

                        GameOverPage gameOverPage = new GameOverPage(username, score, newHighScore,
                                topScores, isNewHighScore);
                        gameOverPage.setVisible(true);
                    } catch (Exception e) {
                        System.err.println("Error showing game over page: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();

            }
        }).start();
    }

    // User class to map JSON data
    class User {
        private String gmail;
        private String userId;
        private String password;
        private Map<String, List<Integer>> scores;

        public User(String gmail, String userId, String password, Map<String, List<Integer>> scores) {
            this.gmail = gmail;
            this.userId = userId;
            this.password = password;
            this.scores = scores;
        }

        public String getGmail() {
            return gmail;
        }

        public String getUserId() {
            return userId;
        }

        public String getPassword() {
            return password;
        }

        public Map<String, List<Integer>> getScores() {
            return scores;
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void moveCar(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            isRight = true;
            speedX = MOVE_SPEED;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            isLeft = true;
            speedX = -MOVE_SPEED;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            isDown = true;
            speedY = MOVE_SPEED;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            isUp = true;
            speedY = -MOVE_SPEED;
        }
    }

    public void stopCar(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            isUp = false;
            speedY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            isDown = false;
            speedY = 0; // Reset speed
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            isLeft = false;
            speedX = 0; // Reset speed
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            isRight = false;
            speedX = 0; // Reset speed
        }
    }

    public void startGameLoop() {
        running = true;
        requestFocusInWindow();
        gameThread = new Thread(() -> {
            int count = 1, c = 1;
            while (running) { // Change while(true) to while(running)
                moveRoad(count);
                while (c <= 1) {
                    repaint();
                    try {
                        Thread.sleep(5);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    c++;
                }
                c = 1;
                count++;

                // Modified spawn logic based on difficulty
                if (nOpponent < maxOpponents && count % spawnRate == 0) {
                    imageLoc[nOpponent] = "images/car_left_" + ((int) ((Math.random() * 100) % 5) + 1) + ".png";
                    lx[nOpponent] = Constants.CAR_SPAWN_X;
                    int p = (int) (Math.random() * 100) % 4;
                    if (p == 0) {
                        p = Constants.CAR_SPAWN_Y_MIDDLE_LOW; // Middle-low lane
                    } else if (p == 1) {
                        p = Constants.CAR_SPAWN_Y_BOTTOM; // Bottom lane
                    } else if (p == 2) {
                        p = Constants.CAR_SPAWN_Y_MIDDLE; // Middle lane
                    } else {
                        p = Constants.CAR_SPAWN_Y_TOP; // Top lane
                    }
                    ly[nOpponent] = p;

                    // Set speed based on difficulty range
                    speedOpponent[nOpponent] = speedRange[0]
                            + (int) (Math.random() * (speedRange[1] - speedRange[0] + 1));
                    nOpponent++;
                }
            }
        });
        gameThread.start();
    }

    // Add new method to stop the game loop
    public void stopGameLoop() {
        running = false;
        if (gameThread != null) {
            gameThread.interrupt();
        }
    }

    public void setDifficulty(int level) {
        this.difficulty = level;
        // Set game parameters based on difficulty
        switch (level) {
            case Constants.EASY:
                maxOpponents = Constants.EASY_MAX_OPPONENTS;
                spawnRate = Constants.EASY_SPAWN_RATE;
                speedRange = Constants.EASY_SPEED_RANGE;
                break;
            case Constants.HARD:
                maxOpponents = Constants.HARD_MAX_OPPONENTS;
                spawnRate = Constants.HARD_SPAWN_RATE;
                speedRange = Constants.HARD_SPEED_RANGE;
                break;
            case Constants.MEDIUM:
            default:
                maxOpponents = Constants.MEDIUM_MAX_OPPONENTS;
                spawnRate = Constants.MEDIUM_SPAWN_RATE;
                speedRange = Constants.MEDIUM_SPEED_RANGE;
                break;
        }
        // Reset shield threshold when difficulty changes
        initShieldThreshold();
    }

    private int getShieldScoreThreshold() {
        switch (difficulty) {
            case Constants.EASY:
                return Constants.EASY_POWERUP_SCORE;
            case Constants.MEDIUM:
                return Constants.MEDIUM_POWERUP_SCORE;
            case Constants.HARD:
                return Constants.HARD_POWERUP_SCORE;
            default:
                return Constants.MEDIUM_POWERUP_SCORE;
        }
    }

    private void initShieldThreshold() {
        shieldScoreThreshold = getShieldScoreThreshold();
    }

    private String getDifficultyKey() {
        switch (difficulty) {
            case Constants.EASY:
                return "easy";
            case Constants.HARD:
                return "hard";
            default:
                return "medium";
        }
    }
}

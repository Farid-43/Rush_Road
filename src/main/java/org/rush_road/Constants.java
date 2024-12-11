package org.rush_road;

public class Constants {
    // Game settings
    public static final int WINDOW_WIDTH = 500;
    public static final int WINDOW_HEIGHT = 500;
    public static final int CAR_WIDTH = 93;
    public static final int CAR_HEIGHT = 46;

    // Road boundaries
    public static final int ROAD_TOP = 130;
    public static final int ROAD_BOTTOM = 364;

    // Car spawn locations
    public static final int CAR_SPAWN_Y_TOP = 135;
    public static final int CAR_SPAWN_Y_MIDDLE = 185;
    public static final int CAR_SPAWN_Y_MIDDLE_LOW = 250;
    public static final int CAR_SPAWN_Y_BOTTOM = 310;
    public static final int CAR_SPAWN_X = 499;

    // Game difficulty levels
    public static final int EASY = 1;
    public static final int MEDIUM = 2;
    public static final int HARD = 3;

    // Difficulty specific settings
    public static final int EASY_MAX_OPPONENTS = 2;
    public static final int MEDIUM_MAX_OPPONENTS = 4;
    public static final int HARD_MAX_OPPONENTS = 6;

    public static final int EASY_SPAWN_RATE = 300; // Slower spawn rate
    public static final int MEDIUM_SPAWN_RATE = 200; // Normal spawn rate
    public static final int HARD_SPAWN_RATE = 150; // Faster spawn rate

    public static final int[] EASY_SPEED_RANGE = { 1, 2 }; // Slower opponents
    public static final int[] MEDIUM_SPEED_RANGE = { 2, 3 }; // Normal speed
    public static final int[] HARD_SPEED_RANGE = { 3, 4 }; // Faster opponents

    public static final int EASY_POWERUP_SCORE = 20;
    public static final int MEDIUM_POWERUP_SCORE = 13;
    public static final int HARD_POWERUP_SCORE = 7;
    public static final String POWERUP_IMAGE = "/images/shield.png";
    public static final int POWERUP_WIDTH = 30;
    public static final int POWERUP_HEIGHT = 30;

    // Score power-up settings
    public static final String SCORE_POWERUP_IMAGE = "/images/two.png";
    public static final int SCORE_POWERUP_WIDTH = 25;
    public static final int SCORE_POWERUP_HEIGHT = 25;
    public static final int SCORE_POWERUP_VALUE = 2;
    public static final int SCORE_POWERUP_SPAWN_RATE = 1000;

    // Pedestrian settings
    public static final int MAX_PEDESTRIANS = 2;
    public static final int PEDESTRIAN_WIDTH = 30;
    public static final int PEDESTRIAN_HEIGHT = 40;
    public static final int ROADSIDE_TOP = 70;
    public static final int ROADSIDE_BOTTOM = 370;
}

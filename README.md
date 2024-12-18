# Rush Road

## Overview
A simple car game built using Java Swing and Firebase for authentication and data management. The game features multiple levels, player authentication, a showcase of F1 cars, and session management. Below is the breakdown of the application's flow and features.

---

## Setup and Installation

### Prerequisites
- Java JDK 11 or higher
- Firebase Account

---


## Main Application Flow

### **Main.java**
- Entry point of the application.
- Preloads F1 car data in the background.
- Initializes Firebase connection.
- Checks for existing user session:
  - **If logged in** → Opens `HomePage`.
  - **If not logged in** → Opens `LoginPage`.

---

## Authentication Flow

### **LoginPage.java**
Provides Sign In and Sign Up options:

#### **Sign In**
- Fields for **Username/Email** and **Password**.
- Validates credentials with Firebase.
- **On success** → Opens `HomePage`.

#### **Sign Up**
- Fields for **Email**, **Username**, and **Password**.
- Registers a new user in Firebase.
- **On success** → Opens `HomePage`.

---

## Home Section

### **HomePage.java**
Shows multiple options:

- **Race** → Opens `StartGamePage`.
- **Update Profile** → Updates user details in Firebase.
- **Logout** → Returns to `LoginPage`.
- **Delete Account** → Removes user account from Firebase.
- **F1 Cars Showcase** → Opens `F1ShowcasePage`.
- **Quit Game** → Exits the application.

---

## Game Flow

### **StartGamePage.java**
- Displays a welcome message.
- Allows difficulty selection (**Easy**, **Medium**, **Hard**).
- **Start Game** button → Opens `Game.java`.

### **Game.java**
Main game logic:

- Controls player car movement.
- Spawns opponent cars.
- Handles collisions.
- Manages power-ups (**shield**, **score**).
- Tracks score.
- **On collision** → Opens `GameOverPage`.

#### Detailed Game Mechanics
- **Car Movement**
  - Arrow keys control player car
  - Boundary checks prevent car from leaving road
  - Smooth acceleration and deceleration

- **Opponent Cars**
  - Dynamic spawn rate based on difficulty
  - Random lane selection
  - Variable speeds per difficulty:
    - Easy: 1-2 speed units
    - Medium: 2-3 speed units
    - Hard: 3-4 speed units

- **Power-ups**
  - Shield Power-up:
    - Spawns based on score thresholds
    - Provides temporary invincibility
    - Different thresholds per difficulty:
      - Easy: Every 20 points
      - Medium: Every 13 points
      - Hard: Every 7 points
  
  - Score Power-up:
    - adds two extra points when collected
    - Random spawn intervals
    - Appears on random road positions

- **Collision Detection**
  - Precise hitbox checking
  - Shield protection handling
  - Game over triggering

- **Score System**
  - Points for avoiding cars
  - Bonus points from power-ups
  - High score tracking per difficulty

### **GameOverPage.java**
- Displays the final score.
- Shows the high score.
- Displays a list of top scores.
- Options:
  - **Play Again** → Returns to `StartGamePage`.
  - **Home** → Returns to `HomePage`.

#### Score Management
- Displays current game score
- Shows all-time high score
- Leaderboard showing top 3 players
- Score persistence in Firebase
- Animated score counting effect

---

## Additional Features

### **F1ShowcasePage.java**
- Displays an F1 car collection.
- Shows car images and details.
- Auto-slideshow functionality.
- Navigation buttons (**Previous/Next**).
- Back button returns to `HomePage`.

### **FirebaseService.java**
Handles all Firebase operations:

- User authentication.
- Score saving.
- High score tracking.
- User profile management.

#### Authentication Methods
- `registerUserWithEmailVerification(email, password, username)`
  - Creates new user account
  - Stores user data in Realtime Database
  - Handles email verification

- `signIn(loginInput, password)`
  - Supports login via email or username
  - Validates credentials
  - Returns user session data

#### Score Management
- `saveScore(userId, difficulty, score)`
  - Adds new score to user's history
  - Maintains separate scores per difficulty
  - Updates high scores if applicable

- `getTopScores(difficulty)`
  - Retrieves top 3 scores per difficulty
  - Sorts scores in descending order
  - Returns formatted leaderboard string

#### Profile Management
- `updateUserProfile(email, oldPassword, newUsername, newPassword)`
  - Verifies current password
  - Updates username and/or password
  - Maintains data consistency

- `deleteUser(email, password)`
  - Removes user authentication
  - Deletes user data from database
  - Handles cleanup of related data

#### Session Management
- Stores logged-in user details
- Maintains persistent login state
- Handles secure logout
- Manages session expiration

### **AnimationUtil.java**
- Provides UI animation utilities.
- Used across different pages for visual effects.

### **Constants.java**
- Stores game constants.
- Defines difficulty settings.
- Contains UI dimensions and settings.


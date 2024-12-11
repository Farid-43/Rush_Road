
# Rush Road

## Overview
A car racing game built using Java Swing and Firebase for authentication and data management. The game features multiple levels, player authentication, a showcase of F1 cars, and session management. Below is the breakdown of the application's flow and features.

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

### **GameOverPage.java**
- Displays the final score.
- Shows the high score.
- Displays a list of top scores.
- Options:
  - **Play Again** → Returns to `StartGamePage`.
  - **Home** → Returns to `HomePage`.

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

### **UserSession.java**
- Manages user session data.
- Saves/loads session to a file.
- Handles session persistence.

### **AnimationUtil.java**
- Provides UI animation utilities.
- Used across different pages for visual effects.

### **Constants.java**
- Stores game constants.
- Defines difficulty settings.
- Contains UI dimensions and settings.



package org.rush_road;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class UserSession {
    private static final String SESSION_FILE = "user_session.dat";
    private static UserSession instance;
    private String username;
    private String email;

    private UserSession() {
        loadSession();
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void saveSession(String username, String email) {
        this.username = username;
        this.email = email;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SESSION_FILE))) {
            oos.writeObject(username);
            oos.writeObject(email);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSession() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SESSION_FILE))) {
            this.username = (String) ois.readObject();
            this.email = (String) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // File doesn't exist or error reading - no session
            this.username = null;
            this.email = null;
        }
    }

    public void clearSession() {
        this.username = null;
        this.email = null;
        new File(SESSION_FILE).delete();
    }

    public boolean isLoggedIn() {
        return username != null && email != null;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
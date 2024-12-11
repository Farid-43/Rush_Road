package org.rush_road;

import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseService {
    private static FirebaseService instance;
    private final DatabaseReference database;

    private FirebaseService() {
        try {
            InputStream serviceAccount = getClass().getResourceAsStream("/serviceAccountKey.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://rush-road-43-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            database = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing Firebase", e);
        }
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    public CompletableFuture<Boolean> saveUser(String gmail, String userId, String password) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Map<String, Object> user = new HashMap<>();
        user.put("gmail", gmail);
        user.put("userId", userId);
        user.put("password", password);
        user.put("scores", new HashMap<String, List<Integer>>() {
            {
                put("easy", new ArrayList<>());
                put("medium", new ArrayList<>());
                put("hard", new ArrayList<>());
            }
        });

        database.child("users").child(userId).setValue(user, (error, ref) -> {
            future.complete(error == null);
        });
        return future;
    }

    public CompletableFuture<List<Map<String, Object>>> getAllUsers() {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Map<String, Object>> users = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        String gmail = userSnapshot.child("gmail").getValue(String.class);
                        String userId = userSnapshot.child("userId").getValue(String.class);
                        String password = userSnapshot.child("password").getValue(String.class);

                        Map<String, List<Integer>> scores = new HashMap<>();
                        DataSnapshot scoresSnapshot = userSnapshot.child("scores");
                        for (String difficulty : Arrays.asList("easy", "medium", "hard")) {
                            List<Integer> difficultyScores = new ArrayList<>();
                            if (scoresSnapshot.hasChild(difficulty)) {
                                for (DataSnapshot scoreSnap : scoresSnapshot.child(difficulty).getChildren()) {
                                    Integer score = scoreSnap.getValue(Integer.class);
                                    if (score != null) {
                                        difficultyScores.add(score);
                                    }
                                }
                            }
                            scores.put(difficulty, difficultyScores);
                        }

                        Map<String, Object> user = new HashMap<>();
                        user.put("gmail", gmail);
                        user.put("userId", userId);
                        user.put("password", password);
                        user.put("scores", scores);

                        users.add(user);
                    } catch (Exception e) {
                        System.err.println("Error parsing user data: " + e.getMessage());
                    }
                }
                future.complete(users);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future;
    }

    public CompletableFuture<Boolean> saveScore(String userId, String difficulty, int score) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // First get existing scores
        database.child("users").child(userId)
                .child("scores").child(difficulty)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Integer> scores = new ArrayList<>();

                        for (DataSnapshot scoreSnapshot : snapshot.getChildren()) {
                            Integer existingScore = scoreSnapshot.getValue(Integer.class);
                            if (existingScore != null) {
                                scores.add(existingScore);
                            }
                        }

                        scores.add(score);

                        // Update scores in Firebase
                        database.child("users").child(userId)
                                .child("scores").child(difficulty)
                                .setValue(scores, (error, ref) -> {
                                    future.complete(error == null);
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(error.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<String> getTopScores(String difficulty) {
        CompletableFuture<String> future = new CompletableFuture<>();
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Map.Entry<String, Integer>> scoreList = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.child("userId").getValue(String.class);
                    DataSnapshot scores = userSnapshot.child("scores").child(difficulty);
                    int highScore = 0;
                    for (DataSnapshot scoreSnapshot : scores.getChildren()) {
                        Integer score = scoreSnapshot.getValue(Integer.class);
                        if (score != null && score > highScore) {
                            highScore = score;
                        }
                    }
                    if (highScore > 0) {
                        scoreList.add(new AbstractMap.SimpleEntry<>(userId, highScore));
                    }
                }
                scoreList.sort((a, b) -> b.getValue() - a.getValue());

                StringBuilder sb = new StringBuilder();
                sb.append("Top 3 Scores:\n");
                // Only take top 3 scores
                int count = 0;
                for (Map.Entry<String, Integer> entry : scoreList) {
                    if (count < 3) {
                        sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                        count++;
                    } else {
                        break;
                    }
                }
                future.complete(sb.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future;
    }

    public CompletableFuture<Integer> getHighScore(String difficulty) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int highScore = 0;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    DataSnapshot scoresSnapshot = userSnapshot.child("scores").child(difficulty);
                    for (DataSnapshot scoreSnapshot : scoresSnapshot.getChildren()) {
                        Integer score = scoreSnapshot.getValue(Integer.class);
                        if (score != null && score > highScore) {
                            highScore = score;
                        }
                    }
                }
                future.complete(highScore);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future;
    }

    public CompletableFuture<String> registerUserWithEmailVerification(String email, String password, String username) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setEmailVerified(false);

            FirebaseAuth.getInstance().createUser(request);

            // Create user in realtime database
            saveUser(email, username, password)
                    .thenAccept(success -> {
                        if (success) {
                            future.complete("Registration successful!");
                        } else {
                            future.completeExceptionally(new Exception("Failed to save user data"));
                        }
                    });

        } catch (FirebaseAuthException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    public CompletableFuture<Map<String, Object>> signIn(String loginInput, String password) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        // Get all users and check both email and username
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean userFound = false;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String gmail = userSnapshot.child("gmail").getValue(String.class);
                    String userId = userSnapshot.child("userId").getValue(String.class);
                    String storedPassword = userSnapshot.child("password").getValue(String.class);

                    // Check if login matches either email or username
                    if ((gmail != null && gmail.equals(loginInput)) ||
                            (userId != null && userId.equals(loginInput))) {

                        userFound = true;

                        if (storedPassword != null && storedPassword.equals(password)) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("gmail", gmail);
                            userData.put("userId", userId);
                            future.complete(userData);
                            return;
                        } else {
                            future.completeExceptionally(new Exception("Invalid password"));
                            return;
                        }
                    }
                }

                if (!userFound) {
                    future.completeExceptionally(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    public CompletableFuture<Void> updateUserProfile(String email, String oldPassword, String newUsername,
            String newPassword) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // First verify old password
        signIn(email, oldPassword)
                .thenAccept(user -> {
                    // Update the user data
                    database.child("users").child((String) user.get("userId"))
                            .updateChildren(Map.of(
                                    "userId", newUsername,
                                    "password", newPassword), (error, ref) -> {
                                        if (error != null) {
                                            future.completeExceptionally(new Exception("Failed to update profile"));
                                        } else {
                                            future.complete(null);
                                        }
                                    });
                })
                .exceptionally(ex -> {
                    future.completeExceptionally(new Exception("Invalid current password"));
                    return null;
                });

        return future;
    }

    public CompletableFuture<Void> deleteUser(String email, String password) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // First verify password and get user info
        signIn(email, password)
                .thenAccept(user -> {
                    try {
                        // First, delete from Authentication
                        String uid = FirebaseAuth.getInstance()
                                .getUserByEmail(email)
                                .getUid();

                        FirebaseAuth.getInstance().deleteUser(uid);

                        // Then delete from Realtime Database
                        database.child("users").child((String) user.get("userId"))
                                .removeValue((error, ref) -> {
                                    if (error != null) {
                                        future.completeExceptionally(
                                                new Exception("Failed to delete account from database"));
                                    } else {
                                        future.complete(null);
                                    }
                                });
                    } catch (FirebaseAuthException e) {
                        future.completeExceptionally(
                                new Exception("Failed to delete authentication: " + e.getMessage()));
                    }
                })
                .exceptionally(ex -> {
                    future.completeExceptionally(new Exception("Invalid password or authentication error"));
                    return null;
                });

        return future;
    }

    public void printAllUsers() {
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println("\n=== All Users in Database ===");
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String gmail = userSnapshot.child("gmail").getValue(String.class);
                    String userId = userSnapshot.child("userId").getValue(String.class);
                    System.out.println("User: " + userId + " (Email: " + gmail + ")");
                }
                System.out.println("===========================\n");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error fetching users: " + error.getMessage());
            }
        });
    }
}
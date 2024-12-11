package org.rush_road;

import java.awt.Image;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class F1Service {

    public static class F1CarData {
        public String image_url;
        public String model_name;
        public String manufacturer;
        public String scale;
        public int price_gbp;
        public String description;
        public Availability availability;
        public Dimensions dimensions;
        public Highlights highlights;

        public static class Availability {
            public Integer dispatch_time_weeks;
            public Boolean pre_order;
            public Boolean in_stock;
            public Integer restock_time_weeks;
        }

        public static class Dimensions {
            public int length_mm;
        }

        public static class Highlights {
            public String championship_rank;
            public String notable_finish;
            public Integer podiums;
            public Integer wins;
        }
    }

    private static final String API_URL = "https://nayeemcode.pythonanywhere.com/data?file=farid&key=jhbfhsdjdjsadbjsd";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static List<F1CarData> carDataCache;
    private static final Map<String, ImageIcon> imageCache = new ConcurrentHashMap<>();
    private static boolean isPreloading = false;
    private static CompletableFuture<Void> preloadFuture;
    private static final ExecutorService executor = Executors.newFixedThreadPool(3); // Thread pool for image loading

    public static CompletableFuture<List<F1CarData>> getF1Cars() {
        if (carDataCache != null) {
            return CompletableFuture.completedFuture(carDataCache);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("API request failed with status: " + response.statusCode());
                    }
                    System.out.println("Received JSON response");
                    return response.body();
                })
                .thenApply(json -> {
                    try {
                        List<F1CarData> cars = gson.fromJson(json, new TypeToken<List<F1CarData>>() {
                        }.getType());
                        carDataCache = cars;
                        // Start preloading images
                        preloadImages(cars);
                        return cars;
                    } catch (Exception e) {
                        System.err.println("Error parsing JSON: " + e.getMessage());
                        throw new RuntimeException("Failed to parse JSON response", e);
                    }
                });
    }

    private static void preloadImages(List<F1CarData> cars) {
        for (F1CarData car : cars) {
            executor.submit(() -> {
                try {
                    if (!imageCache.containsKey(car.image_url)) {
                        ImageIcon icon = new ImageIcon(new URL(car.image_url));
                        Image scaledImage = icon.getImage().getScaledInstance(600, 300, Image.SCALE_SMOOTH);
                        imageCache.put(car.image_url, new ImageIcon(scaledImage));
                        // System.out.println("Preloaded image: " + car.image_url); // Debug log
                    }
                } catch (Exception e) {
                    System.err.println("Error preloading image: " + car.image_url);
                }
            });
        }
    }

    public static ImageIcon getCachedImage(String url) {
        return imageCache.get(url);
    }

    public static ImageIcon getPreloadedImage(String url) {
        return imageCache.get(url);
    }

    public static boolean isDataPreloaded() {
        return carDataCache != null && !imageCache.isEmpty();
    }

    public static void preloadData() {
        if (carDataCache == null) {
            getF1Cars().join();
        }
    }

    // Add shutdown hook to clean up executor
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
        }));
    }
}
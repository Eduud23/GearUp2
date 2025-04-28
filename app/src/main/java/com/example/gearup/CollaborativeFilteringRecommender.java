package com.example.gearup;

import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.*;
import java.util.stream.Collectors;

public class CollaborativeFilteringRecommender {
    private static final String TAG = "CollaborativeFiltering";

    public static void generateRecommendations(String currentUserId, OnRecommendationsGeneratedListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://gearup-df833-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference dbRef = database.getReference("user_interactions");

        dbRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                Log.e(TAG, "❌ Firebase fetch failed or no user interactions found.");
                listener.onRecommendationsGenerated(Collections.emptyList());
                return;
            }

            Map<String, Object> allUsers = (Map<String, Object>) task.getResult().getValue();
            if (allUsers == null || allUsers.isEmpty()) {
                Log.e(TAG, "❌ User interactions map is empty.");
                listener.onRecommendationsGenerated(Collections.emptyList());
                return;
            }

            Log.d(TAG, "🔥 Retrieved " + allUsers.size() + " users from Firebase.");

            // Get the current user's interacted products
            Set<String> currentUserProducts = new HashSet<>();
            if (allUsers.containsKey(currentUserId)) {
                currentUserProducts = ((Map<String, Object>) allUsers.get(currentUserId)).keySet();
            }

            Log.d(TAG, "👤 Current user (" + currentUserId + ") interacted with: " + currentUserProducts);

            // Early exit if new user (no interactions)
            if (currentUserProducts.isEmpty()) {
                Log.w(TAG, "⚠ New user detected. No recommendations will be generated.");
                listener.onRecommendationsGenerated(Collections.emptyList());
                return;
            }

            // Map for collecting recommendations and popular products
            Map<String, Integer> recommendedProducts = new HashMap<>();
            Map<String, Integer> popularProducts = new HashMap<>();
            Set<String> otherUserProducts = new HashSet<>();

            double totalSimilarity = 0.0;
            int userComparisons = 0;

            // Loop over other users to calculate similarity
            for (String userId : allUsers.keySet()) {
                if (userId.equals(currentUserId)) continue; // Skip self

                Map<String, Object> userProducts = (Map<String, Object>) allUsers.get(userId);
                if (userProducts == null || userProducts.isEmpty()) continue;

                Set<String> otherUserProductSet = userProducts.keySet();
                otherUserProducts.addAll(otherUserProductSet);

                // Update popular products (any product interacted with)
                for (String productId : otherUserProductSet) {
                    popularProducts.put(productId, popularProducts.getOrDefault(productId, 0) + 1);
                }

                // Calculate similarity using Jaccard similarity
                double similarity = jaccardSimilarity(currentUserProducts, otherUserProductSet);
                Log.d(TAG, "🔗 Similarity with user " + userId + ": " + similarity);

                if (similarity > 0.0) {
                    totalSimilarity += similarity;
                    userComparisons++;
                }
            }

            // Calculate dynamic similarity threshold
            double averageSimilarity = (userComparisons > 0) ? totalSimilarity / userComparisons : 0.0;
            double similarityThreshold = Math.max(averageSimilarity * 0.5, 0.05);
            Log.d(TAG, "📊 Avg Similarity: " + averageSimilarity + " | Dynamic Threshold: " + similarityThreshold);

            // Create an unmodifiable copy of currentUserProducts before entering the lambda
            final Set<String> currentUserProductsCopy = Collections.unmodifiableSet(new HashSet<>(currentUserProducts));

            // Second pass to collect recommendations based on dynamic threshold
            for (String userId : allUsers.keySet()) {
                if (userId.equals(currentUserId)) continue;

                Map<String, Object> userProducts = (Map<String, Object>) allUsers.get(userId);
                if (userProducts == null || userProducts.isEmpty()) continue;

                Set<String> otherUserProductSet = userProducts.keySet();
                double similarity = jaccardSimilarity(currentUserProducts, otherUserProductSet);

                // If similarity is above the threshold, recommend products not interacted with
                if (similarity >= similarityThreshold) {
                    for (String productId : otherUserProductSet) {
                        // Use the unmodifiable copy of currentUserProducts to avoid modification inside the lambda
                        if (!currentUserProductsCopy.contains(productId)) {
                            recommendedProducts.put(productId, recommendedProducts.getOrDefault(productId, 0) + 1);
                        }
                    }
                }
            }

            // Incorporate reviews and purchases with a higher weight for purchases
            DatabaseReference reviewsRef = database.getReference("user_reviews");
            DatabaseReference purchasesRef = database.getReference("user_purchases");

            reviewsRef.get().addOnCompleteListener(reviewTask -> {
                if (reviewTask.isSuccessful() && reviewTask.getResult().exists()) {
                    Map<String, Object> allReviews = (Map<String, Object>) reviewTask.getResult().getValue();
                    if (allReviews != null) {
                        allReviews.values().forEach(userReview -> {
                            Map<String, Object> userProducts = (Map<String, Object>) userReview;
                            userProducts.keySet().forEach(productId -> {
                                // Only recommend products that the current user hasn't interacted with
                                if (!currentUserProductsCopy.contains(productId)) {
                                    recommendedProducts.put(productId, recommendedProducts.getOrDefault(productId, 0) + 1);
                                }
                            });
                        });
                    }
                }
            });

            purchasesRef.get().addOnCompleteListener(purchaseTask -> {
                if (purchaseTask.isSuccessful() && purchaseTask.getResult().exists()) {
                    Map<String, Object> allPurchases = (Map<String, Object>) purchaseTask.getResult().getValue();
                    if (allPurchases != null) {
                        allPurchases.values().forEach(userPurchase -> {
                            Map<String, Object> userProducts = (Map<String, Object>) userPurchase;
                            userProducts.keySet().forEach(productId -> {
                                // Only recommend products that the current user hasn't interacted with
                                if (!currentUserProductsCopy.contains(productId)) {
                                    // Higher weight for purchases
                                    recommendedProducts.put(productId, recommendedProducts.getOrDefault(productId, 0) + 2);
                                }
                            });
                        });
                    }
                }

                // Sort the recommendations based on the scores
                List<String> sortedRecommendations = recommendedProducts.entrySet().stream()
                        .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                // If no recommendations are found, fallback to popular products
                if (sortedRecommendations.isEmpty()) {
                    sortedRecommendations = popularProducts.entrySet().stream()
                            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                            .map(Map.Entry::getKey)
                            .limit(10)
                            .collect(Collectors.toList());
                    Log.d(TAG, "✅ Final recommendations: " + sortedRecommendations);
                }

                listener.onRecommendationsGenerated(sortedRecommendations);
            });
        });
    }

    private static double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    public interface OnRecommendationsGeneratedListener {
        void onRecommendationsGenerated(List<String> recommendedProductIds);
    }
}

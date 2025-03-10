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

            // Get current user interactions
            Set<String> currentUserProducts = new HashSet<>();
            if (allUsers.containsKey(currentUserId)) {
                currentUserProducts = ((Map<String, Object>) allUsers.get(currentUserId)).keySet();
            }

            Log.d(TAG, "👤 Current user (" + currentUserId + ") interacted with: " + currentUserProducts);

            Map<String, Integer> recommendedProducts = new HashMap<>();
            Map<String, Integer> popularProducts = new HashMap<>();
            Set<String> otherUserProducts = new HashSet<>();

            double totalSimilarity = 0.0;
            int userComparisons = 0;

            for (String userId : allUsers.keySet()) {
                if (userId.equals(currentUserId)) continue; // Skip self

                Map<String, Object> userProducts = (Map<String, Object>) allUsers.get(userId);
                if (userProducts == null || userProducts.isEmpty()) continue;

                Set<String> otherUserProductSet = userProducts.keySet();
                otherUserProducts.addAll(otherUserProductSet);

                for (String productId : otherUserProductSet) {
                    popularProducts.put(productId, popularProducts.getOrDefault(productId, 0) + 1);
                }

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

            // Second pass to collect recommendations based on dynamic threshold
            for (String userId : allUsers.keySet()) {
                if (userId.equals(currentUserId)) continue;

                Map<String, Object> userProducts = (Map<String, Object>) allUsers.get(userId);
                if (userProducts == null || userProducts.isEmpty()) continue;

                Set<String> otherUserProductSet = userProducts.keySet();
                double similarity = jaccardSimilarity(currentUserProducts, otherUserProductSet);

                if (similarity >= similarityThreshold) {
                    for (String productId : otherUserProductSet) {
                        if (!currentUserProducts.contains(productId)) {
                            recommendedProducts.put(productId, recommendedProducts.getOrDefault(productId, 0) + 1);
                        }
                    }
                }
            }

            List<String> sortedRecommendations = recommendedProducts.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // If no recommendations but only one other user exists, recommend all their products
            if (sortedRecommendations.isEmpty() && allUsers.size() == 2) {
                sortedRecommendations = new ArrayList<>(otherUserProducts);
                Log.w(TAG, "⚠ Only two users exist. Recommending all unique products from the other user.");
            }

            Log.d(TAG, "✅ Final recommendations: " + sortedRecommendations);
            listener.onRecommendationsGenerated(sortedRecommendations);
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

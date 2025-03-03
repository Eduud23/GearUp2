package com.example.gearup;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserInteractionLogger {

    private static final String TAG = "FirebaseInteraction";

    public static void logProductClick(String userId, String productId, String productName, String category) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "❌ Error: User ID is null or empty. Cannot log interaction.");
            return;
        }
        if (productId == null || productId.isEmpty()) {
            Log.e(TAG, "❌ Error: Product ID is null or empty. Cannot log interaction.");
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://gearup-df833-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference dbRef = database.getReference("user_interactions").child(userId).child(productId);

        Log.d(TAG, "🔥 Database Path: " + dbRef.toString());

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> interaction = new HashMap<>();
        interaction.put("productId", productId);
        interaction.put("productName", productName);
        interaction.put("category", category);
        interaction.put("timestamp", timestamp);

        dbRef.setValue(interaction)  // Overwrites existing data for the same productId
                .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Interaction logged successfully!"))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to log interaction: " + e.getMessage(), e));
    }

    public static void logReviewInteraction(String userId, String productId, String sellerId, String reviewText, float rating) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "❌ Error: User ID is null or empty. Cannot log review.");
            return;
        }
        if (productId == null || productId.isEmpty()) {
            Log.e(TAG, "❌ Error: Product ID is null or empty. Cannot log review.");
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://gearup-df833-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference dbRef = database.getReference("user_reviews").child(userId).child(productId);

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("productId", productId);
        reviewData.put("sellerId", sellerId);
        reviewData.put("reviewText", reviewText);
        reviewData.put("rating", rating);
        reviewData.put("timestamp", timestamp);

        dbRef.setValue(reviewData)  // Overwrites existing review for the same productId
                .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Review interaction logged successfully!"))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to log review interaction: " + e.getMessage(), e));
    }

    public static String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            Log.e(TAG, "❌ Error: No authenticated user found.");
            return null;
        }
    }

}

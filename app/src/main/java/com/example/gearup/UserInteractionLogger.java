package com.example.gearup;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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

        // Ensure Firebase is initialized
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://gearup-df833-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference dbRef = database.getReference("user_interactions");

        Log.d(TAG, "🔥 Database Path: " + dbRef.toString());

        // Generate timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create interaction object
        Map<String, Object> interaction = new HashMap<>();
        interaction.put("productId", productId);
        interaction.put("productName", productName);
        interaction.put("category", category);
        interaction.put("timestamp", timestamp);

        // Save to Firebase
        dbRef.child(userId).push().setValue(interaction)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Interaction logged successfully!"))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to log interaction: " + e.getMessage(), e));
    }

    // Helper method to log the current user ID
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

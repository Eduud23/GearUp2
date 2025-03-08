package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirestoreTestActivity extends AppCompatActivity {
    private static final String TAG = "FirestoreTest";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the second Firebase app instance
        FirebaseApp secondApp = FirebaseApp.getInstance("gearupdataSecondApp");
        FirebaseFirestore db = FirebaseFirestore.getInstance(secondApp);

        // Sample data
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Test User");
        user.put("email", "testuser@example.com");

        db.collection("users").document("testUser")
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Firestore data added successfully!"))
                .addOnFailureListener(e -> Log.e(TAG, "Error adding Firestore data", e));
    }
}

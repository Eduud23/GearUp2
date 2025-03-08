package com.example.gearup;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyApp extends Application {
    private static final String TAG = "MyApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Unique Firebase app name
        String uniqueFirebaseName = "gearupdataSecondApp";

        // Check if the second Firebase app is already initialized
        boolean isInitialized = false;
        for (FirebaseApp app : FirebaseApp.getApps(this)) {
            if (app.getName().equals(uniqueFirebaseName)) {
                isInitialized = true;
                break;
            }
        }

        if (!isInitialized) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:368644301658:android:your-firebase-app-id") // Replace with actual Firebase App ID
                    .setApiKey("AIzaSyDuFgOsZN2b-SsG1s6w5TkMFX5N0XAiTb4") // Replace with your actual API Key
                    .setProjectId("gearupdata-ac81d") // Replace with your Project ID
                    .setStorageBucket("gearupdata-ac81d.appspot.com") // Replace with your Storage Bucket
                    .setGcmSenderId("368644301658")
                    .build();

            FirebaseApp.initializeApp(this, options, uniqueFirebaseName);
            Log.d(TAG, "Firebase app initialized with name: " + uniqueFirebaseName);
        } else {
            Log.d(TAG, "Firebase app already initialized with name: " + uniqueFirebaseName);
        }

        // Get Firestore instance from the second Firebase app
        FirebaseApp secondApp = FirebaseApp.getInstance(uniqueFirebaseName);
        FirebaseFirestore db = FirebaseFirestore.getInstance(secondApp);
    }
}

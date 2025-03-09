package com.example.gearup;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyApp2 extends Application {
    private static final String TAG = "MyApp2";

    @Override
    public void onCreate() {
        super.onCreate();

        // Unique Firebase app name
        String uniqueFirebaseName = "gearupdataThirdApp";

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
                    .setApplicationId("1:500523403445:android:7a0241f487ed8a24e556fc") // Replace with actual Firebase App ID
                    .setApiKey("AIzaSyDC7TJEh5ULgkOsAaC9PTb_QJmQOPl_bQc") // Replace with your actual API Key
                    .setProjectId("gear2-a56eb") // Replace with your Project ID
                    .setStorageBucket("gear2-a56eb.appspot.com") // Replace with your Storage Bucket
                    .setGcmSenderId("500523403445")
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

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

        // Initialize First Firebase App (Default)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Default FirebaseApp initialized");
        }

        // Initialize Second Firebase App
        initializeSecondFirebaseApp();

        // Initialize Third Firebase App
        initializeThirdFirebaseApp();
        initializeFourthFirebaseApp();
        initializeFifthFirebaseApp();

    }

    private void initializeSecondFirebaseApp() {
        String secondAppName = "gearupdataSecondApp";
        boolean isInitialized = false;

        for (FirebaseApp app : FirebaseApp.getApps(this)) {
            if (app.getName().equals(secondAppName)) {
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

            FirebaseApp.initializeApp(this, options, secondAppName);
            Log.d(TAG, "Second Firebase app initialized: " + secondAppName);
        } else {
            Log.d(TAG, "Second Firebase app already initialized: " + secondAppName);
        }
    }

    private void initializeThirdFirebaseApp() {
        String thirdAppName = "gearupdataThirdApp";
        boolean isInitialized = false;

        for (FirebaseApp app : FirebaseApp.getApps(this)) {
            if (app.getName().equals(thirdAppName)) {
                isInitialized = true;
                break;
            }
        }

        if (!isInitialized) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:500523403445:android:7a0241f487ed8a24e556fc") // Replace with actual Firebase App ID
                    .setApiKey("AIzaSyAtej_Uj9kfPQTCZaK2bIpNzkjCFVzu0qQ") // Replace with your actual API Key
                    .setProjectId("populardata-95ea7") // Replace with your Project ID
                    .setStorageBucket("populardata-95ea7.appspot.com") // Replace with your Storage Bucket
                    .setGcmSenderId("998782782083")
                    .build();

            FirebaseApp.initializeApp(this, options, thirdAppName);
            Log.d(TAG, "Third Firebase app initialized: " + thirdAppName);
        } else {
            Log.d(TAG, "Third Firebase app already initialized: " + thirdAppName);
        }
    }
    private void initializeFourthFirebaseApp() {
        String fourthAppName = "gearupdataFourthApp";
        boolean isInitialized = false;

        for (FirebaseApp app : FirebaseApp.getApps(this)) {
            if (app.getName().equals(fourthAppName)) {
                isInitialized = true;
                break;
            }
        }

        if (!isInitialized) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:866517356305:android:d6bd5930190ac41a7986b0") // Replace with actual Firebase App ID
                    .setApiKey("AIzaSyAtej_Uj9kfPQTCZaK2bIpNzkjCFVzu0qQ") // Replace with your actual API Key
                    .setProjectId("social-popular") // Replace with your Project ID
                    .setStorageBucket("social-popular.appspot.com") // Replace with your Storage Bucket
                    .setGcmSenderId("866517356305") // Replace with your Sender ID
                    .build();

            FirebaseApp.initializeApp(this, options, fourthAppName);
            Log.d(TAG, "Fourth Firebase app initialized: " + fourthAppName);
        } else {
            Log.d(TAG, "Fourth Firebase app already initialized: " + fourthAppName);
        }
    }

    private void initializeFifthFirebaseApp() {
        String fifthAppName = "gearupdataFifthApp";
        boolean isInitialized = false;

        // Check if the Firebase app is already initialized
        for (FirebaseApp app : FirebaseApp.getApps(this)) {
            if (app.getName().equals(fifthAppName)) {
                isInitialized = true;
                break;
            }
        }

        // If the fifth Firebase app is not initialized, initialize it
        if (!isInitialized) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:852626210125:android:45ed84b8f1d733a712cb04") // Replace with actual Firebase App ID
                    .setApiKey("AIzaSyAtej_Uj9kfPQTCZaK2bIpNzkjCFVzu0qQ") // Replace with your actual API Key
                    .setProjectId("forecast-7ba66") // Replace with your Project ID
                    .setStorageBucket("forecast-7ba66.appspot.com") // Replace with your Storage Bucket
                    .setGcmSenderId("852626210125") // Replace with your Sender ID
                    .build();

            FirebaseApp.initializeApp(this, options, fifthAppName);
            Log.d(TAG, "Fifth Firebase app initialized: " + fifthAppName);
        } else {
            Log.d(TAG, "Fifth Firebase app already initialized: " + fifthAppName);
        }
    }
}

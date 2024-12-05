package com.example.gearup;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

public class DeviceUtils {

    // Method to check if the device is an emulator
    public static boolean isEmulator() {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MODEL.contains("Android SDK built for x86_64")
                || Build.MODEL.contains("Genymotion")
                || Build.MODEL.contains("sdk_google")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic")
                || "google_sdk".equals(Build.PRODUCT));
    }

    // Method to check if the device is connected to a local network
    public static boolean isDeviceConnectedToLocalNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                // Check if the device is connected to a local Wi-Fi network (using IP address or network type)
                // You can add more advanced checks here based on your use case
                return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            }
        }
        return false;
    }

    // Method to check if the device is on a staging network (replace with actual logic)
    public static boolean isDeviceOnStagingNetwork(Context context) {
        // Example: Check the device's IP or network characteristics to determine if it is on a staging network
        // This is a placeholder; you can replace it with the actual logic
        return false;  // Replace with actual condition
    }

    // Method to check if the device is on a production network (replace with actual logic)
    public static boolean isDeviceOnProductionNetwork(Context context) {
        // Example: Check the device's IP or network characteristics to determine if it is on a production network
        // This is a placeholder; you can replace it with the actual logic
        return false;  // Replace with actual condition
    }
}

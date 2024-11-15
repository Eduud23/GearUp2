package com.example.gearup;

import android.os.Build;

public class DeviceUtils {
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
}

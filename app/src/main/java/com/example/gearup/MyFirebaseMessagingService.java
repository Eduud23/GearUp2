package com.example.gearup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log; // Make sure to import Log
import androidx.core.app.NotificationCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Optionally save notification to Firestore with current timestamp
            long currentTimestamp = System.currentTimeMillis(); // Get the current time if timestamp is not sent
            saveNotificationToFirestore(title, body, currentTimestamp);

            // Show notification
            sendNotification(title, body);
        }

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            String customTitle = remoteMessage.getData().get("title");
            String customBody = remoteMessage.getData().get("body");
            String timestampStr = remoteMessage.getData().get("timestamp");

            long timestamp = System.currentTimeMillis(); // Default to current time if parsing fails
            if (timestampStr != null) {
                try {
                    timestamp = Long.parseLong(timestampStr); // Convert to long
                } catch (NumberFormatException e) {
                    Log.w("FCM", "Failed to parse timestamp: " + timestampStr, e);
                }
            }

            // Save notification to Firestore
            saveNotificationToFirestore(customTitle, customBody, timestamp);

            // Optionally show the notification with custom data
            sendNotification(customTitle, customBody);
        }
    }


    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, NotificationFragmentBuyer.class); // Change this to your desired activity or fragment
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "User_Notification";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification) // Customize your notification icon
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // For Android O and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel Human-Readable Title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void saveNotificationToFirestore(String title, String body, long timestamp) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        NotificationModel notification = new NotificationModel(title, body);
        db.collection("notifications") // Change to your Firestore collection name
                .add(notification)
                .addOnSuccessListener(documentReference -> Log.d("FCM", "Notification saved with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("FCM", "Error adding notification", e));
    }


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Log or send the token to your server if necessary
        Log.d("FCM Token", token);
    }
}
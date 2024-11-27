package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragmentBuyer extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public NotificationFragmentBuyer() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate your layout
        View rootView = inflater.inflate(R.layout.fragment_notification_buyer, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize the RecyclerView
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the notification list and adapter
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList, new NotificationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Notification notification) {
                // Handle click: Check the type of notification and navigate accordingly
                if (notification.getReceiverId() != null) {
                    // Message notification - Open ConversationListActivity
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String currentUserId = currentUser.getUid();

                        // Create intent and pass the currentUserId
                        Intent intent = new Intent(getContext(), ConversationListActivity.class);
                        intent.putExtra("CURRENT_USER_ID", currentUserId);
                        startActivity(intent);
                    } else {
                        // Handle case where user is not authenticated
                        Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle other types of notifications here (e.g., order notifications)
                    // You can handle other notification types if needed
                    Log.d("NotificationFragment", "Non-message notification clicked");
                }
            }
        });

        recyclerView.setAdapter(notificationAdapter);

        // Fetch message notifications for the current buyer
        fetchMessageNotifications();

        return rootView;
    }
    private void fetchMessageNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("NotificationFragment", "User is not logged in");
            return;
        }

        String currentUserId = currentUser.getUid();  // Get the current user ID
        Log.d("NotificationFragment", "Fetching message notifications for receiverId: " + currentUserId);

        // Query Firestore for notifications where the receiverId matches the current user's ID
        db.collectionGroup("messagenotification")  // Using collectionGroup to query all subcollections named 'messagenotification'
                .whereEqualTo("receiverId", currentUserId)  // Filter by receiverId
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)  // Order by timestamp
                .get()  // Get the notifications
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("NotificationFragment", "No message notifications found for this buyer.");
                    } else {
                        Log.d("NotificationFragment", "Found " + queryDocumentSnapshots.size() + " message notifications.");
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Map the document to a Notification object
                            Notification notification = documentSnapshot.toObject(Notification.class);
                            if (notification != null) {
                                notificationList.add(notification);  // Add to list
                                Log.d("NotificationFragment", "Added message notification: " + notification.getMessage());
                            }
                        }
                        // Notify the adapter that data has changed and update the UI
                        notificationAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationFragment", "Error fetching message notifications", e);
                });
    }
}

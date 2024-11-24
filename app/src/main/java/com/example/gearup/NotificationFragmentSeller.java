package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragmentSeller extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public NotificationFragmentSeller() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_notification_seller, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();  // Initialize FirebaseAuth
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);

        recyclerView.setAdapter(notificationAdapter);

        fetchNotifications();  // Call to fetch notifications

        return rootView;
    }

    private void fetchNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("NotificationFragment", "User is not logged in");
            return;
        }

        String currentUserId = currentUser.getUid();  // Get the current user ID
        Log.d("NotificationFragment", "Fetching notifications for sellerId: " + currentUserId);

        // Query the 'ordernotification' subcollection for the specific seller
        db.collectionGroup("ordernotification")  // Using collectionGroup to query all subcollections named 'ordernotification'
                .whereEqualTo("sellerId", currentUserId)  // Filter by sellerId matching the current user
                .orderBy("timestamp", Query.Direction.DESCENDING)  // Order by timestamp to show the most recent notifications first
                .get()  // Get all documents that match the query
                .addOnSuccessListener(orderNotificationSnapshots -> {
                    if (orderNotificationSnapshots.isEmpty()) {
                        Log.d("NotificationFragment", "No order notifications found for this seller.");
                    } else {
                        Log.d("NotificationFragment", "Found " + orderNotificationSnapshots.size() + " order notifications.");
                        for (DocumentSnapshot orderSnapshot : orderNotificationSnapshots) {
                            // Map the data from the snapshot to a Notification object
                            Notification notification = orderSnapshot.toObject(Notification.class);
                            if (notification != null) {
                                notificationList.add(notification);  // Add to list
                                Log.d("NotificationFragment", "Added notification: " + notification.getMessage());
                            } else {
                                Log.e("NotificationFragment", "Failed to map notification object.");
                            }
                        }
                        // Notify the adapter that data has changed and update the UI
                        notificationAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationFragment", "Error fetching order notifications", e);
                });
    }


}

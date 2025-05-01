package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class NotificationFragmentSeller extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public NotificationFragmentSeller() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification_seller, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        notificationList = new ArrayList<>();

        notificationAdapter = new NotificationAdapter(notificationList, new NotificationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Notification notification) {
                // ✅ First check for order notification
                if (notification.getOrderId() != null) {
                    Log.d("NotificationFragment", "Order notification clicked");

                    Intent intent = new Intent(getContext(), ManageOrderActivity.class);
                    intent.putExtra("ORDER_ID", notification.getOrderId()); // Optional: pass the order ID
                    startActivity(intent);
                }
                // ✅ Then check for message notification
                else if (notification.getReceiverId() != null) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String currentUserId = currentUser.getUid();
                        Intent intent = new Intent(getContext(), ConversationSellerActivity.class);
                        intent.putExtra("CURRENT_USER_ID", currentUserId);
                        startActivity(intent);
                    }
                } else {
                    Log.d("NotificationFragment", "Unknown notification type clicked");
                }
            }
        });

        recyclerView.setAdapter(notificationAdapter);
        fetchNotifications();

        return rootView;
    }

    private void fetchNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("NotificationFragment", "User is not logged in");
            return;
        }

        String currentUserId = currentUser.getUid();
        Log.d("NotificationFragment", "Fetching notifications for sellerId: " + currentUserId);

        fetchOrderNotifications(currentUserId);
        fetchMessageNotifications(currentUserId);
    }

    private void fetchOrderNotifications(String currentUserId) {
        db.collectionGroup("ordernotification")
                .whereEqualTo("sellerId", currentUserId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("NotificationFragment", "No order notifications found for seller.");
                    } else {
                        Log.d("NotificationFragment", "Found " + queryDocumentSnapshots.size() + " order notifications.");
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Notification notification = documentSnapshot.toObject(Notification.class);
                            if (notification != null) {
                                notificationList.add(notification);
                                Log.d("NotificationFragment", "Order notification: " + notification.getMessage());
                            }
                        }
                        notificationAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("NotificationFragment", "Error fetching order notifications", e));
    }

    private void fetchMessageNotifications(String currentUserId) {
        db.collectionGroup("messagenotification")
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("NotificationFragment", "No message notifications found for seller.");
                    } else {
                        Log.d("NotificationFragment", "Found " + queryDocumentSnapshots.size() + " message notifications.");
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Notification notification = documentSnapshot.toObject(Notification.class);
                            if (notification != null) {
                                notificationList.add(notification);
                                Log.d("NotificationFragment", "Message notification: " + notification.getMessage());
                            }
                        }
                        notificationAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("NotificationFragment", "Error fetching message notifications", e));
    }
}

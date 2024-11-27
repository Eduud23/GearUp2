package com.example.gearup;

import android.content.Intent;
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
        mAuth = FirebaseAuth.getInstance();
        notificationList = new ArrayList<>();

        // Set up the adapter with the item click listener
        notificationAdapter = new NotificationAdapter(notificationList, new NotificationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Notification notification) {
                // Handle click: Check the type of notification and navigate accordingly
                if (notification.getReceiverId() != null) {
                    // Message notification - Open ConversationSellerActivity
                    Intent intent = new Intent(getContext(), ConversationSellerActivity.class);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String currentUserId = currentUser.getUid();
                        intent.putExtra("CURRENT_USER_ID", currentUserId); // Pass currentUserId
                        startActivity(intent);
                    }
                } else {
                    // Order notification - Open ManageOrderActivity
                    Intent intent = new Intent(getContext(), ManageOrderActivity.class);
                    startActivity(intent);
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
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(orderNotificationSnapshots -> {
                    if (!orderNotificationSnapshots.isEmpty()) {
                        for (DocumentSnapshot orderSnapshot : orderNotificationSnapshots) {
                            Notification notification = orderSnapshot.toObject(Notification.class);
                            if (notification != null) {
                                notificationList.add(notification);
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
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(messageNotificationSnapshots -> {
                    if (!messageNotificationSnapshots.isEmpty()) {
                        for (DocumentSnapshot messageSnapshot : messageNotificationSnapshots) {
                            Notification notification = messageSnapshot.toObject(Notification.class);
                            if (notification != null) {
                                notificationList.add(notification);
                            }
                        }
                        notificationAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("NotificationFragment", "Error fetching message notifications", e));
    }
}

package com.example.gearup;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationFragmentBuyer extends Fragment {

    //private RecyclerView notificationRecyclerView;
    //private NotificationAdapter notificationAdapter;
    //private List<NotificationModel> notificationList;
   // private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate your layout
        return inflater.inflate(R.layout.fragment_notification_buyer, container, false);
    }

    /*@Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize your views
        notificationRecyclerView = view.findViewById(R.id.notificationRecyclerView);
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);

        // Set up RecyclerView
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationRecyclerView.setAdapter(notificationAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load notifications
        loadNotifications();
    }

    private void loadNotifications() {
        db.collection("notifications")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notificationList.clear(); // Clear the list to avoid duplicates
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String body = document.getString("body");
                            Log.d("NotificationFragment", "Received notification: " + title + ", " + body);
                            notificationList.add(new NotificationModel(title, body));
                        }
                        Log.d("NotificationFragment", "Notifications count: " + notificationList.size());
                        notificationAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("NotificationFragment", "Error getting documents: ", task.getException());
                    }
                });
    }
    */
}

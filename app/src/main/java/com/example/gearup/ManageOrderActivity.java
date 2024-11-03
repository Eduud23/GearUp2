package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class ManageOrderActivity extends AppCompatActivity {
    private RecyclerView recyclerViewOrders;
    private ManageOrderAdapter manageOrderAdapter;
    private List<OrderItem> orderItems;
    private FirebaseFirestore db;
    private ListenerRegistration orderListenerRegistration;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_order);

        recyclerViewOrders = findViewById(R.id.recyclerView_orders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        orderItems = new ArrayList<>();
        manageOrderAdapter = new ManageOrderAdapter(orderItems, this);
        recyclerViewOrders.setAdapter(manageOrderAdapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            fetchOrders();
        } else {
            Log.e("ManageOrder", "User not logged in.");
            finish();
        }
    }

    private void fetchOrders() {
        orderListenerRegistration = db.collection("orders")
                .whereEqualTo("sellerId", currentUserId) // Fetch orders for products sold by the current seller
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("ManageOrder", "Error fetching orders", error);
                            return;
                        }

                        orderItems.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            OrderItem orderItem = doc.toObject(OrderItem.class);
                            orderItems.add(orderItem);
                        }
                        manageOrderAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListenerRegistration != null) {
            orderListenerRegistration.remove();
        }
    }
}

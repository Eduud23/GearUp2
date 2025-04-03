package com.example.gearup;

import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class OrderedProductsFragment extends Fragment {
    private RecyclerView recyclerViewOrdered;
    private PurchasedAdapter purchasedAdapter;
    private FirebaseFirestore db;
    private List<OrderItem> orderedItems;
    private ListenerRegistration orderedListenerRegistration;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ordered_products, container, false);
        recyclerViewOrdered = view.findViewById(R.id.recyclerView_ordered);
        recyclerViewOrdered.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        orderedItems = new ArrayList<>();
        purchasedAdapter = new PurchasedAdapter(orderedItems);
        recyclerViewOrdered.setAdapter(purchasedAdapter);

        // Get the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            fetchOrderedItems();
        }

        return view;
    }

    // Fetch ordered items for the logged-in user
    private void fetchOrderedItems() {
        orderedListenerRegistration = db.collection("orders")
                .whereEqualTo("userId", currentUserId) // Filter orders by current user's ID
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return; // Handle the error (you can show a Toast or log it)
                        }

                        // Clear the existing list of orders and update with new ones
                        orderedItems.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            // Manually retrieve the fields if necessary (to avoid issues with nested fields)
                            String orderId = doc.getId();
                            String productName = doc.getString("productName");
                            Long quantity = doc.getLong("quantity");
                            double totalPrice = doc.getDouble("totalPrice");
                            String customerName = doc.getString("customerInfo.fullName");
                            String shippingAddress = doc.getString("shippingAddress");
                            String paymentMethod = doc.getString("payment.cardType");
                            String orderStatus = doc.getString("status");
                            String imageUrl = doc.getString("imageUrl");

                            // Create an OrderItem and add it to the list
                            OrderItem orderItem = new OrderItem(orderId, productName, quantity, totalPrice,
                                    customerName, shippingAddress, paymentMethod, orderStatus, imageUrl);
                            orderedItems.add(orderItem);
                        }
                        purchasedAdapter.notifyDataSetChanged(); // Notify the adapter that the data set has changed
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (orderedListenerRegistration != null) {
            orderedListenerRegistration.remove(); // Remove the listener when the fragment is destroyed to avoid memory leaks
        }
    }
}
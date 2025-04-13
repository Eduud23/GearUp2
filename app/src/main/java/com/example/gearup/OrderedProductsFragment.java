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
import java.util.Map;

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

        // Pass both Context and the list of orderedItems to the adapter constructor
        purchasedAdapter = new PurchasedAdapter(getContext(), orderedItems); // This line is now fixed
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
                .whereEqualTo("product.userId", currentUserId) // Filter orders by current user's ID
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return; // Handle the error (you can show a Toast or log it)
                        }

                        // Clear the existing list of orders and update with new ones
                        orderedItems.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            // Manually retrieve the fields, handling nested fields
                            String orderId = doc.getId();

                            // Retrieve the 'product' map and its fields
                            Map<String, Object> productMap = (Map<String, Object>) doc.get("product");
                            String productName = (String) productMap.get("productName");
                            String productBrand = (String) productMap.get("productBrand");
                            String productYear = (String) productMap.get("productYear");

                            // Safely retrieve and cast productQuantity
                            Object productQuantityObj = productMap.get("productQuantity");
                            Long productQuantity = null;

                            if (productQuantityObj instanceof Long) {
                                productQuantity = (Long) productQuantityObj;
                            } else if (productQuantityObj instanceof String) {
                                try {
                                    productQuantity = Long.parseLong((String) productQuantityObj); // Parse the string into a Long
                                } catch (NumberFormatException e) {
                                    // Handle invalid format here (log or set a default value)
                                    productQuantity = 0L;  // Default value in case of parsing failure
                                }
                            } else {
                                // Handle the case where the quantity is neither a Long nor a String (invalid data)
                                productQuantity = 0L; // Default value
                            }

                            // Safely retrieve and check if productPrice is null
                            Double productPrice = (Double) productMap.get("totalPrice");
                            if (productPrice == null) {
                                productPrice = 0.0; // Default to 0.0 if productPrice is null
                            }

                            String imageUrl = (String) productMap.get("imageUrl");

                            // Retrieve the 'customerInfo' map and its fields
                            Map<String, Object> customerInfo = (Map<String, Object>) doc.get("customerInfo");
                            String customerName = (String) customerInfo.get("fullName");
                            String shippingAddress = (String) doc.get("shippingAddress");

                            // Retrieve other fields
                            String deliveryOption = (String) doc.get("deliveryType");
                            String orderStatus = (String) doc.get("status");
                            String sellerId = (String) doc.get("product.sellerId");
                            String paymentIntentId = (String) doc.get("product.paymentIntentId");

                            // Create an OrderItem object and add it to the list
                            OrderItem orderItem = new OrderItem(orderId, productName, productQuantity, productPrice,
                                    customerName, shippingAddress, deliveryOption, orderStatus, deliveryOption, imageUrl, sellerId, paymentIntentId);

                            // Add the order item to the list
                            orderedItems.add(orderItem);
                        }

                        // Notify the adapter that the data set has changed
                        purchasedAdapter.notifyDataSetChanged();
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

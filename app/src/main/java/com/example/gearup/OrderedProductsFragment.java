package com.example.gearup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
    private String selectedStatus = "All"; // Default filter is 'All'

    private ImageView filterCategoryIcon; // Filter icon to trigger status selection

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ordered_products, container, false);

        recyclerViewOrdered = view.findViewById(R.id.recyclerView_ordered);
        recyclerViewOrdered.setLayoutManager(new LinearLayoutManager(getContext()));

        filterCategoryIcon = view.findViewById(R.id.spinner_category); // Initialize the filter icon

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
            fetchOrderedItems(); // Fetch the ordered items from Firestore
        }

        // Set up the filter icon's click listener
        filterCategoryIcon.setOnClickListener(v -> showStatusFilterDialog());

        return view;
    }

    // Show a dialog to choose the order status filter
    private void showStatusFilterDialog() {
        final String[] statuses = {"All", "Pending", "Shipping", "Ready to pickup", "Complete"};

        new AlertDialog.Builder(getContext())
                .setTitle("Select Order Status")
                .setSingleChoiceItems(statuses, getStatusIndex(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedStatus = statuses[which];
                        fetchOrderedItems(); // Re-fetch items based on the selected status filter
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // Helper method to get the current index of selected status in the filter dialog
    private int getStatusIndex() {
        switch (selectedStatus) {
            case "Pending":
                return 1;
            case "Shipping":
                return 2;
            case "Ready to pickup":
                return 3;
            case "Delivered":
                return 4;
            default:
                return 0; // Default to "All"
        }
    }

    // Fetch ordered items for the logged-in user and apply status filtering
    private void fetchOrderedItems() {
        orderedListenerRegistration = db.collection("orders")
                .whereEqualTo("product.userId", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }

                        orderedItems.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            String orderId = doc.getId();

                            Map<String, Object> productMap = (Map<String, Object>) doc.get("product");
                            String productName = (String) productMap.get("productName");
                            String productBrand = (String) productMap.get("productBrand");
                            String productYear = (String) productMap.get("productYear");
                            String productId = (String) productMap.get("productId");
                            String buyerId = (String) productMap.get("userId");
                            String sellerId = (String) productMap.get("sellerId");
                            String paymentIntentId = (String) productMap.get("paymentIntentId");

                            Object productQuantityObj = productMap.get("productQuantity");
                            Long productQuantity;
                            if (productQuantityObj instanceof Long) {
                                productQuantity = (Long) productQuantityObj;
                            } else if (productQuantityObj instanceof String) {
                                try {
                                    productQuantity = Long.parseLong((String) productQuantityObj);
                                } catch (NumberFormatException e) {
                                    productQuantity = 0L;
                                }
                            } else {
                                productQuantity = 0L;
                            }

                            Double productPrice = (Double) productMap.get("totalPrice");
                            if (productPrice == null) {
                                productPrice = 0.0;
                            }

                            String imageUrl = (String) productMap.get("imageUrl");

                            Map<String, Object> customerInfo = (Map<String, Object>) doc.get("customerInfo");
                            String customerName = (String) customerInfo.get("fullName");
                            String shippingAddress = (String) doc.get("shippingAddress");

                            String deliveryOption = (String) doc.get("deliveryType");
                            String orderStatus = (String) doc.get("status");

                            // Filter the orders based on the selected status
                            if (selectedStatus.equals("All") || orderStatus.equals(selectedStatus)) {
                                String paymentMethod = (String) productMap.get("paymentMethod");

                                // Create an OrderItem and add it to the list
                                OrderItem orderItem = new OrderItem(
                                        orderId,
                                        productName,
                                        productQuantity,
                                        productPrice,
                                        customerName,
                                        shippingAddress,
                                        paymentMethod,
                                        orderStatus,
                                        deliveryOption,
                                        imageUrl,
                                        sellerId,
                                        paymentIntentId,
                                        productId,
                                        productBrand,
                                        productYear,
                                        buyerId
                                );

                                orderedItems.add(orderItem);
                            }
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

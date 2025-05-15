package com.example.gearup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManageOrderActivity extends AppCompatActivity implements ManageOrderAdapter.OnStatusUpdateListener {

    private RecyclerView recyclerView;
    private ManageOrderAdapter adapter;
    private List<OrderItem> orderList;
    private List<OrderItem> filteredOrderList;
    private ImageButton imageButtonOrderStatus;  // Changed to ImageButton
    private EditText searchView;  // Search field
    private String selectedStatus = "Pending";  // Default status for filtering

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_order);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();
        adapter = new ManageOrderAdapter(filteredOrderList, this, this);
        recyclerView.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        imageButtonOrderStatus = findViewById(R.id.imageButton_order_status);
        searchView = findViewById(R.id.search_view); // Initialize search view

        // Set OnClickListener for ImageButton (Order Status)
        imageButtonOrderStatus.setOnClickListener(v -> showOrderStatusDialog());

        // Set listener for the search field
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Get the search query and filter the orders
                String searchQuery = charSequence.toString().trim();
                filterOrdersByStatusAndSearch(selectedStatus, searchQuery);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Fetch orders from Firestore
        fetchOrdersFromFirestore();
    }

    private void showOrderStatusDialog() {
        final String[] statuses = {"Pending", "Shipping", "Ready to pickup", "Complete"};

        new AlertDialog.Builder(this)
                .setTitle("Select Order Status")
                .setItems(statuses, (dialog, which) -> {
                    selectedStatus = statuses[which];
                    // Filter orders by selected status and current search query
                    filterOrdersByStatusAndSearch(selectedStatus, searchView.getText().toString());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchOrdersFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentSellerId = currentUser.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String sellerId = document.getString("product.sellerId");

                        // Only add if the current user is the seller
                        if (currentSellerId.equals(sellerId)) {
                            String orderId = document.getId();
                            String productName = document.getString("product.productName");
                            Long quantityObj = document.getLong("product.productQuantity");
                            long quantity = quantityObj != null ? quantityObj : 0;

                            Double totalPriceObj = document.getDouble("product.totalPrice");
                            double totalPrice = totalPriceObj != null ? totalPriceObj : 0.0;
                            String buyerId = document.getString("product.userId");
                            String customerName = document.getString("customerInfo.fullName");
                            String shippingAddress = document.getString("shippingAddress");
                            String paymentMethod = document.getString("product.paymentMethod");
                            String orderStatus = document.getString("status");
                            String imageUrl = document.getString("product.imageUrl");
                            String deliveryOption = document.getString("deliveryType");
                            String paymentIntentId = document.getString("product.paymentIntentId");
                            String productId = document.getString("product.productId");
                            String brand = document.getString("product.productBrand");
                            String productYear = document.getString("product.productYear");

                            OrderItem orderItem = new OrderItem(orderId, productName, quantity, totalPrice,
                                    customerName, shippingAddress, paymentMethod, orderStatus, deliveryOption, imageUrl,
                                    sellerId, paymentIntentId, productId, brand, productYear, buyerId);

                            orderList.add(orderItem);
                        }
                    }

                    filterOrdersByStatusAndSearch(selectedStatus, "");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                });
    }


    private void filterOrdersByStatusAndSearch(String status, String searchQuery) {
        filteredOrderList.clear();

        for (OrderItem orderItem : orderList) {
            boolean matchesStatus = orderItem.getOrderStatus().equalsIgnoreCase(status) || status.equals("Default");
            boolean matchesSearch = orderItem.getProductName().toLowerCase().contains(searchQuery.toLowerCase());

            if (matchesStatus && matchesSearch) {
                filteredOrderList.add(orderItem);
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStatusUpdate(OrderItem orderItem) {
        final String[] statuses;
        if ("pickup".equalsIgnoreCase(orderItem.getDeliveryOption())) {
            statuses = new String[]{"Pending", "Ready to pickup", "Complete"};
        } else {
            statuses = new String[]{"Pending", "Shipping", "Ready to pickup", "Complete"};
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Order Status")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];
                    updateOrderStatus(orderItem, newStatus);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateOrderStatus(OrderItem orderItem, String newStatus) {
        int foundPosition = -1;
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderId().equals(orderItem.getOrderId())) {
                foundPosition = i;
                break;
            }
        }

        if (foundPosition != -1) {
            final int position = foundPosition;

            OrderItem updatedOrderItem = new OrderItem(
                    orderItem.getOrderId(),
                    orderItem.getProductName(),
                    orderItem.getQuantity(),
                    orderItem.getTotalPrice(),
                    orderItem.getCustomerName(),
                    orderItem.getShippingAddress(),
                    orderItem.getPaymentMethod(),
                    newStatus,
                    orderItem.getDeliveryOption(),
                    orderItem.getImageUrl(),
                    orderItem.getSellerId(),
                    orderItem.getPaymentIntentId(),
                    orderItem.getProductId(),
                    orderItem.getBrand(),
                    orderItem.getProductYear(),
                    orderItem.getBuyerId()// ✅ new field added here
            );

            orderList.set(position, updatedOrderItem);
            adapter.notifyItemChanged(position);

            // Update Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("orders")
                    .document(orderItem.getOrderId())
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Order status updated to " + newStatus, Toast.LENGTH_SHORT).show();

                        // Send notification to the buyer
                        sendNotificationToBuyer(orderItem, newStatus);

                        // Subtract quantity when shipped or delivered
                        if (newStatus.equals("Shipping") || newStatus.equals("Delivered")) {
                            subtractProductQuantity(orderItem.getProductId(), orderItem.getQuantity());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update order status", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void sendNotificationToBuyer(OrderItem orderItem, String newStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create notification message
        String message = "Order status update: " + newStatus + " for " + orderItem.getProductName();
        String buyerId = orderItem.getBuyerId();  // Assuming buyerId is already part of OrderItem
        String sellerId = orderItem.getSellerId();
        String orderId = orderItem.getOrderId();  // Get order ID from the OrderItem
        String receiverId = buyerId; // In this case, the receiver is the buyer, but could be the seller if you want to notify both

        // Create a Notification object
        Notification notification = new Notification(message, buyerId, orderId, System.currentTimeMillis(), sellerId, receiverId);

        // Store the notification in Firestore
        db.collection("notifications")
                .document(buyerId) // Use buyerId as the document ID
                .collection("ordernotifications") // Use ordernotifications as the collection name
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Notification sent to buyer", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send notification", Toast.LENGTH_SHORT).show();
                });
    }

    private void subtractProductQuantity(String productId, long orderedQuantity) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collectionGroup("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getId().equals(productId)) {
                            Long currentQuantity = doc.getLong("quantity");

                            if (currentQuantity != null) {
                                long newQuantity = currentQuantity - orderedQuantity;
                                if (newQuantity < 0) newQuantity = 0;

                                doc.getReference().update("quantity", newQuantity)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Product quantity updated", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to update product quantity", Toast.LENGTH_SHORT).show();
                                        });
                            }
                            break; // Exit loop after finding the product
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch product data", Toast.LENGTH_SHORT).show();
                });
    }
}

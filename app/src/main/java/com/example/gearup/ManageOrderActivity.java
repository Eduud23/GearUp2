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
        final String[] statuses = {"Pending", "Shipping", "Ready to pickup", "Delivered"};

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String orderId = document.getId();
                        String productName = document.getString("product.productName");
                        Long quantityObj = document.getLong("product.productQuantity");
                        long quantity = quantityObj != null ? quantityObj : 0;

                        Double totalPriceObj = document.getDouble("product.totalPrice");
                        double totalPrice = totalPriceObj != null ? totalPriceObj : 0.0;

                        String customerName = document.getString("customerInfo.fullName");
                        String shippingAddress = document.getString("shippingAddress");
                        String paymentMethod = document.getString("product.paymentMethod");
                        String orderStatus = document.getString("status");
                        String imageUrl = document.getString("product.imageUrl");
                        String deliveryOption = document.getString("deliveryType");
                        String sellerId = document.getString("product.sellerId");
                        String paymentIntentId = document.getString("product.paymentIntentId");
                        String productId = document.getString("product.productId");
                        String brand = document.getString("product.productBrand");
                        String productYear = document.getString("product.productYear"); // ✅ new line

                        // Pass productYear to OrderItem constructor
                        OrderItem orderItem = new OrderItem(orderId, productName, quantity, totalPrice,
                                customerName, shippingAddress, paymentMethod, orderStatus, deliveryOption, imageUrl,
                                sellerId, paymentIntentId, productId, brand, productYear); // ✅ updated constructor

                        orderList.add(orderItem);
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
        final String[] statuses = {"Pending", "Shipping", "Ready to pickup", "Delivered"};

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
                    orderItem.getProductYear() // ✅ new field added here
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

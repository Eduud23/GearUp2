package com.example.gearup;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private ManageOrderAdapter manageOrderAdapter;
    private List<OrderItem> orderItems;
    private FirebaseFirestore db;
    private ListenerRegistration orderListenerRegistration;
    private String currentUserId;
    private Spinner spinnerOrderStatus;
    private String selectedOrderStatus = "Pending"; // Default status

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
            fetchOrders(selectedOrderStatus); // Fetch orders for the default status
        } else {
            Log.e("ManageOrder", "User not logged in.");
            finish();
        }

        // Set up the spinner
        spinnerOrderStatus = findViewById(R.id.spinner_order_status);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.order_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrderStatus.setAdapter(adapter);

        // Listen for changes in the spinner selection
        spinnerOrderStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedOrderStatus = parentView.getItemAtPosition(position).toString();

                // If "Pending" is selected, fetch both Pending and Rejected orders
                if ("Pending".equals(selectedOrderStatus)) {
                    fetchOrdersForPendingAndRejected(); // Fetch Pending and Rejected orders
                } else if ("Shipping/Ready to Pickup".equals(selectedOrderStatus)) {
                    fetchOrdersForShippingReadyToPickup(); // Handle the specific filtering for Shipping/Ready to Pickup
                } else {
                    fetchOrders(selectedOrderStatus); // Handle normal filtering
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }

    private void fetchOrders(String status) {
        // Clear the existing list
        orderItems.clear();

        // Fetch orders from Firestore based on the selected status
        orderListenerRegistration = db.collection("orders")
                .whereEqualTo("sellerId", currentUserId)
                .whereEqualTo("orderStatus", status) // Filter by order status
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ManageOrder", "Error fetching orders", error);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : value) {
                        OrderItem orderItem = doc.toObject(OrderItem.class);
                        orderItem.setDocumentId(doc.getId()); // Set the document ID
                        orderItems.add(orderItem);
                    }

                    manageOrderAdapter.notifyDataSetChanged();
                });
    }

    private void fetchOrdersForPendingAndRejected() {
        // Clear the existing list
        orderItems.clear();

        // Fetch orders where the status is either "Pending" or "Rejected"
        orderListenerRegistration = db.collection("orders")
                .whereEqualTo("sellerId", currentUserId)
                .whereIn("orderStatus", List.of("Pending", "Rejected")) // Fetch both Pending and Rejected
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ManageOrder", "Error fetching orders", error);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : value) {
                        OrderItem orderItem = doc.toObject(OrderItem.class);
                        orderItem.setDocumentId(doc.getId()); // Set the document ID
                        orderItems.add(orderItem);
                    }

                    manageOrderAdapter.notifyDataSetChanged();
                });
    }

    private void fetchOrdersForShippingReadyToPickup() {
        // Clear the existing list
        orderItems.clear();

        // Fetch orders where the shipping method is either "Pick-Up" or "Delivery"
        orderListenerRegistration = db.collection("orders")
                .whereEqualTo("sellerId", currentUserId)
                .whereIn("orderStatus", List.of("Ready to Pick Up", "Shipping")) // Filter for either "Pick-Up" or "Shipping"
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ManageOrder", "Error fetching orders", error);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : value) {
                        OrderItem orderItem = doc.toObject(OrderItem.class);
                        orderItem.setDocumentId(doc.getId()); // Set the document ID
                        orderItems.add(orderItem);
                    }

                    manageOrderAdapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListenerRegistration != null) {
            orderListenerRegistration.remove();
        }
    }

    public void showOrderDetailsDialog(final OrderItem orderItem) {
        // Inflate the dialog view
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_order_details, null);

        // Get the views from the dialog
        TextView tvProductName = dialogView.findViewById(R.id.tvProductName);
        TextView tvProductPrice = dialogView.findViewById(R.id.tvProductPrice);
        TextView tvProductQuantity = dialogView.findViewById(R.id.tvProductQuantity);
        TextView tvOrderStatus = dialogView.findViewById(R.id.tvOrderStatus);
        TextView tvShippingMethod = dialogView.findViewById(R.id.tvShippingMethod); // New text view for shipping method
        ImageView ivProductImage = dialogView.findViewById(R.id.ivProductImage); // New image view for product image

        Button btnApprove = dialogView.findViewById(R.id.btn_approve);  // Existing button ID
        Button btnReject = dialogView.findViewById(R.id.btn_reject);    // Existing button ID

        // Set the product information to the views
        tvProductName.setText(orderItem.getProductName());
        tvProductPrice.setText("₱" + orderItem.getProductPrice());
        tvProductQuantity.setText("Quantity: " + orderItem.getProductQuantity());
        tvOrderStatus.setText("Status: " + orderItem.getOrderStatus());
        tvShippingMethod.setText("Shipping Method: " + orderItem.getShippingMethod()); // Set shipping method

        // Load product image using Glide
        String imageUrl = orderItem.getProductImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(ivProductImage); // Set the product image using Glide
        }

        // Check if the order is "Rejected" and change color to red
        if ("Rejected".equals(orderItem.getOrderStatus())) {
            tvOrderStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Details")
                .setView(dialogView)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        String shippingMethod = orderItem.getShippingMethod();

        // Only show approve and reject buttons for orders with status "Pending"
        if ("Pending".equals(orderItem.getOrderStatus())) {
            btnApprove.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);

            btnApprove.setText("Approve");
            btnReject.setText("Reject");

            // Handle the "Approve" button click (change status to "Approved")
            btnApprove.setOnClickListener(v -> {
                updateOrderStatus(orderItem, "Approved");
                createSalesRecord(orderItem, "Approved");

                dialog.dismiss(); // Close dialog after approving
            });

            // Handle the "Reject" button click (change status to "Rejected")
            btnReject.setOnClickListener(v -> {
                updateOrderStatus(orderItem, "Rejected");
                dialog.dismiss(); // Close dialog after rejecting
            });
        } else {
            btnApprove.setVisibility(View.GONE);  // Hide the approve button
            btnReject.setVisibility(View.GONE);   // Hide the reject button
        }

        // If the order is approved, check the shipping method
        if ("Approved".equals(orderItem.getOrderStatus())) {
            // Make sure the buttons are visible
            btnApprove.setVisibility(View.VISIBLE);  // This button will be used to either "Ship" or "Ready to Pick Up"
            btnReject.setVisibility(View.VISIBLE);   // We will hide this button if the shipping method is "Delivery"

            if ("Pick-Up".equals(shippingMethod)) {
                // For "Pick-Up" shipping method: Show "Ready to Pick Up" button
                btnApprove.setText("Ready to Pick Up");
                btnReject.setVisibility(View.GONE);  // Hide the reject button (not needed for Pick-Up)

                // Handle the "Ready to Pick Up" button click
                btnApprove.setOnClickListener(v -> {
                    updateOrderStatus(orderItem, "Ready to Pick Up");
                    dialog.dismiss(); // Close dialog after action
                });
            } else if ("Delivery".equals(shippingMethod)) {
                // For "Delivery" shipping method: Show "Ship" button
                btnApprove.setText("Ship");
                btnReject.setVisibility(View.GONE);  // Hide the reject button (not needed for Delivery)

                // Handle the "Ship" button click
                btnApprove.setOnClickListener(v -> {
                    updateOrderStatus(orderItem, "Shipping");
                    dialog.dismiss(); // Close dialog after action
                });
            } else {
            }
        }
    }

    private void createSalesRecord(OrderItem orderItem, String status) {
        // Get product details from orderItem
        String productName = orderItem.getProductName();
        String productBrand = orderItem.getProductBrand();
        String productYearModel = orderItem.getProductYearModel();
        int productQuantity = orderItem.getProductQuantity();
        String sellerId = orderItem.getSellerId();
        String buyerId = orderItem.getBuyerId();
        double productPrice = orderItem.getProductPrice();

        // Fetch seller details from Firestore
        db.collection("sellers")
                .document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String shopName = documentSnapshot.getString("shopName");
                        String address = documentSnapshot.getString("address");

                        // Create a new Sales document in Firestore with the seller details
                        long timestamp = System.currentTimeMillis();
                        SalesRecord salesRecord = new SalesRecord(
                                productName,
                                productBrand,
                                productYearModel,
                                productQuantity,
                                timestamp,
                                sellerId,
                                buyerId,
                                address,
                                shopName,
                                productPrice
                        );

                        // Add the sales record to Firestore
                        db.collection("sales")
                                .add(salesRecord)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("ManageOrder", "Sales record created for product: " + productName);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ManageOrder", "Error creating sales record", e);
                                });
                    } else {
                        Log.e("ManageOrder", "Seller document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ManageOrder", "Error fetching seller details", e);
                });
    }


    private void updateOrderStatus(OrderItem orderItem, String status) {
        if (orderItem.getDocumentId() == null) {
            Log.e("ManageOrder", "Document ID is null. Cannot update order status.");
            return;
        }

        // Update order status in Firestore
        db.collection("orders")
                .document(orderItem.getDocumentId())  // Use the document ID from the order item
                .update("orderStatus", status)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ManageOrder", "Order status updated to " + status);
                    Toast.makeText(ManageOrderActivity.this, "Order " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("ManageOrder", "Error updating order status", e);
                    Toast.makeText(ManageOrderActivity.this, "Failed to update order status", Toast.LENGTH_SHORT).show();
                });
    }

}
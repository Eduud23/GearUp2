package com.example.gearup;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
                fetchOrders(selectedOrderStatus); // Fetch orders based on selected status
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
        Button btnApprove = dialogView.findViewById(R.id.btn_approve);
        Button btnReject = dialogView.findViewById(R.id.btn_reject);

        // Set the product information to the views
        tvProductName.setText(orderItem.getProductName());
        tvProductPrice.setText("₱" + orderItem.getProductPrice());
        tvProductQuantity.setText("Quantity: " + orderItem.getProductQuantity());
        tvOrderStatus.setText("Status: " + orderItem.getOrderStatus());

        // Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Details")
                .setView(dialogView)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle approve action
        btnApprove.setOnClickListener(v -> {
            updateOrderStatus(orderItem, "Approved");
            dialog.dismiss(); // Close dialog after approving
        });

        // Handle reject action
        btnReject.setOnClickListener(v -> {
            updateOrderStatus(orderItem, "Rejected");
            dialog.dismiss(); // Close dialog after rejecting
        });
    }

    // Update the order status in Firestore
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

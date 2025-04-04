package com.example.gearup;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
    private Spinner spinnerOrderStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_order);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();
        adapter = new ManageOrderAdapter(filteredOrderList, this);
        recyclerView.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Spinner for Order Status
        spinnerOrderStatus = findViewById(R.id.spinner_order_status);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.order_status_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrderStatus.setAdapter(spinnerAdapter);

        // Set a listener on the Spinner
        spinnerOrderStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedStatus = parentView.getItemAtPosition(position).toString();
                filterOrdersByStatus(selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing or you can reset the list if needed
            }
        });

        // Fetch orders from Firestore
        fetchOrdersFromFirestore();
    }

    private void fetchOrdersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String orderId = document.getId();
                        String productName = document.getString("prouduct.productName");
                        Long quantity = document.getLong("product.quantity");
                        double totalPrice = document.getDouble("product.totalPrice");
                        String customerName = document.getString("customerInfo.fullName");
                        String shippingAddress = document.getString("shippingAddress");
                        String paymentMethod = document.getString("payment.cardType");
                        String orderStatus = document.getString("status");
                        String imageUrl = document.getString("product.imageUrl");

                        // Create an OrderItem with the image URL
                        OrderItem orderItem = new OrderItem(orderId, productName, quantity, totalPrice,
                                customerName, shippingAddress, paymentMethod, orderStatus, imageUrl);
                        orderList.add(orderItem);
                    }
                    adapter.notifyDataSetChanged();

                    // Optionally, filter by default status (e.g., "Pending")
                    filterOrdersByStatus("Pending");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterOrdersByStatus(String status) {
        filteredOrderList.clear();

        for (OrderItem orderItem : orderList) {
            if (orderItem.getOrderStatus().equalsIgnoreCase(status) || status.equals("Default")) {
                filteredOrderList.add(orderItem);
            }
        }

        adapter.notifyDataSetChanged();
    }


    @Override
    public void onStatusUpdate(OrderItem orderItem) {
        // Create an array of possible status options
        final String[] statuses = {"Pending", "Shipping", "Ready to pickup", "Delivered"};

        // Create an AlertDialog to choose a new status
        new AlertDialog.Builder(this)
                .setTitle("Select Order Status")
                .setItems(statuses, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the selected status from the list
                        String newStatus = statuses[which];

                        // Update the order status and refresh the RecyclerView
                        updateOrderStatus(orderItem, newStatus);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateOrderStatus(OrderItem orderItem, String newStatus) {
        // Find the position of the orderItem in the list
        int position = -1;
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderId().equals(orderItem.getOrderId())) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            // Create a new OrderItem with the updated status
            OrderItem updatedOrderItem = new OrderItem(
                    orderItem.getOrderId(),
                    orderItem.getProductName(),
                    orderItem.getQuantity(),
                    orderItem.getTotalPrice(),
                    orderItem.getCustomerName(),
                    orderItem.getShippingAddress(),
                    orderItem.getPaymentMethod(),
                    newStatus,
                    orderItem.getImageUrl() // Keep the image URL unchanged
            );

            // Update the order in the list
            orderList.set(position, updatedOrderItem);
            adapter.notifyItemChanged(position); // Notify the adapter to refresh the item

            // Update the status in Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("orders")
                    .document(orderItem.getOrderId())
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Order status updated to " + newStatus, Toast.LENGTH_SHORT).show();

                        // After status update, refresh the RecyclerView by re-fetching orders if needed
                        refreshRecyclerView();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update order status", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void refreshRecyclerView() {
        // Re-fetch the orders to ensure we have the latest data
        fetchOrdersFromFirestore();
    }


}
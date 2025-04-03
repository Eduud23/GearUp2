package com.example.gearup;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_order);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        adapter = new ManageOrderAdapter(orderList, this);
        recyclerView.setAdapter(adapter);

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
                        String productName = document.getString("productName");
                        String quantity = String.valueOf(document.getLong("quantity"));
                        double totalPrice = document.getDouble("totalPrice");
                        String customerName = document.getString("customerInfo.fullName");
                        String shippingAddress = document.getString("shippingAddress");
                        String paymentMethod = document.getString("payment.cardType");
                        String orderStatus = document.getString("status");
                        String imageUrl = document.getString("imageUrl");  // Fetch the image URL

                        // Create an OrderItem with the image URL
                        OrderItem orderItem = new OrderItem(orderId, productName, quantity, totalPrice,
                                customerName, shippingAddress, paymentMethod, orderStatus, imageUrl);
                        orderList.add(orderItem);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onStatusUpdate(OrderItem orderItem) {
        // Create a new status based on the current order status
        String newStatus = orderItem.getOrderStatus().equals("Pending") ? "Shipped" : "Delivered";

        // Find the position of the orderItem in the list
        int position = -1;
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderId().equals(orderItem.getOrderId())) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            // Update the order status in the list (create a new OrderItem)
            OrderItem updatedOrderItem = new OrderItem(
                    orderItem.getOrderId(),
                    orderItem.getProductName(),
                    orderItem.getQuantity(),
                    orderItem.getTotalPrice(),
                    orderItem.getCustomerName(),
                    orderItem.getShippingAddress(),
                    orderItem.getPaymentMethod(),
                    newStatus,
                    orderItem.getImageUrl()  // Keep the image URL unchanged
            );

            // Update the order in the list
            orderList.set(position, updatedOrderItem);
            adapter.notifyItemChanged(position); // Notify the adapter of the update

            // Update the status in Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("orders")
                    .document(orderItem.getOrderId())
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Order status updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update order status", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}

package com.example.gearup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ManageOrderAdapter extends RecyclerView.Adapter<ManageOrderAdapter.ViewHolder> {
    private final List<OrderItem> orderItems;
    private final Context context;

    public ManageOrderAdapter(List<OrderItem> orderItems, Context context) {
        this.orderItems = orderItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem orderItem = orderItems.get(position);
        holder.tvProductName.setText(orderItem.getProductName());
        holder.tvProductPrice.setText(String.format("₱%.2f", orderItem.getProductPrice()));
        holder.tvProductQuantity.setText("Quantity: " + orderItem.getProductQuantity());
        holder.tvOrderStatus.setText("Status: " + orderItem.getOrderStatus());

        holder.btnApprove.setOnClickListener(v -> updateOrderStatus(orderItem, "Approved"));
        holder.btnReady.setOnClickListener(v -> updateOrderStatus(orderItem, "Ready for Delivery"));
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductQuantity, tvOrderStatus;
        Button btnApprove, btnReady;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_order_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_order_product_price);
            tvProductQuantity = itemView.findViewById(R.id.tv_order_product_quantity);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReady = itemView.findViewById(R.id.btn_ready);
        }
    }

    private void updateOrderStatus(OrderItem orderItem, String newStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("orders").document(orderItem.getDocumentId()) // Make sure to store the document ID in OrderItem
                .update("orderStatus", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Optionally notify user of success
                })
                .addOnFailureListener(e -> {
                    // Optionally notify user of failure
                });
    }
}

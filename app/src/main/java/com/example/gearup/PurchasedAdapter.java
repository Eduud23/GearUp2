package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PurchasedAdapter extends RecyclerView.Adapter<PurchasedAdapter.ViewHolder> {
    private final List<OrderItem> purchasedItems;

    public PurchasedAdapter(List<OrderItem> purchasedItems) {
        this.purchasedItems = purchasedItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchased, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem orderItem = purchasedItems.get(position);
        holder.tvProductName.setText(orderItem.getProductName());
        holder.tvProductPrice.setText(String.format("₱%.2f", orderItem.getProductPrice()));
        holder.tvProductQuantity.setText("Quantity: " + orderItem.getProductQuantity());

        // Load the product image using Glide
        Glide.with(holder.itemView.getContext())
                .load(orderItem.getProductImageUrl())
                .into(holder.ivProductImage);

        // Set the order status
        holder.tvOrderStatus.setText("Status: " + orderItem.getOrderStatus());
    }

    @Override
    public int getItemCount() {
        return purchasedItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductQuantity, tvOrderStatus; // Added order status
        ImageView ivProductImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvProductQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status); // Initialize the order status TextView
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
        }
    }
}

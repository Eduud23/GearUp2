package com.example.gearup;

import android.content.Context;
import android.content.Intent;
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
    private final Context context;

    // Pass context and the list of purchased items to the constructor
    public PurchasedAdapter(Context context, List<OrderItem> purchasedItems) {
        this.context = context;
        this.purchasedItems = purchasedItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(context).inflate(R.layout.item_purchased, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem orderItem = purchasedItems.get(position);

        // Set the product name, quantity, and price
        holder.tvProductName.setText(orderItem.getProductName());
        holder.tvProductQuantity.setText("Quantity: " + orderItem.getQuantity());
        holder.tvProductPrice.setText(String.format("Total Price: ₱%.2f", orderItem.getTotalPrice()));

        // Load the product image using Glide
        Glide.with(holder.itemView.getContext())
                .load(orderItem.getImageUrl())
                .into(holder.ivProductImage);

        // Set the order status
        holder.tvOrderStatus.setText("Status: " + orderItem.getOrderStatus());

        // Set the item click listener to navigate to the OrderDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailsActivity.class);
            intent.putExtra("orderId", orderItem.getOrderId());
            intent.putExtra("productName", orderItem.getProductName());
            intent.putExtra("productQuantity", orderItem.getQuantity());
            intent.putExtra("productPrice", orderItem.getTotalPrice());
            intent.putExtra("customerName", orderItem.getCustomerName());
            intent.putExtra("shippingAddress", orderItem.getShippingAddress());
            intent.putExtra("deliveryOption", orderItem.getDeliveryOption());
            intent.putExtra("orderStatus", orderItem.getOrderStatus());
            intent.putExtra("imageUrl", orderItem.getImageUrl());
            intent.putExtra("sellerId", orderItem.getSellerId());
            intent.putExtra("paymentIntentId", orderItem.getPaymentIntentId());
            intent.putExtra("productId", orderItem.getProductId());
            intent.putExtra("productBrand", orderItem.getBrand());
            intent.putExtra("productYear", orderItem.getProductYear());
            intent.putExtra("customerFullName", orderItem.getCustomerName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return purchasedItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductQuantity, tvOrderStatus;
        ImageView ivProductImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvProductQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
        }
    }
}

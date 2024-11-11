package com.example.gearup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ManageOrderAdapter extends RecyclerView.Adapter<ManageOrderAdapter.ViewHolder> {
    private List<OrderItem> orderItems;
    private Context context;

    // Constructor
    public ManageOrderAdapter(List<OrderItem> orderItems, Context context) {
        this.orderItems = orderItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_order, parent, false);  // Inflate the layout
        return new ViewHolder(itemView);  // Return the ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the order item for the current position
        OrderItem orderItem = orderItems.get(position);

        // Set the data into the views
        holder.tvOrderProductName.setText(orderItem.getProductName());
        holder.tvOrderProductPrice.setText("₱" + orderItem.getProductPrice());
        holder.tvOrderProductQuantity.setText("Quantity: " + orderItem.getProductQuantity());
        holder.tvOrderStatus.setText("Status: " + orderItem.getOrderStatus());
        holder.tvShippingMethod.setText("Shipping Method: " + orderItem.getShippingMethod());

        // Load the product image using Glide
        String imageUrl = orderItem.getProductImageUrl();  // Assuming you have the image URL in the OrderItem class
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.ivProductImage);  // Set image into the ImageView
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_launcher_foreground);  // Use a placeholder image if no URL is available
        }

        // Set click listener to show order details when clicked
        holder.itemView.setOnClickListener(v -> {
            // Assuming ManageOrderActivity has a method to show order details
            ((ManageOrderActivity) context).showOrderDetailsDialog(orderItem);
        });
    }

    @Override
    public int getItemCount() {
        return orderItems.size();  // Return the size of the order items list
    }

    // ViewHolder class for binding views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Declare TextViews
        TextView tvOrderProductName;
        TextView tvOrderProductPrice;
        TextView tvOrderProductQuantity;
        TextView tvOrderStatus;
        TextView tvShippingMethod;  // New TextView for shipping method
        ImageView ivProductImage;   // New ImageView for product image

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Bind the views
            tvOrderProductName = itemView.findViewById(R.id.tv_order_product_name);
            tvOrderProductPrice = itemView.findViewById(R.id.tv_order_product_price);
            tvOrderProductQuantity = itemView.findViewById(R.id.tv_order_product_quantity);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvShippingMethod = itemView.findViewById(R.id.tv_shipping_method);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
        }
    }
}

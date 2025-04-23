package com.example.gearup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ManageOrderAdapter extends RecyclerView.Adapter<ManageOrderAdapter.OrderViewHolder> {

    private List<OrderItem> orderList;
    private OnStatusUpdateListener statusUpdateListener;
    private Context context;

    public ManageOrderAdapter(List<OrderItem> orderList, OnStatusUpdateListener statusUpdateListener, Context context) {
        this.orderList = orderList;
        this.statusUpdateListener = statusUpdateListener;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_order, parent, false);
        return new OrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderItem order = orderList.get(position);

        holder.productName.setText("Product Name: " + order.getProductName());
        holder.quantity.setText("Quantity: " + order.getQuantity());
        holder.totalPrice.setText("₱" + order.getTotalPrice());
        holder.orderStatus.setText("Status: " + order.getOrderStatus());
        holder.deliveryType.setText("Delivery Type: " + order.getDeliveryOption());

        // Load the product image using Glide
        Glide.with(holder.productImageView.getContext())
                .load(order.getImageUrl())  // The image URL from the OrderItem object
                .into(holder.productImageView);  // Load the image into the ImageView

        // Handle item click to open the ManageOrderDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ManageOrderDetailActivity.class);
            // Pass all necessary order details
            intent.putExtra("orderId", order.getOrderId());
            intent.putExtra("productName", order.getProductName());
            intent.putExtra("productQuantity", order.getQuantity());
            intent.putExtra("totalPrice", order.getTotalPrice());
            intent.putExtra("orderStatus", order.getOrderStatus());
            intent.putExtra("deliveryOption", order.getDeliveryOption());
            intent.putExtra("imageUrl", order.getImageUrl());
            intent.putExtra("customerName", order.getCustomerName());
            intent.putExtra("shippingAddress", order.getShippingAddress());
            intent.putExtra("paymentMethod", order.getPaymentMethod());
            intent.putExtra("sellerId", order.getSellerId());
            intent.putExtra("paymentIntentId", order.getPaymentIntentId());
            intent.putExtra("productId", order.getProductId());
            intent.putExtra("productBrand", order.getBrand());
            intent.putExtra("productYear", order.getProductYear());

            // Start the activity
            context.startActivity(intent);
        });

        // Handle status update button click
        holder.updateStatusButton.setOnClickListener(v -> {
            if (statusUpdateListener != null) {
                statusUpdateListener.onStatusUpdate(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, quantity, totalPrice, orderStatus, deliveryType;
        Button updateStatusButton;
        ImageView productImageView;

        public OrderViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            quantity = itemView.findViewById(R.id.product_quantity);
            totalPrice = itemView.findViewById(R.id.product_price);
            deliveryType = itemView.findViewById(R.id.delivery_type);
            orderStatus = itemView.findViewById(R.id.order_status);
            updateStatusButton = itemView.findViewById(R.id.update_status_button);
            productImageView = itemView.findViewById(R.id.product_image);
        }
    }

    public interface OnStatusUpdateListener {
        void onStatusUpdate(OrderItem orderItem);
    }
}

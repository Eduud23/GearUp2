package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ManageOrderAdapter extends RecyclerView.Adapter<ManageOrderAdapter.OrderViewHolder> {

    private List<OrderItem> orderList;
    private OnStatusUpdateListener statusUpdateListener;

    public ManageOrderAdapter(List<OrderItem> orderList, OnStatusUpdateListener statusUpdateListener) {
        this.orderList = orderList;
        this.statusUpdateListener = statusUpdateListener;
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

        holder.productName.setText(order.getProductName());
        holder.quantity.setText("Quantity: " + order.getQuantity());
        holder.totalPrice.setText("₱" + order.getTotalPrice());
        holder.customerName.setText("Customer: " + order.getCustomerName());
        holder.shippingAddress.setText("Address: " + order.getShippingAddress());
        holder.paymentMethod.setText("Payment: " + order.getPaymentMethod());
        holder.orderStatus.setText("Status: " + order.getOrderStatus());

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
        TextView productName, quantity, totalPrice, customerName, shippingAddress, paymentMethod, orderStatus;
        Button updateStatusButton;

        public OrderViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            quantity = itemView.findViewById(R.id.product_quantity);
            totalPrice = itemView.findViewById(R.id.product_price);
            customerName = itemView.findViewById(R.id.customer_name);
            shippingAddress = itemView.findViewById(R.id.customer_address);
            paymentMethod = itemView.findViewById(R.id.payment_method);
            orderStatus = itemView.findViewById(R.id.order_status);
            updateStatusButton = itemView.findViewById(R.id.update_status_button);
        }
    }

    public interface OnStatusUpdateListener {
        void onStatusUpdate(OrderItem orderItem);
    }
}

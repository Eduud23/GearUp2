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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnItemClickListener listener;

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(CartItem cartItem);
    }

    public CartAdapter(List<CartItem> cartItems, OnItemClickListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        Product product = cartItem.getProduct();

        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(String.format("₱%.2f", product.getPrice()));
        holder.tvProductQuantity.setText("Quantity: " + cartItem.getQuantity());

        // Load product image using Glide
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrls.get(0)) // Load the first image
                    .into(holder.ivProductImage);
        } else {
            // Optionally set a placeholder or a default image
            holder.ivProductImage.setImageResource(R.drawable.ic_launcher_foreground); // Use a placeholder image
        }

        // Set the click listener for the item
        holder.itemView.setOnClickListener(v -> listener.onItemClick(cartItem));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductQuantity;
        ImageView ivProductImage;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvProductQuantity = itemView.findViewById(R.id.tv_product_quantity);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
        }
    }
}

package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnItemClickListener listener;
    private List<CartItem> selectedItems; // List to keep track of selected items
    private RemoveItemListener removeItemListener; // Callback interface

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(CartItem cartItem);
    }

    // Interface for removing item
    public interface RemoveItemListener {
        void onItemLongPress(CartItem cartItem); // Method that will be called for long press
    }

    public CartAdapter(List<CartItem> cartItems, OnItemClickListener listener, RemoveItemListener removeItemListener) {
        this.cartItems = cartItems;
        this.listener = listener;
        this.selectedItems = new ArrayList<>();
        this.removeItemListener = removeItemListener; // Initialize callback
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

        // Format and calculate the total price of the item
        double totalPrice = product.getPrice() * cartItem.getQuantity();
        String formattedPrice = formatPrice(totalPrice);

        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText("₱" + formattedPrice);
        holder.tvProductQuantity.setText("Quantity: " + cartItem.getQuantity());

        // Load product image
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrls.get(0))
                    .into(holder.ivProductImage);
        }

        // Set checkbox listener
        holder.checkbox.setChecked(selectedItems.contains(cartItem));
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedItems.add(cartItem);
            } else {
                selectedItems.remove(cartItem);
            }
            listener.onItemClick(cartItem);
        });

        // Long press listener to trigger the remove item dialog
        holder.itemView.setOnLongClickListener(v -> {
            // Call the callback method in the fragment
            removeItemListener.onItemLongPress(cartItem);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public List<CartItem> getSelectedItems() {
        return selectedItems;
    }

    // Helper method to format price with commas
    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price);
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductQuantity;
        ImageView ivProductImage;
        CheckBox checkbox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvProductQuantity = itemView.findViewById(R.id.tv_product_quantity);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            checkbox = itemView.findViewById(R.id.checkbox_cart_item);
        }
    }
}

package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.ViewHolder> {

    private List<CartItem> checkoutItems;

    public CheckoutAdapter(List<CartItem> checkoutItems) {
        this.checkoutItems = checkoutItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = checkoutItems.get(position);

        // Access CartItem fields directly
        String productName = cartItem.getProductName();
        int quantity = cartItem.getQuantity();
        double totalPrice = cartItem.getTotalPrice();  // This is already total price (price * quantity)
        String formattedPrice = formatPrice(totalPrice);
        String imageUrl = cartItem.getImageUrl();  // Directly using the image URL from CartItem
        String brand = cartItem.getBrand();  // If you have a brand field in CartItem, use it here (e.g., cartItem.getBrand())
        String yearModel = cartItem.getYearModel();  // If you have a year model field, use it here as well (e.g., cartItem.getYearModel())

        // Set data to the views
        holder.productNameTextView.setText(productName);
        holder.productQuantityTextView.setText("Quantity: " + quantity);
        holder.productPriceTextView.setText("₱" + formattedPrice);
        holder.productBrandTextView.setText("Brand: " + brand);  // You can update this if your CartItem has a brand
        holder.productYearModelTextView.setText("Year: " + yearModel);  // Similarly, update this for year model

        // Load product image using Glide (from CartItem's imageUrl)
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.productImageView.getContext())
                    .load(imageUrl)
                    .into(holder.productImageView);
        }
    }

    @Override
    public int getItemCount() {
        return checkoutItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView, productQuantityTextView, productPriceTextView, productBrandTextView, productYearModelTextView;
        ImageView productImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.textView_checkout_product_name);
            productQuantityTextView = itemView.findViewById(R.id.textView_checkout_quantity);
            productPriceTextView = itemView.findViewById(R.id.textView_checkout_price);
            productBrandTextView = itemView.findViewById(R.id.textView_checkout_brand);
            productYearModelTextView = itemView.findViewById(R.id.textView_checkout_year);
            productImageView = itemView.findViewById(R.id.imageView_checkout_product);
        }
    }

    // Helper method to format price with commas
    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price);
    }
}

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
        Product product = cartItem.getProduct();

        holder.productNameTextView.setText(product.getName());
        holder.productQuantityTextView.setText("Quantity: " + cartItem.getQuantity());
        holder.productPriceTextView.setText("₱" + formatPrice(cartItem.getTotalPrice()));

        // Load the product image using Glide
        if (!product.getImageUrls().isEmpty()) {
            Glide.with(holder.productImageView.getContext())
                    .load(product.getImageUrls().get(0))
                    .into(holder.productImageView);
        }
    }

    @Override
    public int getItemCount() {
        return checkoutItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView, productQuantityTextView, productPriceTextView;
        ImageView productImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.textView_checkout_product_name);
            productQuantityTextView = itemView.findViewById(R.id.textView_checkout_quantity);
            productPriceTextView = itemView.findViewById(R.id.textView_checkout_price);
            productImageView = itemView.findViewById(R.id.imageView_checkout_product);
        }
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price);
    }
}

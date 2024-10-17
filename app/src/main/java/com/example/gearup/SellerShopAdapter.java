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

public class SellerShopAdapter extends RecyclerView.Adapter<SellerShopAdapter.ViewHolder> {
    private List<Product> productList;
    private final OnProductClickListener listener;

    public SellerShopAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_seller, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productNameTextView.setText(product.getName());
        holder.productPriceTextView.setText(String.format("₱%.2f", product.getPrice()));
        holder.productDescriptionTextView.setText(product.getDescription());

        // Load the product image using Glide
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrls.get(0))
                    .into(holder.productImageView);
        } else {
            holder.productImageView.setImageResource(R.drawable.ic_launcher_foreground); // Fallback image
        }

        // Set click listener for the product
        holder.itemView.setOnClickListener(v -> listener.onProductClick(position, "All Products"));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView;
        TextView productPriceTextView;
        TextView productDescriptionTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.iv_product_image);
            productNameTextView = itemView.findViewById(R.id.tv_product_name);
            productPriceTextView = itemView.findViewById(R.id.tv_product_price);
            productDescriptionTextView = itemView.findViewById(R.id.tv_product_description);
        }
    }

    // Listener interface for handling product clicks
    public interface OnProductClickListener {
        void onProductClick(int position, String category);
    }
}

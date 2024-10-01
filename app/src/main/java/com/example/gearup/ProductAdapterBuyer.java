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

public class ProductAdapterBuyer extends RecyclerView.Adapter<ProductAdapterBuyer.ProductViewHolder> {
    private final List<Product> products;
    private final OnProductClickListener listener;

    public ProductAdapterBuyer(List<Product> products, OnProductClickListener listener) {
        if (products == null || listener == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        // Bind data for two products
        for (int i = 0; i < 2; i++) {
            int index = position * 2 + i;
            if (index < products.size()) {
                Product product = products.get(index);
                holder.productName[i].setText(product.getName());
                holder.productPrice[i].setText(String.format("₱%.2f", product.getPrice()));
                holder.productDescription[i].setText(product.getDescription());

                // Load product image
                List<String> imageUrls = product.getImageUrls(); // Get the list of image URLs
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    String imageUri = imageUrls.get(0); // Load the first image
                    Glide.with(holder.itemView.getContext())
                            .load(imageUri)
                            .into(holder.productImage[i]);
                } else {
                    holder.productImage[i].setImageResource(R.drawable.ic_launcher_foreground); // Use a placeholder image
                }

                // Load seller profile image
                String profileImageUrl = product.getSellerProfileImageUrl();
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                            .load(profileImageUrl)
                            .into(holder.sellerProfileImage[i]);
                } else {
                    holder.sellerProfileImage[i].setImageResource(R.drawable.ic_launcher_foreground); // Default image
                }

                // Show product details
                holder.productImage[i].setVisibility(View.VISIBLE);
                holder.productName[i].setVisibility(View.VISIBLE);
                holder.productPrice[i].setVisibility(View.VISIBLE);
                holder.productDescription[i].setVisibility(View.VISIBLE);
            } else {
                // Hide second product if there's no data
                holder.productImage[i].setVisibility(View.GONE);
                holder.productName[i].setVisibility(View.GONE);
                holder.productPrice[i].setVisibility(View.GONE);
                holder.productDescription[i].setVisibility(View.GONE);
            }
        }

        // Set click listeners for each item
        holder.itemView.setOnClickListener(v -> {
            if (position * 2 < products.size()) {
                listener.onProductClick(position * 2);
            }
            if (position * 2 + 1 < products.size()) {
                listener.onProductClick(position * 2 + 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (products.size() + 1) / 2; // Two products per item
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView[] productName = new TextView[2];
        TextView[] productPrice = new TextView[2];
        TextView[] productDescription = new TextView[2];
        ImageView[] productImage = new ImageView[2];
        ImageView[] sellerProfileImage = new ImageView[2];

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName[0] = itemView.findViewById(R.id.tv_product_name);
            productPrice[0] = itemView.findViewById(R.id.tv_product_price);
            productDescription[0] = itemView.findViewById(R.id.tv_product_description);
            productImage[0] = itemView.findViewById(R.id.iv_product_image);
            sellerProfileImage[0] = itemView.findViewById(R.id.civ_seller_profile_image);

            // Initialize second set of views
            productName[1] = itemView.findViewById(R.id.tv_product_name_2);
            productPrice[1] = itemView.findViewById(R.id.tv_product_price_2);
            productDescription[1] = itemView.findViewById(R.id.tv_product_description_2);
            productImage[1] = itemView.findViewById(R.id.iv_product_image_2);
            sellerProfileImage[1] = itemView.findViewById(R.id.civ_seller_profile_image_2);
        }
    }

    public interface OnProductClickListener {
        void onProductClick(int position);
    }
}

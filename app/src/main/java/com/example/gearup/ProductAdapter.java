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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> products;
    private final OnProductClickListener onProductClickListener;

    public ProductAdapter(List<Product> products, OnProductClickListener onProductClickListener) {
        if (products == null || onProductClickListener == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        this.products = products;
        this.onProductClickListener = onProductClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_seller, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        holder.productName.setText(product.getName());
        holder.productPrice.setText(String.format("₱%.2f", product.getPrice()));
        holder.productDescription.setText(product.getDescription());

        // Bind brand and year model
        holder.productBrand.setText(product.getBrand());
        holder.productYearModel.setText(product.getYearModel());

        // Load the first product image from the list of URLs
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrls.get(0))
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.ic_launcher_foreground); // Placeholder image
        }

        // Load seller profile image
        String profileImageUrl = product.getSellerProfileImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(profileImageUrl)
                    .into(holder.sellerProfileImage);
        } else {
            holder.sellerProfileImage.setImageResource(R.drawable.ic_launcher_foreground); // Default image
        }

        // Set click listener for each item
        holder.itemView.setOnClickListener(v -> onProductClickListener.onProductClick(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productDescription, productBrand, productYearModel;
        ImageView productImage, sellerProfileImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.tv_product_name);
            productPrice = itemView.findViewById(R.id.tv_product_price);
            productDescription = itemView.findViewById(R.id.tv_product_description);
            productBrand = itemView.findViewById(R.id.tv_product_brand); // New TextView for brand
            productYearModel = itemView.findViewById(R.id.tv_product_year_model); // New TextView for year model
            productImage = itemView.findViewById(R.id.iv_product_image);
            sellerProfileImage = itemView.findViewById(R.id.civ_seller_profile_image);
        }
    }

    public interface OnProductClickListener {
        void onProductClick(int position);
    }

    // New method to update the product list
    public void updateProducts(List<Product> newProducts) {
        products.clear();
        products.addAll(newProducts);
        notifyDataSetChanged();
    }
}

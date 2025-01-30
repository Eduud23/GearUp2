package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapterBuyer extends RecyclerView.Adapter<ProductAdapterBuyer.ProductViewHolder> {
    private List<Product> allProducts; // Full list of products
    private List<Product> displayedProducts; // Currently displayed products
    private final OnProductClickListener listener;
    private final String category;

    public ProductAdapterBuyer(List<Product> products, String category, OnProductClickListener listener) {
        if (products == null || listener == null || category == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        this.allProducts = new ArrayList<>(products); // Initialize full list
        this.displayedProducts = new ArrayList<>(products); // Start with all products displayed
        this.listener = listener;
        this.category = category;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        // Bind first product
        int firstProductIndex = position * 2;
        if (firstProductIndex < displayedProducts.size()) {
            Product firstProduct = displayedProducts.get(firstProductIndex);
            holder.productName[0].setText(firstProduct.getName());
            holder.productPrice[0].setText(String.format("₱%,.2f", firstProduct.getPrice()));
            holder.productDescription[0].setText(firstProduct.getDescription());

            // Load product image
            List<String> imageUrls = firstProduct.getImageUrls();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                String imageUri = imageUrls.get(0);
                Glide.with(holder.itemView.getContext())
                        .load(imageUri)
                        .into(holder.productImage[0]);
            } else {
                holder.productImage[0].setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Load seller profile image
            String profileImageUrl = firstProduct.getSellerProfileImageUrl();
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(profileImageUrl)
                        .into(holder.sellerProfileImage[0]);
            } else {
                holder.sellerProfileImage[0].setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Set click listener for the first product
            holder.productImage[0].setOnClickListener(v -> listener.onProductClick(firstProductIndex, category));
        }

        // Bind second product if exists
        int secondProductIndex = firstProductIndex + 1;
        if (secondProductIndex < displayedProducts.size()) {
            Product secondProduct = displayedProducts.get(secondProductIndex);
            holder.productName[1].setText(secondProduct.getName());
            holder.productPrice[1].setText(String.format("₱%,.2f", secondProduct.getPrice()));
            holder.productDescription[1].setText(secondProduct.getDescription());

            // Load product image
            List<String> imageUrls = secondProduct.getImageUrls();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                String imageUri = imageUrls.get(0);
                Glide.with(holder.itemView.getContext())
                        .load(imageUri)
                        .into(holder.productImage[1]);
            } else {
                holder.productImage[1].setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Load seller profile image
            String profileImageUrl = secondProduct.getSellerProfileImageUrl();
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(profileImageUrl)
                        .into(holder.sellerProfileImage[1]);
            } else {
                holder.sellerProfileImage[1].setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Set click listener for the second product
            holder.productImage[1].setOnClickListener(v -> listener.onProductClick(secondProductIndex, category));
        } else {
            // Hide the second product view if there isn't one
            holder.productImage[1].setVisibility(View.GONE);
            holder.productName[1].setVisibility(View.GONE);
            holder.productPrice[1].setVisibility(View.GONE);
            holder.productDescription[1].setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return (displayedProducts.size() + 1) / 2; // Two products per row
    }

    public void updateProductList(List<Product> newProducts) {
        this.displayedProducts.clear();
        this.displayedProducts.addAll(newProducts);
        notifyDataSetChanged(); // Refresh the adapter with new product list
    }

    public void resetProductList() {
        this.displayedProducts.clear();
        this.displayedProducts.addAll(allProducts); // Restore to full list
        notifyDataSetChanged(); // Refresh the adapter with full product list
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
        void onProductClick(int position, String category);
    }
}
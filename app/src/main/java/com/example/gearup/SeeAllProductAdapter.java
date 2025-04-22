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

public class SeeAllProductAdapter extends RecyclerView.Adapter<SeeAllProductAdapter.ProductViewHolder> {
    private List<Product> displayedProducts; // Products currently displayed
    private final OnProductClickListener listener;

    // Constructor
    public SeeAllProductAdapter(List<Product> products, OnProductClickListener listener) {
        if (products == null || listener == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        this.displayedProducts = new ArrayList<>(products);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_seller, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = displayedProducts.get(position);
        holder.bind(product, position);
    }

    @Override
    public int getItemCount() {
        return displayedProducts.size();
    }

    // Method to update the product list and refresh the adapter
    public void updateProducts(List<Product> updatedProducts) {
        this.displayedProducts = updatedProducts;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    // Method to filter products by category
    public void filterByCategory(String category) {
        List<Product> filteredProducts = new ArrayList<>();
        for (Product product : displayedProducts) {
            if (product.getCategory().equalsIgnoreCase(category)) {
                filteredProducts.add(product);
            }
        }
        this.displayedProducts.clear();
        this.displayedProducts.addAll(filteredProducts);
        notifyDataSetChanged();
    }

    // ViewHolder class to hold references to product views
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView productPrice;
        TextView productBrand;  // Brand TextView
        ImageView productImage;
        ImageView sellerProfileImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.tv_product_name);
            productPrice = itemView.findViewById(R.id.tv_product_price);
            productBrand = itemView.findViewById(R.id.tv_product_brand);  // Initialize the brand field
            productImage = itemView.findViewById(R.id.iv_product_image);
            sellerProfileImage = itemView.findViewById(R.id.civ_seller_profile_image);
        }

        public void bind(Product product, int position) {
            // Bind product name
            productName.setText(product.getName());

            // Bind product price
            productPrice.setText("Price: " + String.format("₱%.2f", product.getPrice()));

            // Bind product brand
            productBrand.setText("Brand: " + product.getBrand());

            // Load product image
            List<String> imageUrls = product.getImageUrls();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrls.get(0))
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Load seller profile image
            String profileImageUrl = product.getSellerProfileImageUrl();
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(profileImageUrl)
                        .into(sellerProfileImage);
            } else {
                sellerProfileImage.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Set click listener for the product
            itemView.setOnClickListener(v -> listener.onProductClick(position, "All Products"));
        }
    }

    // Listener interface for handling product clicks
    public interface OnProductClickListener {
        void onProductClick(int position, String category);
    }
}

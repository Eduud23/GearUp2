package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;  // For loading images from URLs
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SearchProductAdapter extends RecyclerView.Adapter<SearchProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private String category;
    private OnProductClickListener listener;

    public SearchProductAdapter(List<Product> productList, String category, OnProductClickListener listener) {
        this.productList = productList;
        this.category = category;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.productDescription.setText(product.getDescription());
        holder.productPrice.setText(String.format("₱%,.2f", product.getPrice()));

        // Use Glide to load the first image from the imageUrls list
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            // Load the first image from the list
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrls().get(0)) // Get the first image URL
                    .into(holder.productImage);
        }

        // Set click listener for each item if needed
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateProductList(List<Product> newProductList) {
        productList = newProductList;
        notifyDataSetChanged(); // Notify that the data has changed, which updates the UI
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productDescription, productPrice;
        ImageView productImage;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productDescription = itemView.findViewById(R.id.product_description);
            productPrice = itemView.findViewById(R.id.product_price);
            productImage = itemView.findViewById(R.id.product_image);
        }
    }

    // Interface for handling product click events (if you need to do something when a product is clicked)
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}

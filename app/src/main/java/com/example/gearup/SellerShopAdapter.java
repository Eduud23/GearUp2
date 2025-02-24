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

    // Constructor for the adapter, takes the product list and click listener
    public SellerShopAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the product item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seller_shop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the product at the current position
        Product product = productList.get(position);

        if (product != null) {
            // Set product name, price, and description
            holder.productNameTextView.setText(product.getName());
            holder.brandTextView.setText(product.getBrand());
            holder.yearModelTextView.setText(product.getYearModel());
            holder.productPriceTextView.setText(String.format("₱%.2f", product.getPrice()));
            holder.productDescriptionTextView.setText(product.getDescription());

            // Load the product image using Glide (First image from the list or fallback image)
            List<String> imageUrls = product.getImageUrls();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrls.get(0))  // Load the first image in the list
                        .into(holder.productImageView);
            } else {
                // Fallback image if no URL is available
                holder.productImageView.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Set the click listener for the item, passing the full Product object
            holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
    }
    public void updateList(List<Product> updatedList) {
        this.productList = updatedList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ViewHolder for the product items
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView;
        TextView brandTextView;
        TextView yearModelTextView;
        TextView productPriceTextView;
        TextView productDescriptionTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            // Initialize the views from the layout
            productImageView = itemView.findViewById(R.id.iv_product_image);
            productNameTextView = itemView.findViewById(R.id.tv_product_name);
            brandTextView = itemView.findViewById(R.id.tv_product_brand);
            yearModelTextView = itemView.findViewById(R.id.tv_product_year_model);
            productPriceTextView = itemView.findViewById(R.id.tv_product_price);
            productDescriptionTextView = itemView.findViewById(R.id.tv_product_description);
        }
    }

    // Listener interface for handling product clicks
    public interface OnProductClickListener {
        void onProductClick(Product product);  // Expect Product object
    }
}

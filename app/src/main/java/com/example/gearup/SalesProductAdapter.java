package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class SalesProductAdapter extends RecyclerView.Adapter<SalesProductAdapter.SalesProductViewHolder> {

    private List<ProductSalesItem> productList;


    // Constructor to initialize the product list
    public SalesProductAdapter(List<ProductSalesItem> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public SalesProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the product item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sales_product, parent, false);
        return new SalesProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalesProductViewHolder holder, int position) {
        ProductSalesItem product = productList.get(position);

        // Bind the product data to the views
        holder.tvProductName.setText(product.getProductName());
        holder.tvProductPrice.setText(String.format("₱%.2f", product.getProductPrice()));
        holder.tvProductQuantity.setText("Sold: " + product.getProductQuantity());
        holder.tvProductYearModel.setText("Model: " + product.getProductYearModel());

        // Load the product image into the ImageView using Glide with optimizations
        String productImageUrl = product.getProductImage();

        // Check if the image URL is valid
        if (productImageUrl != null && !productImageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(productImageUrl) // Product image URL
                    .circleCrop() // Apply CircleCrop transformation for circular image
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image for offline use
                    .placeholder(R.drawable.gear) // Placeholder while loading
                    .error(R.drawable.gear) // Fallback image if loading fails
                    .into(holder.ivProductImage); // ImageView to load the image into
        } else {
            holder.ivProductImage.setImageResource(R.drawable.gear); // Default fallback image
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ViewHolder class to hold the views for each product item
    public static class SalesProductViewHolder extends RecyclerView.ViewHolder {

        // Define your view components
        TextView tvProductName, tvProductPrice, tvProductQuantity, tvProductYearModel;
        ImageView ivProductImage;

        public SalesProductViewHolder(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvProductYearModel = itemView.findViewById(R.id.tvProductYearModel);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);

        }
    }
}

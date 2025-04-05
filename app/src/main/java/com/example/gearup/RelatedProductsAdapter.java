package com.example.gearup;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RelatedProductsAdapter extends RecyclerView.Adapter<RelatedProductsAdapter.RelatedProductViewHolder> {

    private List<Product> relatedProducts;

    public RelatedProductsAdapter(List<Product> relatedProducts) {
        this.relatedProducts = relatedProducts;
    }

    @Override
    public RelatedProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_related_product, parent, false);
        return new RelatedProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RelatedProductViewHolder holder, int position) {
        Product product = relatedProducts.get(position);
        Log.d("RelatedProductsAdapter", "Product: " + product.getName() + ", Price: " + product.getPrice() + ", Image URLs: " + product.getImageUrls());

        holder.productName.setText(product.getName());
        holder.productPrice.setText(String.format("₱%,.2f", product.getPrice()));

        // Check if there are any image URLs
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            // Use the first image URL in the list
            String imageUrl = imageUrls.get(0);  // Modify as needed if you want to display other images
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl) // Load the first image URL from the list
                    .placeholder(R.drawable.ic_launcher_background) // Placeholder while loading
                    .into(holder.productImage);
        } else {
            // Set a default image if no image URLs are available
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_launcher_background) // Default image
                    .into(holder.productImage);
        }
    }

    @Override
    public int getItemCount() {
        return relatedProducts.size();
    }

    public static class RelatedProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice;
        ImageView productImage;

        public RelatedProductViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.tv_product_name);
            productPrice = itemView.findViewById(R.id.tv_product_price);
            productImage = itemView.findViewById(R.id.iv_product_image);
        }
    }
}

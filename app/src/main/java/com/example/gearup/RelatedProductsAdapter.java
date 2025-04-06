package com.example.gearup;

import android.content.Intent;
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
            String imageUrl = imageUrls.get(0);  // Modify as needed if you want to display other images
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.productImage);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_launcher_background)
                    .into(holder.productImage);
        }

        holder.itemView.setOnClickListener(v -> {
            // Log to ensure product is valid
            Log.d("RelatedProductsAdapter", "Product clicked: " + product);

            // Pass the product to the next activity
            Intent intent = new Intent(holder.itemView.getContext(), ProductDetailsBuyerActivity.class);
            intent.putExtra("PRODUCT", product);  // Passing entire product object

            // Log the product ID being passed
            Log.d("RelatedProductsAdapter", "Passing Product ID to ProductDetailsActivity: " + product.getId());

            holder.itemView.getContext().startActivity(intent);
        });
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

package com.example.gearup;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SearchProductAdapter extends RecyclerView.Adapter<SearchProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private String category;
    private OnProductClickListener listener;
    private Context context;

    public SearchProductAdapter(Context context, List<Product> productList, String category, OnProductClickListener listener) {
        this.context = context;
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
        holder.productBrand.setText("Brand: " + product.getBrand()); // Changed from description to brand
        holder.productPrice.setText("Price: " + String.format("₱%,.2f", product.getPrice()));

        // Load the product image (if any)
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrls().get(0))
                    .into(holder.productImage);
        }

        // Load the seller profile image (if available)
        if (product.getSellerProfileImageUrl() != null && !product.getSellerProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getSellerProfileImageUrl())
                    .circleCrop()  // Optionally crop it to a circular shape for profile image
                    .into(holder.sellerProfileImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsBuyerActivity.class);
            intent.putExtra("PRODUCT", product);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateProductList(List<Product> newProductList) {
        Log.d(TAG, "Updating adapter with " + newProductList.size() + " products.");
        this.productList.clear();
        this.productList.addAll(newProductList);
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productBrand, productPrice;
        ImageView productImage, sellerProfileImage;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.tv_product_name);
            productBrand = itemView.findViewById(R.id.tv_product_brand);
            productPrice = itemView.findViewById(R.id.tv_product_price);
            productImage = itemView.findViewById(R.id.iv_product_image);
            sellerProfileImage = itemView.findViewById(R.id.civ_seller_profile_image); // Added seller profile image view
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}

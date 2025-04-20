package com.example.gearup;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PopularProductAdapter extends RecyclerView.Adapter<PopularProductAdapter.ProductViewHolder> {

    private List<PopularProduct> productList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(PopularProduct product);
    }

    public PopularProductAdapter(List<PopularProduct> products, OnItemClickListener listener) {
        this.productList = products;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each product
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_popular, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        PopularProduct product = productList.get(position);

        // Set product data into the views
        holder.titleTextView.setText(product.getTitle());
        holder.priceTextView.setText(product.getPrice());
        holder.conditionTextView.setText("Condition: " + (product.getCondition() != null ? product.getCondition() : "N/A"));
        holder.ratedTextView.setText("Rated: " + (product.getRated() != "NaN" ? product.getRated() : "N/A"));
       // holder.discountTextView.setText("Discount: " + (product.getDiscount() != "NaN" ? product.getDiscount() : "N/A"));

        // Load product image using Glide
        Glide.with(holder.itemView.getContext()).load(product.getImageUrl()).into(holder.productImageView);

        // Set the click listener for the product item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                // Pass the selected product to the listener
                onItemClickListener.onItemClick(product);
            }
        });
    }

    // Method to update the product list
    public void updateProducts(List<PopularProduct> newProducts) {
        this.productList.clear();
        this.productList.addAll(newProducts);
        notifyDataSetChanged();
    }

    // Method to update the similar products list
    public void setSimilarProducts(List<PopularProduct> similarProducts) {
        this.productList = similarProducts;  // Update the main list to be similar products
        notifyDataSetChanged();  // Refresh the RecyclerView
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, priceTextView, conditionTextView, ratedTextView, discountTextView;
        ImageView productImageView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the views for the product item
            titleTextView = itemView.findViewById(R.id.product_title);
            priceTextView = itemView.findViewById(R.id.product_price);
            conditionTextView = itemView.findViewById(R.id.product_condition);
            ratedTextView = itemView.findViewById(R.id.product_rated);
            productImageView = itemView.findViewById(R.id.product_image);
        }
    }
}

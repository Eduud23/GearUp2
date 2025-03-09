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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_popular, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        PopularProduct product = productList.get(position);

        // Setting the data first
        holder.titleTextView.setText(product.getTitle());
        holder.priceTextView.setText(product.getPrice());
        holder.conditionTextView.setText("Condition: " + (product.getCondition() != null ? product.getCondition() : "N/A"));
        holder.ratedTextView.setText("Rated: " + (product.getRated() != null ? product.getRated() : "N/A"));
        holder.discountTextView.setText("Discount: " + (product.getDiscount() != null ? product.getDiscount() : "N/A"));

        // Load product image
        Glide.with(holder.itemView.getContext()).load(product.getImageUrl()).into(holder.productImageView);

        // Set the click listener last
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(product));
        }
    }

    public void updateProducts(List<PopularProduct> newProducts) {
        this.productList.clear();
        this.productList.addAll(newProducts);
        notifyDataSetChanged();
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
            titleTextView = itemView.findViewById(R.id.product_title);
            priceTextView = itemView.findViewById(R.id.product_price);
            conditionTextView = itemView.findViewById(R.id.product_condition);
            ratedTextView = itemView.findViewById(R.id.product_rated);
            discountTextView = itemView.findViewById(R.id.product_discount);
            productImageView = itemView.findViewById(R.id.product_image);
        }
    }
}

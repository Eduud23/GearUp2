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
        int firstIndex = position * 2;
        int secondIndex = firstIndex + 1;

        PopularProduct firstProduct = productList.get(firstIndex);
        holder.titleTextView1.setText(firstProduct.getTitle());
        holder.priceTextView1.setText(firstProduct.getPrice());
        Glide.with(holder.itemView.getContext()).load(firstProduct.getImageUrl()).into(holder.productImageView1);

        // Click listener for the first product
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(firstProduct));

        if (secondIndex < productList.size()) {
            PopularProduct secondProduct = productList.get(secondIndex);
            holder.titleTextView2.setText(secondProduct.getTitle());
            holder.priceTextView2.setText(secondProduct.getPrice());
            Glide.with(holder.itemView.getContext()).load(secondProduct.getImageUrl()).into(holder.productImageView2);

            // Click listener for the second product
            holder.productContainer2.setVisibility(View.VISIBLE);
            holder.productContainer2.setOnClickListener(v -> onItemClickListener.onItemClick(secondProduct));
        } else {
            holder.productContainer2.setVisibility(View.INVISIBLE);
        }
    }
    public void updateProducts(List<PopularProduct> newProducts) {
        this.productList.clear();
        this.productList.addAll(newProducts);
        notifyDataSetChanged();
    }



    @Override
    public int getItemCount() {
        return (int) Math.ceil(productList.size() / 2.0);
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView1, priceTextView1, titleTextView2, priceTextView2;
        ImageView productImageView1, productImageView2;
        View productContainer2;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView1 = itemView.findViewById(R.id.product_title_1);
            priceTextView1 = itemView.findViewById(R.id.product_price_1);
            productImageView1 = itemView.findViewById(R.id.product_image_1);

            titleTextView2 = itemView.findViewById(R.id.product_title_2);
            priceTextView2 = itemView.findViewById(R.id.product_price_2);
            productImageView2 = itemView.findViewById(R.id.product_image_2);
            productContainer2 = itemView.findViewById(R.id.product_container_2);
        }
    }
}

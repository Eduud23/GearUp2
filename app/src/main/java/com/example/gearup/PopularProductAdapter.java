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

    public PopularProductAdapter(List<PopularProduct> products) {
        this.productList = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_popular, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        // Each page holds TWO products
        int firstIndex = position * 2;
        int secondIndex = firstIndex + 1;

        PopularProduct firstProduct = productList.get(firstIndex);
        holder.titleTextView1.setText(firstProduct.getTitle());
        holder.priceTextView1.setText("$" + firstProduct.getPrice());
        Glide.with(holder.itemView.getContext()).load(firstProduct.getImageUrl()).into(holder.productImageView1);

        // Check if there's a second product to display
        if (secondIndex < productList.size()) {
            PopularProduct secondProduct = productList.get(secondIndex);
            holder.titleTextView2.setText(secondProduct.getTitle());
            holder.priceTextView2.setText("$" + secondProduct.getPrice());
            Glide.with(holder.itemView.getContext()).load(secondProduct.getImageUrl()).into(holder.productImageView2);
        } else {
            // Hide the second product if there's only one left
            holder.productContainer2.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return (int) Math.ceil(productList.size() / 2.0); // Divide total products by 2 for pages
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

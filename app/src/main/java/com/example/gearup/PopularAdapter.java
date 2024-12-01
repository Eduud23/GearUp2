package com.example.gearup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.PopularViewHolder> {

    private List<PopularItem> popularItemList;
    private Context context;

    public PopularAdapter(List<PopularItem> popularItemList, Context context) {
        this.popularItemList = popularItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public PopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_popular, parent, false);
        return new PopularViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularViewHolder holder, int position) {
        PopularItem popularItem = popularItemList.get(position);

        // Set address and zipCode
        holder.tvAddress.setText(popularItem.getAddress());
        holder.tvZipCode.setText(popularItem.getZipCode());

        // Load the product image using Glide
        if (popularItem.getProductImage() != null && !popularItem.getProductImage().isEmpty()) {
            Glide.with(context)
                    .load(popularItem.getProductImage())  // Assuming productImage is a URL or URI
                    .into(holder.imgProduct);
        }

        // Display the quantity sold
        holder.tvQuantity.setText("Quantity Sold: " + popularItem.getProductQuantity());

        // Set OnClickListener for the "View Details" button
        holder.btnViewDetails.setOnClickListener(v -> {
            // Passing address, zipCode, product image, and quantity to the next activity
            Intent intent = new Intent(context, ProductSalesActivity.class);
            intent.putExtra("address", popularItem.getAddress());
            intent.putExtra("zipCode", popularItem.getZipCode());
            intent.putExtra("productImage", popularItem.getProductImage());
            intent.putExtra("productQuantity", popularItem.getProductQuantity());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return popularItemList.size();
    }

    public void setFilteredData(List<PopularItem> filteredData) {
        this.popularItemList = filteredData;
        notifyDataSetChanged();  // Notify that the data has changed
    }

    public static class PopularViewHolder extends RecyclerView.ViewHolder {

        TextView tvAddress, tvZipCode, tvQuantity;
        ImageView imgProduct;
        Button btnViewDetails;

        public PopularViewHolder(View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvZipCode = itemView.findViewById(R.id.tvZipCode);
            tvQuantity = itemView.findViewById(R.id.tvQuantity); // TextView for quantity sold
            imgProduct = itemView.findViewById(R.id.imgProduct); // ImageView for product image
            btnViewDetails = itemView.findViewById(R.id.btnAction);
        }
    }
}

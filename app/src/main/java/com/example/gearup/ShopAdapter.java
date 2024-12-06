package com.example.gearup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<Shop> shopList;
    private OnShopClickListener listener;
    private Context context;

    public ShopAdapter(List<Shop> shopList, OnShopClickListener listener, Context context) {
        this.shopList = shopList;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Shop shop = shopList.get(position);
        holder.shopName.setText(shop.getShopName());
        holder.address.setText(shop.getAddress());
        holder.phone.setText(shop.getPhone());

        // Load the profile image using Glide
        Glide.with(context)
                .load(shop.getProfileImageUrl()) // Load the profile image URL
                .placeholder(R.drawable.gear) // Show a placeholder if no image is available
                .error(R.drawable.gear) // Show an error image if the URL is invalid
                .into(holder.shopImage); // Bind the image to the ImageView

        // Set the click listener for the item
        holder.itemView.setOnClickListener(v -> listener.onShopClick(position));
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public interface OnShopClickListener {
        void onShopClick(int position);
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView shopName, address, phone;
        ImageView shopImage;

        public ShopViewHolder(View itemView) {
            super(itemView);
            shopName = itemView.findViewById(R.id.shopName);
            address = itemView.findViewById(R.id.address);
            phone = itemView.findViewById(R.id.phone);
            shopImage = itemView.findViewById(R.id.shopImage); // Profile image view
        }
    }
}

package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<Shop> shopList;
    private OnShopClickListener listener;

    public ShopAdapter(List<Shop> shopList, OnShopClickListener listener) {
        this.shopList = shopList;
        this.listener = listener;
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

        public ShopViewHolder(View itemView) {
            super(itemView);
            shopName = itemView.findViewById(R.id.shopName);
            address = itemView.findViewById(R.id.address);
            phone = itemView.findViewById(R.id.phone);
        }
    }
}

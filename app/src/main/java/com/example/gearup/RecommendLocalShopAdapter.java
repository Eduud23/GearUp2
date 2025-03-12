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

public class RecommendLocalShopAdapter extends RecyclerView.Adapter<RecommendLocalShopAdapter.ViewHolder> {

    private List<RecommendLocalShop> shopList;
    private Context context;
    private OnShopClickListener onShopClickListener;

    public interface OnShopClickListener {
        void onShopClick(RecommendLocalShop shop);
    }

    public RecommendLocalShopAdapter(List<RecommendLocalShop> shopList, Context context, OnShopClickListener onShopClickListener) {
        this.shopList = shopList;
        this.context = context;
        this.onShopClickListener = onShopClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend_local_shop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendLocalShop shop = shopList.get(position);
        holder.shopName.setText(shop.getShopName());
        holder.place.setText(shop.getPlace());
        holder.ratings.setText(String.valueOf(shop.getRatings()));

        // Load image using Glide
        Glide.with(context).load(shop.getImage()).into(holder.shopImage);

        holder.itemView.setOnClickListener(v -> onShopClickListener.onShopClick(shop));
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView shopImage;
        TextView shopName, place, ratings;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            shopImage = itemView.findViewById(R.id.shopImage);
            shopName = itemView.findViewById(R.id.shopName);
            place = itemView.findViewById(R.id.shopPlace);
            ratings = itemView.findViewById(R.id.ratings);
        }
    }
}

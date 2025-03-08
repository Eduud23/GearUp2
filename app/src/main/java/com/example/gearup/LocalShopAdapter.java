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

public class LocalShopAdapter extends RecyclerView.Adapter<LocalShopAdapter.ShopViewHolder> {

    private List<LocalShop> shopList;
    private Context context;

    public LocalShopAdapter(List<LocalShop> shopList, Context context) {
        this.shopList = shopList;
        this.context = context;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_local_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        LocalShop shop = shopList.get(position);
        holder.shopName.setText(shop.getShopName());
        holder.kindOfRepair.setText(shop.getKindOfRepair());
        holder.timeSchedule.setText(shop.getTimeSchedule());
        holder.place.setText(shop.getPlace());
        holder.ratings.setText("Ratings: " + shop.getRatings());
        holder.distance.setText(String.format("Distance: %.2f km", shop.getDistance()));

        // Load image using Glide
        Glide.with(context)
                .load(shop.getImage())
                .placeholder(R.drawable.gear)
                .error(R.drawable.gear)
                .into(holder.shopImage);
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView shopName, kindOfRepair, timeSchedule, place, ratings, distance;
        ImageView shopImage;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            shopName = itemView.findViewById(R.id.shopName);
            kindOfRepair = itemView.findViewById(R.id.kindOfRepair);
            timeSchedule = itemView.findViewById(R.id.timeSchedule);
            place = itemView.findViewById(R.id.place);
            ratings = itemView.findViewById(R.id.ratings);
            distance = itemView.findViewById(R.id.distance);
            shopImage = itemView.findViewById(R.id.shopImage);
        }
    }
}

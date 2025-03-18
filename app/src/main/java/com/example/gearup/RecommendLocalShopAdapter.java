package com.example.gearup;

import android.content.Context;
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

public class RecommendLocalShopAdapter extends RecyclerView.Adapter<RecommendLocalShopAdapter.ViewHolder> {

    private List<RecommendLocalShop> shopList;
    private Context context;

    public RecommendLocalShopAdapter(List<RecommendLocalShop> shopList, Context context)

        {
        this.shopList = shopList;
        this.context = context;
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
        holder.kindOfService.setText(shop.getKindOfService());

        // Load image using Glide
        Glide.with(context).load(shop.getImage()).into(holder.shopImage);

        // Display distance
        double distance = shop.getDistance();
        String distanceText = (distance >= 1000) ? String.format("%.2f km", distance / 1000) : String.format("%.0f m", distance);
        holder.distance.setText(distanceText);

        // Click listener to open Local Shop Detail Activity
        holder.itemView.setOnClickListener(v -> openLocalShopDetailActivity(shop));
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    private void openLocalShopDetailActivity(RecommendLocalShop shop) {
        Intent intent = new Intent(context, ServiceDetailActivity.class);
        intent.putExtra("isLocalShop", true);
        intent.putExtra("name", shop.getShopName());
        intent.putExtra("latitude", shop.getLatitude());
        intent.putExtra("longitude", shop.getLongitude());
        intent.putExtra("kindOfService", shop.getKindOfService());
        intent.putExtra("place", shop.getPlace());
        intent.putExtra("distance", shop.getDistance());
        intent.putExtra("image", shop.getImage());
        intent.putExtra("contactNumber", shop.getContactNumber());
        intent.putExtra("ratings", shop.getRatings());
        intent.putExtra("timeSchedule", shop.getTimeSchedule());
        intent.putExtra("website", shop.getWebsite());
        context.startActivity(intent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView shopImage;
        TextView shopName, place, ratings, kindOfService, distance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            shopImage = itemView.findViewById(R.id.shopImage);
            shopName = itemView.findViewById(R.id.shopName);
            kindOfService = itemView.findViewById(R.id.serviceKind);
            place = itemView.findViewById(R.id.shopPlace);
            ratings = itemView.findViewById(R.id.ratings);
            distance = itemView.findViewById(R.id.distance);
        }
    }
}

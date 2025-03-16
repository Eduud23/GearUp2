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

public class RecommendTowingAdapter extends RecyclerView.Adapter<RecommendTowingAdapter.TowingViewHolder> {
    private Context context;
    private List<RecommendTowing> towingList;

    public RecommendTowingAdapter(Context context, List<RecommendTowing> towingList) {
        this.context = context;
        this.towingList = towingList;
    }

    @NonNull
    @Override
    public TowingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_towing, parent, false);
        return new TowingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TowingViewHolder holder, int position) {
        RecommendTowing towing = towingList.get(position);
        holder.shopName.setText(towing.getShopName());
        holder.kindOfService.setText(towing.getKindOfService());
        holder.place.setText(towing.getPlace());
        holder.contactNumber.setText(towing.getContactNumber());
        holder.ratings.setText(towing.getRatings());

        Glide.with(context).load(towing.getImage()).into(holder.imageView);

        double distance = towing.getDistance();
        String distanceText = (distance >= 1000) ? String.format("%.2f km", distance / 1000) : String.format("%.0f m", distance);
        holder.distance.setText(distanceText);
    }

    @Override
    public int getItemCount() {
        return towingList.size();
    }

    public static class TowingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView shopName, kindOfService, place, ratings, distance, contactNumber;

        public TowingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.towingImage);
            shopName = itemView.findViewById(R.id.towingShopName);
            kindOfService = itemView.findViewById(R.id.towingKindOfService);
            place = itemView.findViewById(R.id.towingPlace);
            ratings = itemView.findViewById(R.id.towingRatings);
            contactNumber = itemView.findViewById(R.id.towingContactNumber);
            distance = itemView.findViewById(R.id.distance);
        }
    }
}

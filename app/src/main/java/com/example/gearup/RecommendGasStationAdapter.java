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

public class RecommendGasStationAdapter extends RecyclerView.Adapter<RecommendGasStationAdapter.ViewHolder> {

    private Context context;
    private List<RecommendGasStation> gasStationList;

    public RecommendGasStationAdapter(Context context, List<RecommendGasStation> gasStationList) {
        this.context = context;
        this.gasStationList = gasStationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gas_station, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendGasStation gasStation = gasStationList.get(position);
        holder.nameTextView.setText(gasStation.getName());
        holder.kindOfServiceTextView.setText(gasStation.getKindOfService());
        holder.placeTextView.setText(gasStation.getPlace());

        Glide.with(context)
                .load(gasStation.getImageUrl())
                .placeholder(R.drawable.gear)
                .into(holder.gasStationImageView);
    }

    @Override
    public int getItemCount() {
        return gasStationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, kindOfServiceTextView, placeTextView;
        ImageView gasStationImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            kindOfServiceTextView = itemView.findViewById(R.id.kindOfServiceTextView);
            placeTextView = itemView.findViewById(R.id.placeTextView);
            gasStationImageView = itemView.findViewById(R.id.gasStationImageView);
        }
    }
}

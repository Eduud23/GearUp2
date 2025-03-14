package com.example.gearup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class LocalTrendsAdapter extends RecyclerView.Adapter<LocalTrendsAdapter.ViewHolder> {

    private List<LocalTrendsData> localTrendsList;

    public LocalTrendsAdapter(List<LocalTrendsData> localTrendsList) {
        this.localTrendsList = localTrendsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_local_trend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalTrendsData data = localTrendsList.get(position);

        Glide.with(holder.itemView.getContext()).load(data.getImage()).into(holder.imageView);
        holder.nameTextView.setText(data.getName());
        holder.placeTextView.setText(data.getPlace());
        holder.ratingsTextView.setText("Ratings: " + data.getRatings());
        holder.soldTextView.setText("Sold: " + data.getSold());

        holder.itemView.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.getLink()));
            v.getContext().startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return localTrendsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, placeTextView, ratingsTextView, soldTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            placeTextView = itemView.findViewById(R.id.placeTextView);
            ratingsTextView = itemView.findViewById(R.id.ratingsTextView);
            soldTextView = itemView.findViewById(R.id.soldTextView);
        }
    }
}

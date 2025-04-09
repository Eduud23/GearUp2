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

public class LocalTrendsAdapter extends RecyclerView.Adapter<LocalTrendsAdapter.ViewHolder> {

    private List<LocalTrendsData> localTrendsList;
    private OnItemClickListener onItemClickListener;

    // Interface to handle item click events
    public interface OnItemClickListener {
        void onItemClick(LocalTrendsData data);
    }

    // Constructor accepting the list and the click listener
    public LocalTrendsAdapter(List<LocalTrendsData> localTrendsList, OnItemClickListener onItemClickListener) {
        this.localTrendsList = localTrendsList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_local_trend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the current item (local trend)
        LocalTrendsData data = localTrendsList.get(position);

        // Bind data to the views
        Glide.with(holder.itemView.getContext())
                .load(data.getImage())  // Load image with Glide
                .placeholder(R.drawable.ic_launcher_foreground)  // Placeholder image while loading
                .error(R.drawable.gear)  // Error image in case of failure
                .into(holder.imageView);

        holder.nameTextView.setText(data.getName());
        holder.placeTextView.setText(data.getPlace());
        holder.ratingsTextView.setText("Ratings: " + data.getRatings());
        holder.soldTextView.setText("Sold: " + data.getSold());

        // Set up click listener for each item
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(data));
    }

    @Override
    public int getItemCount() {
        // Return the number of items in the list
        return localTrendsList.size();
    }

    // ViewHolder to hold each item's views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, placeTextView, ratingsTextView, soldTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind the views to the ViewHolder
            imageView = itemView.findViewById(R.id.imageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            placeTextView = itemView.findViewById(R.id.placeTextView);
            ratingsTextView = itemView.findViewById(R.id.ratingsTextView);
            soldTextView = itemView.findViewById(R.id.soldTextView);
        }
    }

    // Method to update the product list when new data is fetched
    public void updateProducts(List<LocalTrendsData> newLocalTrendsList) {
        // Clear the existing list and add the new data
        localTrendsList.clear();
        localTrendsList.addAll(newLocalTrendsList);
        notifyDataSetChanged();  // Notify the adapter that the data has changed
    }
}

package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ForecastDetailAdapter extends RecyclerView.Adapter<ForecastDetailAdapter.ViewHolder> {

    private final List<ForecastProductItem> itemList;

    public ForecastDetailAdapter(List<ForecastProductItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ForecastDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastDetailAdapter.ViewHolder holder, int position) {
        ForecastProductItem item = itemList.get(position);

        holder.category.setText("Category: " + item.getCategory());  // This is the new 'category' (was 'product_line')
        holder.vehicleType.setText("Vehicle Type: " + item.getVehicle_type());  // Formerly 'category'
        holder.component.setText("Component: " + item.getComponent());
        holder.description.setText("Description: " + item.getDescription());

        Glide.with(holder.itemView.getContext())
                .load(item.getImage_url())
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView category, vehicleType, component, description;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.categoryText);
            vehicleType = itemView.findViewById(R.id.vehicleTypeText);  // You'll need to add this in XML
            component = itemView.findViewById(R.id.componentText);
            description = itemView.findViewById(R.id.descriptionText);
            image = itemView.findViewById(R.id.imageView);
        }
    }
}

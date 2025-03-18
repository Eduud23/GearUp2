package com.example.gearup;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecommendCombinedAdapter extends RecyclerView.Adapter<RecommendCombinedAdapter.ServiceViewHolder> {

    private final Context context;
    private final List<Object> services;
    private static final String TAG = "RecommendAdapter";

    public RecommendCombinedAdapter(Context context, List<Object> services, Object currentService) {
        this.context = context;
        this.services = new ArrayList<>();
        for (Object service : services) {
            if (service instanceof RecommendLocalShop && currentService instanceof RecommendLocalShop) {
                if (!((RecommendLocalShop) service).getShopName().equals(((RecommendLocalShop) currentService).getShopName())) {
                    this.services.add(service);
                }
            } else if (service instanceof RecommendGasStation && currentService instanceof RecommendGasStation) {
                if (!((RecommendGasStation) service).getName().equals(((RecommendGasStation) currentService).getName())) {
                    this.services.add(service);
                }
            } else if (service instanceof RecommendTowing && currentService instanceof RecommendTowing) {
                if (!((RecommendTowing) service).getShopName().equals(((RecommendTowing) currentService).getShopName())) {
                    this.services.add(service);
                }
            } else {
                this.services.add(service);
            }
        }
        // Sort services by distance (nearest to farthest)
        Collections.sort(this.services, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                double distance1 = getDistance(o1);
                double distance2 = getDistance(o2);
                return Double.compare(distance1, distance2);
            }
        });

        Log.d(TAG, "Adapter initialized with " + services.size() + " services.");
    }

    private double getDistance(Object service) {
        if (service instanceof RecommendLocalShop) {
            return ((RecommendLocalShop) service).getDistance();
        } else if (service instanceof RecommendGasStation) {
            return ((RecommendGasStation) service).getDistance();
        } else if (service instanceof RecommendTowing) {
            return ((RecommendTowing) service).getDistance();
        }
        return Double.MAX_VALUE;  // Default to max if unknown type
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_combined_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Object service = services.get(position);
        Log.d(TAG, "Binding service at position " + position + ": " + service.getClass().getSimpleName());
        if (holder.name != null && holder.type != null && holder.distance != null && holder.icon != null) {
            if (service instanceof RecommendLocalShop) {
                RecommendLocalShop shop = (RecommendLocalShop) service;
                holder.name.setText(shop.getShopName());
                holder.type.setText(shop.getKindOfService());
                holder.distance.setText(String.format("%.2f km", shop.getDistance() / 1000));
                Glide.with(context).load(shop.getImage()).into(holder.icon);

                holder.itemView.setOnClickListener(v -> openLocalShopDetailActivity(shop));
            } else if (service instanceof RecommendGasStation) {
                RecommendGasStation station = (RecommendGasStation) service;
                holder.name.setText(station.getName());
                holder.type.setText("Gas Station");
                holder.distance.setText(String.format("%.2f km", station.getDistance() / 1000));
                Glide.with(context).load(station.getImageUrl()).into(holder.icon);

                holder.itemView.setOnClickListener(v -> openGasStationDetailActivity(station));
            } else if (service instanceof RecommendTowing) {
                RecommendTowing towing = (RecommendTowing) service;
                holder.name.setText(towing.getShopName());
                holder.type.setText("Towing Service");
                holder.distance.setText(String.format("%.2f km", towing.getDistance() / 1000));
                Glide.with(context).load(towing.getImage()).into(holder.icon);

                holder.itemView.setOnClickListener(v -> openTowingDetailActivity(towing));
            } else {
                Log.w(TAG, "Unknown service type: " + service.getClass().getSimpleName());
            }
        } else {
            Log.e(TAG, "ViewHolder views are null");
        }
    }

    private void openLocalShopDetailActivity(RecommendLocalShop shop) {
        Intent intent = new Intent(context, ServiceDetailActivity.class);
        intent.putExtra("isLocalShop", true);
        intent.putExtra("selectedService", shop);
        intent.putExtra("allServices", new ArrayList<>(services));
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

    private void openGasStationDetailActivity(RecommendGasStation station) {
        Intent intent = new Intent(context, ServiceDetailActivity.class);
        intent.putExtra("isGasStation", true);
        intent.putExtra("selectedService", station);
        intent.putExtra("allServices", new ArrayList<>(services));
        intent.putExtra("name", station.getName());
        intent.putExtra("latitude", station.getLatitude());
        intent.putExtra("longitude", station.getLongitude());
        intent.putExtra("kindOfService", station.getKindOfService());
        intent.putExtra("place", station.getPlace());
        intent.putExtra("distance", station.getDistance());
        intent.putExtra("image", station.getImageUrl());
        context.startActivity(intent);
    }

    private void openTowingDetailActivity(RecommendTowing towing) {
        Intent intent = new Intent(context, ServiceDetailActivity.class);
        intent.putExtra("isTowing", true);
        intent.putExtra("selectedService", towing);
        intent.putExtra("allServices", new ArrayList<>(services));
        intent.putExtra("name", towing.getShopName());
        intent.putExtra("latitude", towing.getLatitude());
        intent.putExtra("longitude", towing.getLongitude());
        intent.putExtra("kindOfService", towing.getKindOfService());
        intent.putExtra("place", towing.getPlace());
        intent.putExtra("contactNumber", towing.getContactNumber());
        intent.putExtra("ratings", towing.getRatings());
        intent.putExtra("distance", towing.getDistance());
        intent.putExtra("image", towing.getImage());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "Total items count: " + services.size());
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, distance;
        ImageView icon;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.serviceName);
            type = itemView.findViewById(R.id.serviceKind);
            distance = itemView.findViewById(R.id.distance);
            icon = itemView.findViewById(R.id.serviceIcon);
        }
    }
}

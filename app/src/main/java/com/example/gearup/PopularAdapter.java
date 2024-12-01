package com.example.gearup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.PopularViewHolder> {

    private List<String> addressList;
    private Context context;

    public PopularAdapter(List<String> addressList, Context context) {
        this.addressList = addressList;
        this.context = context;
    }

    @NonNull
    @Override
    public PopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_popular, parent, false);
        return new PopularViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularViewHolder holder, int position) {
        String address = addressList.get(position);
        holder.tvAddress.setText(address);

        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductSalesActivity.class);
            intent.putExtra("address", address); // Passing the address to the next activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public void setFilteredData(List<String> filteredList) {
        this.addressList = filteredList;
        notifyDataSetChanged(); // Notify that the data has changed
    }

    public static class PopularViewHolder extends RecyclerView.ViewHolder {

        TextView tvAddress;
        Button btnViewDetails;

        public PopularViewHolder(View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            btnViewDetails = itemView.findViewById(R.id.btnAction);
        }
    }
}

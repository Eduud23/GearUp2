package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;
import java.util.List;

public class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.PredictionViewHolder> {

    private List<AddressData> addressDataList;

    public PredictionAdapter(List<AddressData> addressDataList) {
        this.addressDataList = addressDataList;
    }

    @Override
    public PredictionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prediction, parent, false);
        return new PredictionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PredictionViewHolder holder, int position) {
        AddressData addressData = addressDataList.get(position);
        holder.addressTextView.setText(addressData.getAddress());

        StringBuilder nextMonthText = new StringBuilder("Next Month Predictions:\n\n");
        for (Prediction prediction : addressData.getNextMonthPredictions()) {
            nextMonthText.append("  Product: ").append(prediction.getProduct())
                    .append("\n      Predicted Sales: ").append(String.format("%.2f", prediction.getPredictedSales()))
                    .append("\n\n");
        }

        holder.nextMonthTextView.setText(nextMonthText.toString());

        // Initially hide the predictions text
        holder.nextMonthTextView.setVisibility(View.GONE);

        // Show the 'See More' button and set its click listener
        holder.btnSeeMore.setVisibility(View.VISIBLE);
        holder.btnSeeMore.setOnClickListener(v -> {
            if (holder.nextMonthTextView.getVisibility() == View.GONE) {
                holder.nextMonthTextView.setVisibility(View.VISIBLE);
                holder.btnSeeMore.setText("See Less"); // Change button text to 'See Less'
            } else {
                holder.nextMonthTextView.setVisibility(View.GONE);
                holder.btnSeeMore.setText("See More"); // Change button text back to 'See More'
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressDataList.size();
    }

    public static class PredictionViewHolder extends RecyclerView.ViewHolder {
        TextView addressTextView;
        TextView nextMonthTextView;
        Button btnSeeMore;
        CardView cardView;

        public PredictionViewHolder(View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.tvAddress);
            nextMonthTextView = itemView.findViewById(R.id.tvNextMonthPredictions);
            btnSeeMore = itemView.findViewById(R.id.btnSeeMore);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}

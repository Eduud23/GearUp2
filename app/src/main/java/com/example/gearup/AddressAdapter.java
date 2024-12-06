package com.example.gearup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<String> addresses;
    private List<String> zipCodes;
    private Context context;

    public AddressAdapter(Context context, List<String> addresses, List<String> zipCodes) {
        this.context = context;
        this.addresses = addresses;
        this.zipCodes = zipCodes;
    }

    @Override
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AddressViewHolder holder, int position) {
        String address = addresses.get(position);
        String zipCode = zipCodes.get(position);

        holder.addressTextView.setText(address);
        holder.zipCodeTextView.setText(zipCode);

        // Initially hide the predictions
        holder.predictionContainer.setVisibility(View.GONE);

        // Set the "See More" button click listener
        holder.seeMoreButton.setOnClickListener(v -> {
            // Toggle the visibility of the prediction container
            if (holder.predictionContainer.getVisibility() == View.GONE) {
                // Fetch predictions and show
                fetchSalesPrediction(address, holder);
            } else {
                // Hide predictions if they are already visible
                holder.predictionContainer.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    private void fetchSalesPrediction(String address, AddressViewHolder holder) {
        // Default base URL (for development)
        String baseUrl = "http://192.168.254.155:5002/"; // Development URL

        // Check for different environments using else-if
        if (isDevelopmentEnvironment()) {
            baseUrl = "http://192.168.254.155:5002/"; // Development URL
        } else if (isStagingEnvironment()) {
            baseUrl = "http//192.168.42.85:5002/"; // Staging URL
        } else if (isProductionEnvironment()) {
            baseUrl = "https://api.yourcompany.com/"; // Production URL
        }

        // Create Retrofit instance with the dynamically set base URL
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create the ApiService
        ApiService apiService = retrofit.create(ApiService.class);

        // Make the API call
        Call<JsonObject> call = apiService.getSalesPrediction(address);

        // Handle the response asynchronously
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Process the prediction data
                    JsonObject predictions = response.body();
                    // Display the predictions in the container
                    displayPredictions(predictions, holder);
                } else {
                    Toast.makeText(context, "Error fetching predictions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(context, "Failed to fetch predictions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isDevelopmentEnvironment() {

        return true;
    }

    private boolean isStagingEnvironment() {
        return false;
    }

    private boolean isProductionEnvironment() {
        return false;
    }

    private void displayPredictions(JsonObject predictions, AddressViewHolder holder) {
        StringBuilder predictionText = new StringBuilder("Predicted Popular Products for Next Month:\n\n");

        // Assuming predictions are in the format we set up in Flask
        JsonObject addressPredictions = predictions.getAsJsonObject(holder.addressTextView.getText().toString());
        JsonArray nextMonthPredictions = addressPredictions.getAsJsonArray("next_month");

        for (int i = 0; i < nextMonthPredictions.size(); i++) {
            JsonObject productPrediction = nextMonthPredictions.get(i).getAsJsonObject();
            String product = productPrediction.get("product").getAsString();
            double predictedSales = productPrediction.get("predicted_sales").getAsDouble();

            predictionText.append(product)
                    .append(": \n     ")
                    .append(predictedSales)
                    .append(" Predicted sales units\n\n");
        }

        // Set the predictions text and show the prediction container
        holder.predictionTextView.setText(predictionText.toString());
        holder.predictionContainer.setVisibility(View.VISIBLE);
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView addressTextView;
        TextView zipCodeTextView;
        Button seeMoreButton;
        LinearLayout predictionContainer;
        TextView predictionTextView;

        public AddressViewHolder(View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            zipCodeTextView = itemView.findViewById(R.id.zipCodeTextView);
            seeMoreButton = itemView.findViewById(R.id.seeMoreButton);
            predictionContainer = itemView.findViewById(R.id.predictionContainer);
            predictionTextView = itemView.findViewById(R.id.predictionTextView);
        }
    }
}

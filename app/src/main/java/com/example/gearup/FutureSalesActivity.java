package com.example.gearup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FutureSalesActivity extends AppCompatActivity {

    private Button btnGetPredictions;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private PredictionAdapter adapter;
    private List<AddressData> addressDataList = new ArrayList<>();
    private List<AddressData> filteredAddressDataList = new ArrayList<>();
    private boolean isFetchingData = false;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_future_sales);  // Update layout if necessary

        btnGetPredictions = findViewById(R.id.btnGetPredictions);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        etSearch = findViewById(R.id.et_search);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PredictionAdapter(filteredAddressDataList);
        recyclerView.setAdapter(adapter);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });


        // Set up EditText listener for search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterData(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        btnGetPredictions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFetchingData) {
                    fetchPredictions();
                }
            }
        });
    }

    private void fetchPredictions() {
        isFetchingData = true;
        progressBar.setVisibility(View.VISIBLE);
        btnGetPredictions.setEnabled(false);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Select the appropriate URL based on device type and network conditions
        String url = selectApiUrl();  // Call the method to select the correct URL

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        btnGetPredictions.setEnabled(true);
                        isFetchingData = false;
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject predictionsObject = new JSONObject(responseData);
                        List<AddressData> fetchedData = new ArrayList<>();

                        // Iterate through the predictions object for each address
                        for (Iterator<String> it = predictionsObject.keys(); it.hasNext(); ) {
                            String address = it.next();
                            JSONObject addressData = predictionsObject.getJSONObject(address);

                            // Only handle next month predictions
                            List<Prediction> nextMonthPredictions = parsePredictions(addressData.getJSONArray("next_month"));

                            fetchedData.add(new AddressData(address, nextMonthPredictions));
                        }

                        // Update UI with the fetched data
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addressDataList.clear();
                                addressDataList.addAll(fetchedData);
                                // Initially display all data
                                filteredAddressDataList.clear();
                                filteredAddressDataList.addAll(fetchedData);
                                adapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                                btnGetPredictions.setEnabled(true);
                                isFetchingData = false;
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private String selectApiUrl() {
        // Choose the appropriate URL based on the device's status
        if (DeviceUtils.isEmulator()) {
            return "http://10.0.2.2:5001/predict";  // Emulator URL
        } else if (DeviceUtils.isDeviceConnectedToLocalNetwork(this)) {
            return "http://192.168.254.155:5002/predict";  // Local network URL (your Flask API URL)
        } else if (DeviceUtils.isDeviceOnStagingNetwork(this)) {
            return "http//192.168.42.85:5001/predict";  // Staging network URL
        } else if (DeviceUtils.isDeviceOnProductionNetwork(this)) {
            return "https://api.yourdomain.com/predict";  // Production network URL
        } else {
            return "https://api.fallback.com/predict";  // Fallback URL
        }
    }

    private List<Prediction> parsePredictions(JSONArray predictionsArray) throws Exception {
        List<Prediction> predictions = new ArrayList<>();
        for (int i = 0; i < predictionsArray.length(); i++) {
            JSONObject predictionObject = predictionsArray.getJSONObject(i);
            String product = predictionObject.getString("product");
            double predictedSales = predictionObject.getDouble("predicted_sales");
            predictions.add(new Prediction(product, predictedSales));
        }
        return predictions;
    }

    private void filterData(String query) {
        filteredAddressDataList.clear();
        for (AddressData addressData : addressDataList) {
            if (addressData.getAddress().toLowerCase().contains(query.toLowerCase())) {
                filteredAddressDataList.add(addressData);
            }
        }
        adapter.notifyDataSetChanged();
    }
}

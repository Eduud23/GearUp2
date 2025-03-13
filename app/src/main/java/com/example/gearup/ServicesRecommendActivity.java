package com.example.gearup;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServicesRecommendActivity extends AppCompatActivity {

    private EditText queryInput;
    private Button predictButton;
    private TextView resultView;
    private RecyclerView recyclerView;
    private RecommendLocalShopAdapter adapter;
    private List<RecommendLocalShop> shopList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();
    private static final String API_KEY = "AIzaSyAqN2a7lbuzQGe20b8cZ6UhMF2K9jHAIHs";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + API_KEY;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude;
    private double userLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_recommend);

        queryInput = findViewById(R.id.queryInput);
        predictButton = findViewById(R.id.predictButton);
        resultView = findViewById(R.id.resultView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new RecommendLocalShopAdapter(shopList, this, shop -> {});
        recyclerView.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocation();

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userQuery = queryInput.getText().toString().trim();
                if (!userQuery.isEmpty()) {
                    new Thread(() -> {
                        String prediction = askGemini(userQuery).trim();
                        runOnUiThread(() -> {
                            resultView.setText(prediction);
                            DisplayMethodServices.getAutoPartsShops(ServicesRecommendActivity.this, shops -> {
                                List<RecommendLocalShop> filteredShops = new ArrayList<>();
                                for (RecommendLocalShop shop : shops) {
                                    if (shop.getKindOfService().equalsIgnoreCase(prediction)) {
                                        Object websiteField = shop.getWebsite();
                                        if (websiteField != null) {
                                            shop.setWebsite(websiteField.toString());
                                        }
                                        // Calculate distance
                                        float[] results = new float[1];
                                        Location.distanceBetween(userLatitude, userLongitude, shop.getLatitude(), shop.getLongitude(), results);
                                        shop.setDistance(results[0]);
                                        filteredShops.add(shop);
                                    }
                                }
                                // Sort by distance
                                Collections.sort(filteredShops, Comparator.comparingDouble(RecommendLocalShop::getDistance));
                                RecommendLocalShopAdapter newAdapter = new RecommendLocalShopAdapter(filteredShops, ServicesRecommendActivity.this, shop -> {});
                                recyclerView.setAdapter(newAdapter);
                            });
                        });
                    }).start();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    userLatitude = location.getLatitude();
                    userLongitude = location.getLongitude();
                }
            }
        });
    }

    private String askGemini(String prompt) {
        String formattedPrompt = String.format(
                "The user describes a vehicle-related issue. Categorize it into one of these services:\n" +
                        "- Car Repair\n" +
                        "- Auto Parts Store\n" +
                        "- Fuel Station\n" +
                        "- Towing Services\n" +
                        "If unrelated, respond with 'Error: No relevant services found.'\n" +
                        "User Query: \"%s\"\nProvide only the service category.",
                prompt
        );

        try {
            JSONObject jsonRequest = new JSONObject().put("contents", new JSONObject().put("parts", new JSONObject().put("text", formattedPrompt)));
            RequestBody body = RequestBody.create(jsonRequest.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(GEMINI_URL).post(body).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                JSONObject jsonResponse = new JSONObject(response.body().string());
                return jsonResponse.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text").trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}

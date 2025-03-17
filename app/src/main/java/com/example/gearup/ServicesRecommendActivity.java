package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
    private RecommendLocalShopAdapter shopAdapter;
    private RecommendGasStationAdapter gasStationAdapter;
    private RecommendTowingAdapter towingAdapter;
    private final OkHttpClient client = new OkHttpClient();
    private static final String API_KEY = "AIzaSyAqN2a7lbuzQGe20b8cZ6UhMF2K9jHAIHs";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + API_KEY;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude = 0.0;
    private double userLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_recommend);

        queryInput = findViewById(R.id.queryInput);
        predictButton = findViewById(R.id.predictButton);
        resultView = findViewById(R.id.resultView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocation();

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userQuery = queryInput.getText().toString().trim();
                if (!userQuery.isEmpty()) {
                    if (userLatitude != 0.0 && userLongitude != 0.0) {
                        makePrediction(userQuery);
                    } else {
                        getUserLocation();
                    }
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

    private void makePrediction(String userQuery) {
        new Thread(() -> {
            String prediction = askGemini(userQuery).trim();
            runOnUiThread(() -> {
                resultView.setText(prediction);
                List<RecommendLocalShop> combinedShops = new ArrayList<>();
                List<RecommendGasStation> gasStations = new ArrayList<>();
                List<RecommendTowing> towingServices = new ArrayList<>();

                DisplayMethodServices.getAutoPartsShops(ServicesRecommendActivity.this, autoPartsShops -> {
                    combinedShops.addAll(autoPartsShops);
                    DisplayMethodServices.getLocalRepair(ServicesRecommendActivity.this, localRepairShops -> {
                        combinedShops.addAll(localRepairShops);
                        DisplayMethodServices.getGasStation(ServicesRecommendActivity.this, fetchedGasStations -> {
                            gasStations.addAll(fetchedGasStations);
                            DisplayMethodServices.getTowing(ServicesRecommendActivity.this, fetchedTowing -> {
                                towingServices.addAll(fetchedTowing);

                                List<String> addedNames = new ArrayList<>();
                                List<RecommendLocalShop> filteredShops = new ArrayList<>();
                                List<RecommendGasStation> filteredGasStations = new ArrayList<>();
                                List<RecommendTowing> filteredTowing = new ArrayList<>();

                                String[] predictedServices = prediction.split(",");
                                for (String service : predictedServices) {
                                    service = service.trim();
                                    if (service.equalsIgnoreCase("Gas station")) {
                                        for (RecommendGasStation station : gasStations) {
                                            if (!addedNames.contains(station.getName())) {
                                                float[] results = new float[1];
                                                Location.distanceBetween(userLatitude, userLongitude, station.getLatitude(), station.getLongitude(), results);
                                                station.setDistance(results[0]);
                                                filteredGasStations.add(station);
                                                addedNames.add(station.getName());
                                            }
                                        }
                                    } else if (service.equalsIgnoreCase("Towing service")) {
                                        for (RecommendTowing towing : towingServices) {
                                            if (!addedNames.contains(towing.getShopName())) {
                                                float[] results = new float[1];
                                                Location.distanceBetween(userLatitude, userLongitude, towing.getLatitude(), towing.getLongitude(), results);
                                                towing.setDistance(results[0]);
                                                filteredTowing.add(towing);
                                                addedNames.add(towing.getShopName());
                                            }
                                        }
                                    } else {
                                        for (RecommendLocalShop shop : combinedShops) {
                                            String kindOfService = shop.getKindOfService();
                                            if (kindOfService != null && kindOfService.equalsIgnoreCase(service)) {
                                                if (!addedNames.contains(shop.getShopName())) {
                                                    float[] results = new float[1];
                                                    Location.distanceBetween(userLatitude, userLongitude, shop.getLatitude(), shop.getLongitude(), results);
                                                    shop.setDistance(results[0]);
                                                    filteredShops.add(shop);
                                                    addedNames.add(shop.getShopName());
                                                }
                                            }
                                        }
                                    }
                                }

                                Collections.sort(filteredShops, Comparator.comparingDouble(RecommendLocalShop::getDistance));
                                Collections.sort(filteredGasStations, Comparator.comparingDouble(RecommendGasStation::getDistance));
                                Collections.sort(filteredTowing, Comparator.comparingDouble(RecommendTowing::getDistance));

                                if (!filteredGasStations.isEmpty()) {
                                    gasStationAdapter = new RecommendGasStationAdapter(ServicesRecommendActivity.this, filteredGasStations);
                                    recyclerView.setAdapter(gasStationAdapter);
                                } else if (!filteredTowing.isEmpty()) {
                                    towingAdapter = new RecommendTowingAdapter(ServicesRecommendActivity.this, filteredTowing);
                                    recyclerView.setAdapter(towingAdapter);
                                } else {
                                    shopAdapter = new RecommendLocalShopAdapter(filteredShops, ServicesRecommendActivity.this);
                                    recyclerView.setAdapter(shopAdapter);
                                }
                            });
                        });
                    });
                });
            });
        }).start();
    }


    private String askGemini(String prompt) {
        String formattedPrompt = String.format(
                "The user describes a vehicle-related issue. Categorize it into one or more of these services, separated by commas if multiple:\n" +
                        "- Auto repair shop\n" +
                        "- Auto Parts Store\n" +
                        "- Motorcycle parts store\n" +
                        "- Auto body parts supplier\n" +
                        "- Gas station\n" +
                        "- Towing Service\n" +
                        "If unrelated, respond with 'Error: No relevant services found.'\n" +
                        "User Query: \"%s\"\n" +
                        "Provide only the service category or categories, separated by commas.",
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

    private void openDetailActivityGas(RecommendLocalShop shop) {
        Intent intent = new Intent(this, ServiceDetailActivity.class);
        intent.putExtra("name", shop.getShopName());
        intent.putExtra("latitude", shop.getLatitude());
        intent.putExtra("longitude", shop.getLongitude());
        intent.putExtra("kindOfService", shop.getKindOfService());
        intent.putExtra("timeSchedule", shop.getTimeSchedule());
        intent.putExtra("contactNumber", shop.getContactNumber());
        intent.putExtra("ratings", shop.getRatings());
        intent.putExtra("website", shop.getWebsite());
        intent.putExtra("image", shop.getImage());
        intent.putExtra("distance", shop.getDistance());
        startActivity(intent);
    }
    private void openDetailActivity(RecommendLocalShop shop) {
        Intent intent = new Intent(this, ServiceDetailActivity.class);
        intent.putExtra("name", shop.getShopName());
        intent.putExtra("latitude", shop.getLatitude());
        intent.putExtra("longitude", shop.getLongitude());
        intent.putExtra("kindOfService", shop.getKindOfService());
        intent.putExtra("timeSchedule", shop.getTimeSchedule());
        intent.putExtra("contactNumber", shop.getContactNumber());
        intent.putExtra("ratings", shop.getRatings());
        intent.putExtra("website", shop.getWebsite());
        intent.putExtra("image", shop.getImage());
        intent.putExtra("distance", shop.getDistance());
        startActivity(intent);
    }


}

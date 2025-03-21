package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
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
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServicesRecommendActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private EditText queryInput;
    private Button predictButton;
    private TextView resultView;
    private RecyclerView recyclerView;
    private RecommendCombinedAdapter combinedAdapter;
    private final OkHttpClient client = new OkHttpClient();
    private static final String VERCEL_URL = "https://test-sage-nine-37.vercel.app/?q=";
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

        predictButton.setOnClickListener(v -> {
            String userQuery = queryInput.getText().toString().trim();
            if (!userQuery.isEmpty()) {
                if (userLatitude != 0.0 && userLongitude != 0.0) {
                    makePrediction(userQuery);
                } else {
                    getUserLocation();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLatitude = location.getLatitude();
                    userLongitude = location.getLongitude();
                }
            });
        } else {
            // Request location permissions
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getUserLocation();
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                resultView.setText("Location permission is required to get nearby services.");
            }
        }
    }

    private void makePrediction(String userQuery) {
        new Thread(() -> {
            String prediction = askVercel(userQuery).trim();
            runOnUiThread(() -> {
                resultView.setText(prediction);
                List<RecommendLocalShop> combinedShops = new ArrayList<>();
                List<RecommendGasStation> gasStations = new ArrayList<>();
                List<RecommendTowing> towingServices = new ArrayList<>();
                List<RecommendSmokeService> smokeServices = new ArrayList<>();
                List<RecommendParking> parkingLot = new ArrayList<>();

                DisplayMethodServices.getAutoPartsShops(this, autoPartsShops -> {
                    combinedShops.addAll(autoPartsShops);
                    DisplayMethodServices.getLocalRepair(this, localRepairShops -> {
                        combinedShops.addAll(localRepairShops);
                        DisplayMethodServices.getBatteryShop(this, batteryShop -> {
                            combinedShops.addAll(batteryShop);
                            DisplayMethodServices.getGasStation(this, fetchedGasStations -> {
                                gasStations.addAll(fetchedGasStations);
                                DisplayMethodServices.getTowing(this, fetchedTowing -> {
                                    towingServices.addAll(fetchedTowing);
                                    DisplayMethodServices.getSmokeService(this, fetchedSmoke -> {
                                        smokeServices.addAll(fetchedSmoke);
                                        DisplayMethodServices.getParkingLot(this, fetchedParking -> {
                                            parkingLot.addAll(fetchedParking);

                                        List<Object> combinedList = new ArrayList<>();
                                        List<String> addedNames = new ArrayList<>();

                                        String[] predictedServices = prediction.split(",");
                                        for (String service : predictedServices) {
                                            service = service.trim();
                                            if (service.equalsIgnoreCase("Gas station")) {
                                                for (RecommendGasStation station : gasStations) {
                                                    if (!addedNames.contains(station.getName())) {
                                                        float[] results = new float[1];
                                                        Location.distanceBetween(userLatitude, userLongitude, station.getLatitude(), station.getLongitude(), results);
                                                        station.setDistance(results[0]);
                                                        combinedList.add(station);
                                                        addedNames.add(station.getName());
                                                    }
                                                }
                                            } else if (service.equalsIgnoreCase("Towing service")) {
                                                for (RecommendTowing towing : towingServices) {
                                                    if (!addedNames.contains(towing.getShopName())) {
                                                        float[] results = new float[1];
                                                        Location.distanceBetween(userLatitude, userLongitude, towing.getLatitude(), towing.getLongitude(), results);
                                                        towing.setDistance(results[0]);
                                                        combinedList.add(towing);
                                                        addedNames.add(towing.getShopName());
                                                    }
                                                }
                                            } else if (service.equalsIgnoreCase("Parking lot") ||
                                                    service.equalsIgnoreCase("Public parking space") ||
                                                    service.equalsIgnoreCase("Free parking lot") ||
                                                    service.equalsIgnoreCase("Parking garage") ||
                                                    service.equalsIgnoreCase("Parking lot for motorcycles")) {
                                                for (RecommendParking parking : parkingLot) {
                                                    if (!addedNames.contains(parking.getShopName())) {
                                                        float[] results = new float[1];
                                                        Location.distanceBetween(userLatitude, userLongitude, parking.getLatitude(), parking.getLongitude(), results);
                                                        parking.setDistance(results[0]);
                                                        combinedList.add(parking);
                                                        addedNames.add(parking.getShopName());
                                                    }
                                                }
                                            }

                                            else if (service.equalsIgnoreCase("Vehicle inspection service") || service.equalsIgnoreCase("Smog inspection station")) {
                                                for (RecommendParking parking : parkingLot) {
                                                    if (!addedNames.contains(parking.getShopName())) {
                                                        float[] results = new float[1];
                                                        Location.distanceBetween(userLatitude, userLongitude, parking.getLatitude(), parking.getLongitude(), results);
                                                        parking.setDistance(results[0]);
                                                        combinedList.add(parking);
                                                        addedNames.add(parking.getShopName());
                                                    }
                                                }
                                            }else {
                                                for (RecommendLocalShop shop : combinedShops) {
                                                    String kindOfService = shop.getKindOfService();
                                                    if (kindOfService != null && kindOfService.equalsIgnoreCase(service)) {
                                                        if (!addedNames.contains(shop.getShopName())) {
                                                            float[] results = new float[1];
                                                            Location.distanceBetween(userLatitude, userLongitude, shop.getLatitude(), shop.getLongitude(), results);
                                                            shop.setDistance(results[0]);
                                                            combinedList.add(shop);
                                                            addedNames.add(shop.getShopName());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        combinedAdapter = new RecommendCombinedAdapter(ServicesRecommendActivity.this, combinedList, null);
                                        recyclerView.setAdapter(combinedAdapter);
                                    });
                                });
                            });
                        });
                    });
                });
            });
            });
        }).start();
    }

    private String askVercel(String prompt) {
        try {
            Request request = new Request.Builder()
                    .url(VERCEL_URL + prompt)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                JSONObject jsonResponse = new JSONObject(response.body().string());
                return jsonResponse.optString("prediction", "Error: No relevant services found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}

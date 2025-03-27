package com.example.gearup;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecommendedServicesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecommendCombinedAdapter combinedAdapter;
    private TextView predictionView;
    private double userLatitude, userLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended_services);

        recyclerView = findViewById(R.id.recommendedServicesRecyclerView);
        predictionView = findViewById(R.id.predictionView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Get intent data
        Intent intent = getIntent();
        String prediction = intent.getStringExtra("prediction");
        userLatitude = intent.getDoubleExtra("userLatitude", 0.0);
        userLongitude = intent.getDoubleExtra("userLongitude", 0.0);

        predictionView.setText(prediction);

        // Fetch all services
        List<Object> combinedList = new ArrayList<>();
        List<String> addedNames = new ArrayList<>();

        String[] predictedServices = prediction.split(",");
        DisplayMethodServices.getAutoPartsShops(this, autoPartsShops -> {
            combinedList.addAll(autoPartsShops);
            DisplayMethodServices.getLocalRepair(this, localRepairShops -> {
                combinedList.addAll(localRepairShops);
                DisplayMethodServices.getBatteryShop(this, batteryShop -> {
                    combinedList.addAll(batteryShop);
                    DisplayMethodServices.getGasStation(this, gasStations -> {
                        combinedList.addAll(gasStations);
                        DisplayMethodServices.getTowing(this, towingServices -> {
                            combinedList.addAll(towingServices);
                            DisplayMethodServices.getSmokeService(this, smokeServices -> {
                                combinedList.addAll(smokeServices);
                                DisplayMethodServices.getParkingLot(this, parkingLot -> {
                                    combinedList.addAll(parkingLot);

                                    // Filter and calculate distances
                                    List<Object> filteredList = new ArrayList<>();
                                    for (String service : predictedServices) {
                                        service = service.trim();
                                        for (Object item : combinedList) {
                                            if (item instanceof RecommendLocalShop && ((RecommendLocalShop) item).getKindOfService().equalsIgnoreCase(service)) {
                                                if (!addedNames.contains(((RecommendLocalShop) item).getShopName())) {
                                                    calculateDistanceAndAdd(item, filteredList, addedNames);
                                                }
                                            } else if (item instanceof RecommendGasStation && service.equalsIgnoreCase("Gas station")) {
                                                calculateDistanceAndAdd(item, filteredList, addedNames);
                                            } else if (item instanceof RecommendTowing && service.equalsIgnoreCase("Towing service")) {
                                                calculateDistanceAndAdd(item, filteredList, addedNames);
                                            } else if (item instanceof RecommendParking && service.toLowerCase().contains("parking")) {
                                                calculateDistanceAndAdd(item, filteredList, addedNames);
                                            } else if (item instanceof RecommendSmokeService && (service.equalsIgnoreCase("Vehicle inspection service") || service.equalsIgnoreCase("Smog inspection station"))) {
                                                calculateDistanceAndAdd(item, filteredList, addedNames);
                                            }
                                        }
                                    }

                                    combinedAdapter = new RecommendCombinedAdapter(RecommendedServicesActivity.this, filteredList, null);
                                    recyclerView.setAdapter(combinedAdapter);
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    private void calculateDistanceAndAdd(Object item, List<Object> filteredList, List<String> addedNames) {
        float[] results = new float[1];
        if (item instanceof RecommendLocalShop) {
            RecommendLocalShop shop = (RecommendLocalShop) item;
            Location.distanceBetween(userLatitude, userLongitude, shop.getLatitude(), shop.getLongitude(), results);
            shop.setDistance(results[0]);
            addedNames.add(shop.getShopName());
            filteredList.add(shop);
        } else if (item instanceof RecommendGasStation) {
            RecommendGasStation station = (RecommendGasStation) item;
            Location.distanceBetween(userLatitude, userLongitude, station.getLatitude(), station.getLongitude(), results);
            station.setDistance(results[0]);
            addedNames.add(station.getName());
            filteredList.add(station);
        } else if (item instanceof RecommendTowing) {
            RecommendTowing towing = (RecommendTowing) item;
            Location.distanceBetween(userLatitude, userLongitude, towing.getLatitude(), towing.getLongitude(), results);
            towing.setDistance(results[0]);
            addedNames.add(towing.getShopName());
            filteredList.add(towing);
        } else if (item instanceof RecommendParking) {
            RecommendParking parking = (RecommendParking) item;
            Location.distanceBetween(userLatitude, userLongitude, parking.getLatitude(), parking.getLongitude(), results);
            parking.setDistance(results[0]);
            addedNames.add(parking.getShopName());
            filteredList.add(parking);
        } else if (item instanceof RecommendSmokeService) {
            RecommendSmokeService smoke = (RecommendSmokeService) item;
            Location.distanceBetween(userLatitude, userLongitude, smoke.getLatitude(), smoke.getLongitude(), results);
            smoke.setDistance(results[0]);
            addedNames.add(smoke.getShopName());
            filteredList.add(smoke);
        }
    }
}
package com.example.gearup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocalShopsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LocalShopAdapter shopAdapter;
    private List<LocalShop> shopList;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude = 0.0;
    private double userLongitude = 0.0;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final String TAG = "LocalShopsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_shops, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewLocalShops);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        shopList = new ArrayList<>();
        shopAdapter = new LocalShopAdapter(shopList, getContext());
        recyclerView.setAdapter(shopAdapter);

        // Initialize Firestore correctly
        try {
            FirebaseApp secondApp = FirebaseApp.getInstance("gearupdataSecondApp");
            db = FirebaseFirestore.getInstance(secondApp);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firestore", e);
        }

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Request location
        getUserLocation();

        return view;
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request location permissions
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();
                Log.d(TAG, "User Location: " + userLatitude + ", " + userLongitude);

                // Load shops after getting location
                loadLocalShops();
            } else {
                Log.e(TAG, "Failed to get location.");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error getting location", e));
    }

    private void loadLocalShops() {
        db.collection("auto_parts_shops").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                shopList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        String shopName = document.getString("shopName");

                        // Handle `image` field properly
                        Object imageField = document.get("image");
                        String image = imageField instanceof String ? (String) imageField : "";

                        String kindOfRepair = document.getString("kindOfRepair");
                        String timeSchedule = document.getString("timeSchedule");
                        String place = document.getString("place");
                        String contactNumber = document.getString("contactNumber");

                        double ratings = document.getDouble("ratings") != null ? document.getDouble("ratings") : 0.0;

                        // Handle `website` field safely
                        Object websiteObj = document.get("website");
                        String website = websiteObj instanceof String ? (String) websiteObj : "";

                        // Handle `latitude` and `longitude` safely
                        Double latitudeObj = document.getDouble("latitude");
                        Double longitudeObj = document.getDouble("longitude");
                        double latitude = latitudeObj != null ? latitudeObj : 0.0;
                        double longitude = longitudeObj != null ? longitudeObj : 0.0;

                        double distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);

                        // Add shop to the list
                        shopList.add(new LocalShop(shopName, image, kindOfRepair, timeSchedule, place, contactNumber, ratings, website, latitude, longitude, distance));
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing document: " + document.getId(), e);
                    }
                }

                // Sort shops from nearest to farthest
                Collections.sort(shopList, Comparator.comparingDouble(LocalShop::getDistance));
                shopAdapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "Error getting shops: ", task.getException());
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Firestore request failed", e));
    }

    // Haversine formula to calculate distance in km
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0.0 && lon1 == 0.0) {
            return Double.MAX_VALUE; // If user location is not available, push these shops to the end
        }

        final int R = 6371; // Radius of the Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            Log.e(TAG, "Location permission denied.");
        }
    }
}

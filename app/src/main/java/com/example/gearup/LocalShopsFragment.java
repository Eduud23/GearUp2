package com.example.gearup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
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

import com.google.android.gms.location.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocalShopsFragment extends Fragment implements LocalShopAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private LocalShopAdapter shopAdapter;
    private List<LocalShop> shopList;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final String TAG = "LocalShopsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_shops, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewLocalShops);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        shopList = new ArrayList<>();
        shopAdapter = new LocalShopAdapter(shopList, getContext(), this);
        recyclerView.setAdapter(shopAdapter);

        // Initialize Firestore
        try {
            FirebaseApp secondApp = FirebaseApp.getInstance("gearupdataSecondApp");
            db = FirebaseFirestore.getInstance(secondApp);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firestore", e);
        }

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Setup location request for fresh updates
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000)
                .setNumUpdates(1);

        // Get the user’s location
        getUserLocation();

        return view;
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                updateLocationAndLoadShops(location);
            } else {
                Log.e(TAG, "Last known location is null. Requesting fresh location...");
                requestNewLocationData();
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error getting last location", e));
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    updateLocationAndLoadShops(location);
                } else {
                    Log.e(TAG, "Failed to get fresh location update.");
                }
            }
        }, Looper.getMainLooper());
    }

    private void updateLocationAndLoadShops(Location location) {
        if (location != null) {
            double userLatitude = location.getLatitude();
            double userLongitude = location.getLongitude();
            Log.d(TAG, "User Location: " + userLatitude + ", " + userLongitude);
            loadLocalShops(userLatitude, userLongitude);
        } else {
            Log.e(TAG, "Location is null.");
        }
    }

    private void loadLocalShops(double userLatitude, double userLongitude) {
        db.collection("auto_parts_shops").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                shopList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        String shopName = document.getString("shop_name");
                        Object imageField = document.get("image");
                        String image = imageField instanceof String ? (String) imageField : "";
                        String kindOfRepair = document.getString("kind_of_service");
                        String timeSchedule = document.getString("time_schedule");
                        String place = document.getString("place");
                        Object contactObj = document.get("contact_number");
                        String contactNumber = (contactObj instanceof String) ? (String) contactObj : String.valueOf(contactObj);
                        double ratings = document.getDouble("ratings") != null ? document.getDouble("ratings") : 0.0;
                        Object websiteObj = document.get("website");
                        String website = websiteObj instanceof String ? (String) websiteObj : "";
                        Double latitudeObj = document.getDouble("latitude");
                        Double longitudeObj = document.getDouble("longitude");
                        double latitude = latitudeObj != null ? latitudeObj : 0.0;
                        double longitude = longitudeObj != null ? longitudeObj : 0.0;
                        double distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);

                        shopList.add(new LocalShop(shopName, image, kindOfRepair, timeSchedule, place, contactNumber, ratings, website, latitude, longitude, distance));
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing document: " + document.getId(), e);
                    }
                }

                Collections.sort(shopList, Comparator.comparingDouble(LocalShop::getDistance));
                shopAdapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "Error getting shops: ", task.getException());
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Firestore request failed", e));
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0.0 && lon1 == 0.0) {
            return Double.MAX_VALUE;
        }
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            Log.e(TAG, "Location permission denied.");
        }
    }

    @Override
    public void onItemClick(LocalShop shop) {
        Intent intent = new Intent(getContext(), LocalShopDetailsActivity.class);
        intent.putExtra("shopName", shop.getShopName());
        intent.putExtra("kindOfRepair", shop.getKindOfRepair());
        intent.putExtra("timeSchedule", shop.getTimeSchedule());
        intent.putExtra("place", shop.getPlace());
        intent.putExtra("ratings", shop.getRatings());
        intent.putExtra("image", shop.getImage());
        intent.putExtra("contactNumber", shop.getContactNumber());
        intent.putExtra("website", shop.getWebsite());
        intent.putExtra("latitude", shop.getLatitude());
        intent.putExtra("longitude", shop.getLongitude());
        intent.putExtra("distance", shop.getDistance());
        requireContext().startActivity(intent);
    }
}

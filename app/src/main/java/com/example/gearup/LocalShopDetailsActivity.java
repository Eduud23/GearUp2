package com.example.gearup;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class LocalShopDetailsActivity extends AppCompatActivity {

    private ImageView shopImage;
    private TextView shopName, kindOfRepair, timeSchedule, place, ratings, distanceTextView;
    private ImageButton callButton, websiteButton, navigateButton;
    private RecyclerView similarShopsRecyclerView;
    private LocalShopAdapter similarShopsAdapter;
    private List<LocalShop> similarShopsList;
    private FirebaseFirestore db;

    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0.0, currentLongitude = 0.0;

    private static final int LOCATION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_shop_details);

        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Initialize UI components
        shopImage = findViewById(R.id.shopImage);
        shopName = findViewById(R.id.shopName);
        distanceTextView = findViewById(R.id.distance);

        kindOfRepair = findViewById(R.id.kindOfRepair);
        timeSchedule = findViewById(R.id.timeSchedule);
        place = findViewById(R.id.place);
        ratings = findViewById(R.id.ratings);
        callButton = findViewById(R.id.callButton);
        websiteButton = findViewById(R.id.websiteButton);
        navigateButton = findViewById(R.id.navigateButton);

        similarShopsRecyclerView = findViewById(R.id.similarShopsRecyclerView);
        similarShopsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        similarShopsList = new ArrayList<>();
        similarShopsAdapter = new LocalShopAdapter(similarShopsList, this, shop -> {
            // Handle similar shop click
            Intent intent = new Intent(LocalShopDetailsActivity.this, LocalShopDetailsActivity.class);
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
            startActivity(intent);
        });

        similarShopsRecyclerView.setAdapter(similarShopsAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check if we have location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("shopName");
            String kind = intent.getStringExtra("kindOfRepair");
            String time = intent.getStringExtra("timeSchedule");
            String location = intent.getStringExtra("place");
            double rating = intent.getDoubleExtra("ratings", 0.0);
            String imageUrl = intent.getStringExtra("image");
            final String contactNumber = intent.getStringExtra("contactNumber");
            final String website = intent.getStringExtra("website");
            final double latitude = intent.getDoubleExtra("latitude", 0.0);
            final double longitude = intent.getDoubleExtra("longitude", 0.0);

            // Set data to views
            shopName.setText(name);
            kindOfRepair.setText(kind);
            timeSchedule.setText(time);
            place.setText(location);
            ratings.setText("Ratings: " + rating);

            // Ensure we get current location before calculating distance
            if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                calculateAndDisplayDistance(latitude, longitude);
            } else {
                distanceTextView.setText("Location unavailable. Please wait...");
            }

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.gear)
                    .error(R.drawable.gear)
                    .into(shopImage);

            // Set visibility of call button based on the contact number
            if (contactNumber == null || contactNumber.isEmpty()) {
                callButton.setVisibility(View.GONE);
            } else {
                callButton.setVisibility(View.VISIBLE);
                callButton.setOnClickListener(v -> {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + contactNumber));
                    startActivity(callIntent);
                });
            }

            // Set visibility of website button based on the website URL
            if (website == null || website.isEmpty() || website.equalsIgnoreCase("none")) {
                websiteButton.setVisibility(View.GONE);
            } else {
                websiteButton.setVisibility(View.VISIBLE);
                websiteButton.setOnClickListener(v -> {
                    if (website != null && !website.isEmpty()) {
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                        startActivity(webIntent);
                    }
                });
            }

            // Navigation button action
            navigateButton.setOnClickListener(v -> {
                String uri = "geo:" + latitude + "," + longitude + "?q=" + Uri.encode(name);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(mapIntent);
            });

            // Fetch similar shops based on the kind of repair or place
            loadSimilarShops(kind);
        }
    }

    // Get the user's current location
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        Log.d(TAG, "Current location: " + currentLatitude + ", " + currentLongitude);

                        // Calculate and display the distance after getting the current location
                        Intent intent = getIntent();
                        if (intent != null) {
                            double latitude = intent.getDoubleExtra("latitude", 0.0);
                            double longitude = intent.getDoubleExtra("longitude", 0.0);
                            calculateAndDisplayDistance(latitude, longitude);
                        }
                    } else {
                        Log.e(TAG, "Failed to get current location");
                        distanceTextView.setText("Unable to fetch location.");
                    }
                });
    }

    // Calculate and display the distance
    private void calculateAndDisplayDistance(double shopLatitude, double shopLongitude) {
        if (currentLatitude != 0.0 && currentLongitude != 0.0) {
            double calculatedDistance = calculateDistance(currentLatitude, currentLongitude, shopLatitude, shopLongitude);
            distanceTextView.setText(String.format("%.2f km", calculatedDistance));
        } else {
            distanceTextView.setText("Location unavailable.");
        }
    }

    // Haversine formula to calculate distance between two points
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Returns the distance in kilometers
    }

    // Load similar shops based on the repair type (or other criteria)
    private void loadSimilarShops(String kindOfRepair) {
        FirebaseFirestore db = null;

        try {
            // Initialize the second Firebase app
            FirebaseApp secondApp = FirebaseApp.getInstance("gearupdataSecondApp");
            db = FirebaseFirestore.getInstance(secondApp);  // Use the second Firebase app's Firestore instance
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firestore", e);
        }

        if (db != null) {
            db.collection("auto_parts_shops")
                    .whereEqualTo("kind_of_service", kindOfRepair)  // Adjust the criteria if needed
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            similarShopsList.clear();  // Clear previous similar shops before adding new ones

                            // Get the current shop's name (you can use the shopName or a unique ID if preferred)
                            String currentShopName = getIntent().getStringExtra("shopName");

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String shopName = document.getString("shop_name");

                                // Skip the current shop from being displayed in similar shops
                                if (shopName != null && shopName.equals(currentShopName)) {
                                    continue;  // Skip this iteration if the shop name matches the current shop
                                }

                                String image = document.getString("image");
                                String kind = document.getString("kind_of_service");
                                String time = document.getString("time_schedule");
                                String place = document.getString("place");
                                String contactNumber = document.getString("contact_number");
                                String website = document.getString("website");
                                double latitude = document.getDouble("latitude");
                                double longitude = document.getDouble("longitude");

                                // Handle ratings field: check if it's a valid number
                                double ratings = 0.0;  // Default value
                                Object ratingsObject = document.get("ratings");
                                if (ratingsObject instanceof Double) {
                                    ratings = (Double) ratingsObject;
                                } else if (ratingsObject instanceof Long) {
                                    ratings = ((Long) ratingsObject).doubleValue();
                                } else if (ratingsObject instanceof String) {
                                    try {
                                        ratings = Double.parseDouble((String) ratingsObject);
                                    } catch (NumberFormatException e) {
                                        ratings = 0.0;
                                    }
                                }

                                // Calculate distance from user's current location
                                double distance = calculateDistance(currentLatitude, currentLongitude, latitude, longitude);

                                // Add to the list of similar shops
                                similarShopsList.add(new LocalShop(shopName, image, kind, time, place, contactNumber, ratings, website, latitude, longitude, distance));
                            }

                            // Notify the adapter of the data change
                            similarShopsAdapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Error getting shops: ", task.getException());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firestore request failed", e);
                    });
        }
    }


}

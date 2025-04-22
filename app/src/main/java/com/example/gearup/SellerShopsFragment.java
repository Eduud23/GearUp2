package com.example.gearup;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SellerShopsFragment extends Fragment implements ShopsFragment.Searchable {

    private RecyclerView recyclerView;
    private ShopAdapter shopAdapter;
    private List<Shop> shopList;
    private List<Shop> originalShopList;  // To store the full list for filtering
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude, userLongitude;
    private boolean isDataLoaded = false;  // Flag to track if data has been loaded

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_shops, container, false);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerViewSellerShops);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        shopList = new ArrayList<>();
        originalShopList = new ArrayList<>();

        // Initialize the adapter with a lambda to handle shop clicks
        shopAdapter = new ShopAdapter(shopList, sellerId -> {
            Intent intent = new Intent(getContext(), SellerShopActivity.class);
            intent.putExtra("SELLER_ID", sellerId);  // Pass seller ID to new activity
            startActivity(intent);
        }, requireContext());

        recyclerView.setAdapter(shopAdapter);

        // Initialize Location Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        loadUserLocation(); // Load user location when the fragment is created

        return view;
    }

    // Get user's current location
    private void loadUserLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Handle permission request (assuming permission is handled outside of this code)
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                        loadShopsFromFirestore(); // Load shops from Firestore after location is obtained
                    }
                });
    }

    private void loadShopsFromFirestore() {
        db.collection("sellers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String shopName = document.getString("shopName");
                    String address = document.getString("address");
                    String phone = document.getString("phone");
                    String sellerId = document.getId();  // Get seller ID from Firestore
                    String profileImageUrl = document.getString("profileImageUrl");

                    if (profileImageUrl == null) {
                        profileImageUrl = "default_image_url_here";  // Replace with actual default image URL
                    }

                    double sellerLatitude = document.contains("latitude") ? document.getDouble("latitude") : 0.0;
                    double sellerLongitude = document.contains("longitude") ? document.getDouble("longitude") : 0.0;

                    float distanceInKm = -1;  // -1 indicates distance is unavailable

                    if (sellerLatitude != 0.0 && sellerLongitude != 0.0) {
                        // Calculate the distance between the user and the seller
                        float[] results = new float[1];
                        Location.distanceBetween(userLatitude, userLongitude, sellerLatitude, sellerLongitude, results);
                        float distanceInMeters = results[0];
                        distanceInKm = distanceInMeters / 1000;  // Convert to kilometers
                    }

                    Shop shop = new Shop(shopName, address, phone, sellerId, profileImageUrl, distanceInKm);
                    shopList.add(shop);  // Add to the displayed list
                    originalShopList.add(shop);  // Add to the original list for filtering
                }

                // Sort the list by distance from nearest to farthest
                shopList.sort((shop1, shop2) -> {
                    if (shop1.getDistance() == -1) return 1;  // Treat -1 as farthest
                    if (shop2.getDistance() == -1) return -1;  // Treat -1 as farthest
                    return Float.compare(shop1.getDistance(), shop2.getDistance());
                });

                shopAdapter.notifyDataSetChanged();
                isDataLoaded = true;  // Set flag to true after data is loaded
                Log.d(TAG, "Data loaded successfully.");
            } else {
                Log.e(TAG, "Error loading shops: ", task.getException());
            }
        });
    }

    @Override
    public void searchShops(String query) {
        if (!isDataLoaded) {
            Log.d(TAG, "Data not loaded yet, returning from search.");
            return;  // Do not perform search if data is not loaded
        }

        if (originalShopList == null || shopList == null) {
            Log.d(TAG, "Shop list is null, returning from search.");
            return;
        }

        // If search query is empty, reset to show all shops
        if (query.trim().isEmpty()) {
            // Clear the shopList and add all items from originalShopList to it
            shopList.clear();
            shopList.addAll(originalShopList);

            // Notify the adapter about the data change
            shopAdapter.notifyDataSetChanged();
            Log.d(TAG, "Search query is empty, showing all shops.");
            return;
        }

        List<Shop> filteredList = new ArrayList<>();
        String queryLower = query.toLowerCase();
        Log.d(TAG, "Search started with query: " + queryLower);

        // Iterate through the original shop list and match based on query
        for (Shop shop : originalShopList) {
            boolean matches = false;

            // Check if shop name matches (case-insensitive)
            if (shop.getShopName() != null && shop.getShopName().toLowerCase().contains(queryLower)) {
                matches = true;
                Log.d(TAG, "Match found in shop name: " + shop.getShopName());
            }

            // Check if address matches (case-insensitive)
            if (shop.getAddress() != null && shop.getAddress().toLowerCase().contains(queryLower)) {
                matches = true;
                Log.d(TAG, "Match found in shop address: " + shop.getAddress());
            }

            // Check if phone number matches (case-insensitive)
            if (shop.getPhone() != null && shop.getPhone().toLowerCase().contains(queryLower)) {
                matches = true;
                Log.d(TAG, "Match found in shop phone number: " + shop.getPhone());
            }

            // If any condition matched, add shop to filtered list
            if (matches) {
                filteredList.add(shop);
                Log.d(TAG, "Shop added to filtered list: " + shop.getShopName());
            }
        }

        // Update shop list with the filtered results
        shopList.clear();
        shopList.addAll(filteredList);
        shopAdapter.notifyDataSetChanged();

        // Log the final size of the filtered list after search
        Log.d(TAG, "Search completed. Filtered list size: " + filteredList.size());

        // Log the query passed if any match was found
        if (!filteredList.isEmpty()) {
            Log.d(TAG, "Search query matched, showing " + filteredList.size() + " results.");
        } else {
            Log.d(TAG, "No match found for query: " + query);
        }
    }



    // Implement the resetToAllShops method to reset the list
    @Override
    public void resetToAllShops() {
        if (originalShopList != null && shopList != null) {
            shopList.clear();
            shopList.addAll(originalShopList);
            shopAdapter.notifyDataSetChanged();
            Log.d(TAG, "Shop list reset to all items.");
        }
    }
}

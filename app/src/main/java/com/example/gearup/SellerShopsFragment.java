package com.example.gearup;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SellerShopsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ShopAdapter shopAdapter;
    private List<Shop> shopList;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude, userLongitude;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_shops, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewSellerShops);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        shopList = new ArrayList<>();
        shopAdapter = new ShopAdapter(shopList, sellerId -> {
            Intent intent = new Intent(getContext(), SellerShopActivity.class);
            intent.putExtra("SELLER_ID", sellerId); // Pass seller ID to new activity
            startActivity(intent);
        }, requireContext());

        recyclerView.setAdapter(shopAdapter);

        loadUserLocation();

        return view;
    }

    // Get user's current location
    private void loadUserLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted. Handle accordingly
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            userLatitude = location.getLatitude();
                            userLongitude = location.getLongitude();
                            loadShopsFromFirestore(); // Once location is obtained, load shops
                        }
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
                    String sellerId = document.getId(); // Get seller ID from Firestore
                    String profileImageUrl = document.getString("profileImageUrl");

                    if (profileImageUrl == null) {
                        profileImageUrl = "default_image_url_here"; // Replace with actual default image URL
                    }

                    double sellerLatitude = document.contains("latitude") ? document.getDouble("latitude") : 0.0;
                    double sellerLongitude = document.contains("longitude") ? document.getDouble("longitude") : 0.0;

                    float distanceInKm = -1;  // -1 indicates distance is unavailable

                    if (sellerLatitude != 0.0 && sellerLongitude != 0.0) {
                        // Calculate the distance between the user and the seller
                        float[] results = new float[1];
                        Location.distanceBetween(userLatitude, userLongitude, sellerLatitude, sellerLongitude, results);
                        float distanceInMeters = results[0];
                        distanceInKm = distanceInMeters / 1000; // Convert to kilometers
                    }

                    shopList.add(new Shop(shopName, address, phone, sellerId, profileImageUrl, distanceInKm));
                }

                // Sort the list by distance from nearest to farthest (handling shops with no distance data)
                shopList.sort((shop1, shop2) -> {
                    if (shop1.getDistance() == -1) return 1; // Treat -1 as farthest
                    if (shop2.getDistance() == -1) return -1; // Treat -1 as farthest
                    return Float.compare(shop1.getDistance(), shop2.getDistance());
                });

                shopAdapter.notifyDataSetChanged();
            }
        });
    }
}

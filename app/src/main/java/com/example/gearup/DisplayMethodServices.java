package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DisplayMethodServices {
    private static final String TAG = "DisplayMethodServices";

    public interface FirestoreCallback {
        void onCallback(List<RecommendLocalShop> shops);
    }

    @SuppressLint("MissingPermission")
    public static Location getUserLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }

    public static void getAutoPartsShops(Context context, FirestoreCallback callback) {
        FirebaseApp secondApp = FirebaseApp.getInstance("gearupdataSecondApp");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance(secondApp);

        firestore.collection("auto_parts_shops").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<RecommendLocalShop> shopList = new ArrayList<>();
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            for (QueryDocumentSnapshot document : result) {
                                RecommendLocalShop shop = new RecommendLocalShop();
                                shop.setShopName(document.getString("shop_name"));
                                shop.setPlace(document.getString("place"));
                                shop.setKindOfService(document.getString("kind_of_service"));
                                shop.setTimeSchedule(document.get("time_schedule") != null ? document.get("time_schedule").toString() : "");
                                shop.setContactNumber(document.get("contact_number") != null ? document.get("contact_number").toString() : "");
                                try {
                                    shop.setRatings(document.get("ratings") != null && !document.get("ratings").toString().equalsIgnoreCase("none") ? Double.parseDouble(document.get("ratings").toString()) : 0.0);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid ratings format", e);
                                    shop.setRatings(0.0);
                                }
                                shop.setWebsite(document.get("website") != null ? document.get("website").toString() : "");
                                try {
                                    shop.setLatitude(document.get("latitude") != null ? Double.parseDouble(document.get("latitude").toString()) : 0.0);
                                    shop.setLongitude(document.get("longitude") != null ? Double.parseDouble(document.get("longitude").toString()) : 0.0);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid latitude/longitude format", e);
                                    shop.setLatitude(0.0);
                                    shop.setLongitude(0.0);
                                }
                                shop.setImage(document.get("image") != null ? document.get("image").toString() : "");

                                shopList.add(shop);
                            }

                            // Get user location
                            Location userLocation = getUserLocation(context);
                            if (userLocation != null) {
                                for (RecommendLocalShop shop : shopList) {
                                    Location shopLocation = new Location("");
                                    shopLocation.setLatitude(shop.getLatitude());
                                    shopLocation.setLongitude(shop.getLongitude());
                                    float distance = userLocation.distanceTo(shopLocation);
                                    shop.setDistance(distance);
                                }
                                // Sort by distance
                                Collections.sort(shopList, Comparator.comparingDouble(RecommendLocalShop::getDistance));
                            } else {
                                Log.e(TAG, "User location is null");
                            }
                            callback.onCallback(shopList);
                        } else {
                            Log.e(TAG, "No data found in auto_parts_shops");
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
}

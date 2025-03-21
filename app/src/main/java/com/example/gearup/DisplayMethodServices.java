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
    public interface FirestoreGasStationCallback {
        void onCallback(List<RecommendGasStation> gasStationList);
    }
    public interface FirestoreSmokeServiceCallback {
        void onCallback(List<RecommendSmokeService> smokeList);
    }
    public interface FirestoreTowingCallback {
        void onCallback(List<RecommendTowing> towingList);
    }
    public interface FirestoreParkingCallback {
        void onCallback(List<RecommendParking> parkingList);
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
                                Object kindOfService = document.get("kind_of_service");
                                shop.setKindOfService(kindOfService != null ? kindOfService.toString() : "");
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
    public static void getLocalRepair(Context context, FirestoreCallback callback) {
        FirebaseApp secondApp = FirebaseApp.getInstance("gearupdataSecondApp");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance(secondApp);

        firestore.collection("car_repair_shops").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<RecommendLocalShop> shopList = new ArrayList<>();
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            for (QueryDocumentSnapshot document : result) {
                                RecommendLocalShop shop = new RecommendLocalShop();
                                shop.setShopName(document.getString("shop_name"));
                                shop.setPlace(document.getString("place"));
                                shop.setKindOfService(document.getString("kind_of_repair"));
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
                            Log.e(TAG, "No data found in auto_repair");
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
    public static void getGasStation(Context context, FirestoreGasStationCallback callback) {
        FirebaseApp secondApp = FirebaseApp.getInstance("gearupdataSecondApp");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance(secondApp);

        firestore.collection("gas_stations").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<RecommendGasStation> gasStationList = new ArrayList<>();
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            for (QueryDocumentSnapshot document : result) {
                                RecommendGasStation gasStation = new RecommendGasStation();
                                gasStation.setName(document.getString("name"));
                                gasStation.setKindOfService(document.getString("kind_of_service"));
                                gasStation.setPlace(document.getString("place"));
                                gasStation.setImageUrl(document.getString("image"));
                                gasStation.setTimeSchedule(document.get("time_schedule") != null ? document.get("time_schedule").toString() : "");

                                gasStation.setWebsite(document.get("website") != null ? document.get("website").toString() : "");

                                try {
                                    gasStation.setLatitude(document.get("latitude") != null ? Double.parseDouble(document.get("latitude").toString()) : 0.0);
                                    gasStation.setLongitude(document.get("longitude") != null ? Double.parseDouble(document.get("longitude").toString()) : 0.0);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid latitude/longitude format", e);
                                    gasStation.setLatitude(0.0);
                                    gasStation.setLongitude(0.0);
                                }

                                gasStationList.add(gasStation);
                            }

                            // Get user location
                            Location userLocation = getUserLocation(context);
                            if (userLocation != null) {
                                for (RecommendGasStation gasStation : gasStationList) {
                                    Location stationLocation = new Location("");
                                    stationLocation.setLatitude(gasStation.getLatitude());
                                    stationLocation.setLongitude(gasStation.getLongitude());
                                    float distance = userLocation.distanceTo(stationLocation);
                                    gasStation.setDistance(distance);
                                }
                                Collections.sort(gasStationList, Comparator.comparingDouble(RecommendGasStation::getDistance));
                            } else {
                                Log.e(TAG, "User location is null");
                            }
                            callback.onCallback(gasStationList);
                        } else {
                            Log.e(TAG, "No data found in gas_stations");
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
    public static void getTowing(Context context, FirestoreTowingCallback callback) {
        FirebaseApp secondApp = FirebaseApp.getInstance("gearupdataSecondApp");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance(secondApp);

        firestore.collection("towing_services").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<RecommendTowing> towingList = new ArrayList<>();
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            for (QueryDocumentSnapshot document : result) {
                                RecommendTowing towing = new RecommendTowing();
                                towing.setShopName(document.getString("shop_name"));
                                towing.setKindOfService(document.getString("kind_of_service"));
                                towing.setPlace(document.getString("place"));
                                towing.setImage(document.getString("image"));
                                towing.setTimeSchedule(document.get("time_schedule") != null ? document.get("time_schedule").toString() : "");
                                towing.setContactNumber(document.get("contact_number") != null ? document.get("contact_number").toString() : "");
                                towing.setRatings(document.get("ratings") != null ? document.get("ratings").toString() : "");
                                towing.setWebsite(document.get("website") != null ? document.get("website").toString() : "");

                                try {
                                    towing.setLatitude(document.get("latitude") != null ? Double.parseDouble(document.get("latitude").toString()) : 0.0);
                                    towing.setLongitude(document.get("longitude") != null ? Double.parseDouble(document.get("longitude").toString()) : 0.0);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid latitude/longitude format", e);
                                    towing.setLatitude(0.0);
                                    towing.setLongitude(0.0);
                                }

                                towingList.add(towing);
                            }

                            // Get user location
                            Location userLocation = getUserLocation(context);
                            if (userLocation != null) {
                                for (RecommendTowing towing : towingList) {
                                    Location towingLocation = new Location("");
                                    towingLocation.setLatitude(towing.getLatitude());
                                    towingLocation.setLongitude(towing.getLongitude());
                                    float distance = userLocation.distanceTo(towingLocation);
                                    towing.setDistance(distance);
                                }
                                Collections.sort(towingList, Comparator.comparingDouble(RecommendTowing::getDistance));
                            } else {
                                Log.e(TAG, "User location is null");
                            }
                            callback.onCallback(towingList);
                        } else {
                            Log.e(TAG, "No data found in towing_services");
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
    public static void getBatteryShop(Context context, FirestoreCallback callback) {
        FirebaseApp secondApp = FirebaseApp.getInstance("gearupdataSecondApp");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance(secondApp);

        firestore.collection("batteries_shops").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<RecommendLocalShop> shopList = new ArrayList<>();
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            for (QueryDocumentSnapshot document : result) {
                                RecommendLocalShop shop = new RecommendLocalShop();
                                shop.setShopName(document.getString("shop_name"));
                                shop.setPlace(document.getString("place"));
                                Object kindOfService = document.get("kind_of_service");
                                shop.setKindOfService(kindOfService != null ? kindOfService.toString() : "");
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
    public static void getSmokeService(Context context, FirestoreSmokeServiceCallback callback) {
        FirebaseApp secondApp = FirebaseApp.getInstance("gearupdataSecondApp");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance(secondApp);

        firestore.collection("smoke_services").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<RecommendSmokeService> smokeList = new ArrayList<>();
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            for (QueryDocumentSnapshot document : result) {
                                RecommendSmokeService shop = new RecommendSmokeService();
                                shop.setShopName(document.getString("shop_name"));
                                shop.setLocation(document.getString("place"));
                                shop.setServices(document.get("kind_of_service") != null ? document.get("kind_of_service").toString() : "");
                                shop.setTimeSchedule(document.get("time_schedule") != null ? document.get("time_schedule").toString() : "");
                                shop.setNumber(document.get("contact_number") != null ? document.get("contact_number").toString() : "");
                                try {
                                    shop.setRatings(document.get("ratings") != null && !document.get("ratings").toString().equalsIgnoreCase("none") ? Double.parseDouble(document.get("ratings").toString()) : 0.0);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid ratings format", e);
                                    shop.setRatings(0.0);
                                }
                                String numReviewsStr = document.get("num_reviews") != null ? document.get("num_reviews").toString() : "0";
                                int numReviews;
                                try {
                                    numReviews = (int) Float.parseFloat(numReviewsStr);
                                } catch (NumberFormatException e) {
                                    numReviews = 0;
                                }
                                shop.setNumReviews(numReviews);

                                try {
                                    shop.setLatitude(document.get("latitude") != null ? Double.parseDouble(document.get("latitude").toString()) : 0.0);
                                    shop.setLongitude(document.get("longitude") != null ? Double.parseDouble(document.get("longitude").toString()) : 0.0);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid latitude/longitude format", e);
                                    shop.setLatitude(0.0);
                                    shop.setLongitude(0.0);
                                }
                                shop.setImage(document.get("image") != null ? document.get("image").toString() : "");

                                smokeList.add(shop);
                            }

                            // Get user location
                            Location userLocation = getUserLocation(context);
                            if (userLocation != null) {
                                for (RecommendSmokeService shop : smokeList) {
                                    Location shopLocation = new Location("");
                                    shopLocation.setLatitude(shop.getLatitude());
                                    shopLocation.setLongitude(shop.getLongitude());
                                    float distance = userLocation.distanceTo(shopLocation);
                                    shop.setDistance(distance);
                                }
                                // Sort by distance
                                Collections.sort(smokeList, Comparator.comparingDouble(shop -> {
                                    Location shopLocation = new Location("");
                                    shopLocation.setLatitude(shop.getLatitude());
                                    shopLocation.setLongitude(shop.getLongitude());
                                    return userLocation.distanceTo(shopLocation);
                                }));
                            } else {
                                Log.e(TAG, "User location is null");
                            }
                            callback.onCallback(smokeList);
                        } else {
                            Log.e(TAG, "No data found in smoke_services");
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
    public static void getParkingLot(Context context, FirestoreParkingCallback callback) {
        FirebaseApp secondApp = FirebaseApp.getInstance("gearupdataSecondApp");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance(secondApp);

        firestore.collection("parking_lot").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<RecommendParking> parkingList = new ArrayList<>();
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            for (QueryDocumentSnapshot document : result) {
                                RecommendParking parking = new RecommendParking();
                                parking.setShopName(document.getString("shop_name"));
                                parking.setPlace(document.getString("place"));
                                parking.setKindOfService(document.get("kind_of_service") != null ? document.get("kind_of_service").toString() : "");
                                parking.setTimeSchedule(document.get("time_schedule") != null ? document.get("time_schedule").toString() : "");
                                try {
                                    parking.setRatings(document.get("ratings") != null && !document.get("ratings").toString().equalsIgnoreCase("none") ? Float.parseFloat(document.get("ratings").toString()) : 0.0f);
                                } catch (NumberFormatException e) {
                                    Log.e("TAG", "Invalid ratings format", e);
                                    parking.setRatings(0.0f);
                                }
                                try {
                                    parking.setLatitude(document.get("latitude") != null ? Double.parseDouble(document.get("latitude").toString()) : 0.0);
                                    parking.setLongitude(document.get("longitude") != null ? Double.parseDouble(document.get("longitude").toString()) : 0.0);
                                } catch (NumberFormatException e) {
                                    Log.e("TAG", "Invalid latitude/longitude format", e);
                                    parking.setLatitude(0.0);
                                    parking.setLongitude(0.0);
                                }
                                parking.setImage(document.get("image") != null ? document.get("image").toString() : "");

                                parkingList.add(parking);
                            }

                            // Get user location
                            Location userLocation = getUserLocation(context);
                            if (userLocation != null) {
                                for (RecommendParking parking : parkingList) {
                                    Location parkingLocation = new Location("");
                                    parkingLocation.setLatitude(parking.getLatitude());
                                    parkingLocation.setLongitude(parking.getLongitude());
                                    float distance = userLocation.distanceTo(parkingLocation);
                                    parking.setDistance(distance);
                                }
                                // Sort by distance
                                Collections.sort(parkingList, Comparator.comparingDouble(RecommendParking::getDistance));
                            } else {
                                Log.e("TAG", "User location is null");
                            }
                            callback.onCallback(parkingList);
                        } else {
                            Log.e("TAG", "No data found in parking_lot");
                        }
                    } else {
                        Log.e("TAG", "Error getting documents: ", task.getException());
                    }
                });
    }
}

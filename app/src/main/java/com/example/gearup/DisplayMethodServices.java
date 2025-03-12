package com.example.gearup;

import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class DisplayMethodServices {

    private static final String TAG = "DisplayMethodServices";

    public interface FirestoreCallback {
        void onCallback(List<RecommendLocalShop> shops);
    }

    public static void getAutoPartsShops(FirestoreCallback callback) {
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
                                Object timeScheduleField = document.get("time_schedule");
                                if (timeScheduleField != null) {
                                    shop.setTimeSchedule(timeScheduleField.toString());
                                }

                                Object contactNumberField = document.get("contact_number");
                                if (contactNumberField != null) {
                                    shop.setContactNumber(contactNumberField.toString());
                                }


                                // Handle ratings conversion
                                Object ratingsField = document.get("ratings");
                                if (ratingsField != null) {
                                    shop.setRatings(Double.parseDouble(ratingsField.toString()));
                                }

                                // Handle website conversion
                                Object websiteField = document.get("website");
                                if (websiteField != null) {
                                    shop.setWebsite(websiteField.toString());
                                }

                                // Handle latitude conversion
                                Object latitudeField = document.get("latitude");
                                if (latitudeField != null) {
                                    shop.setLatitude(Double.parseDouble(latitudeField.toString()));
                                }

                                // Handle longitude conversion
                                Object longitudeField = document.get("longitude");
                                if (longitudeField != null) {
                                    shop.setLongitude(Double.parseDouble(longitudeField.toString()));
                                }

                                Object imageField = document.get("image");
                                if (imageField != null) {
                                    shop.setImage(imageField.toString());
                                }


                                shopList.add(shop);
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

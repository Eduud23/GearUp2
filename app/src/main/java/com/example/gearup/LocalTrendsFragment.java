package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalTrendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LocalTrendsAdapter adapter;
    private List<LocalTrendsData> localTrendsList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;  // Declare SwipeRefreshLayout
    private static final String TAG = "LocalTrendsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_trends, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_local_trends);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);  // Initialize SwipeRefreshLayout

        // Set up RecyclerView layout manager
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Initialize the adapter with the click listener
        adapter = new LocalTrendsAdapter(localTrendsList, this::onItemClick);
        recyclerView.setAdapter(adapter);

        // Set up SwipeRefreshLayout listener to refresh data
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchLocalTrendsData(true);  // Pass 'true' to shuffle the list after refreshing
        });

        // Initial data fetch
        fetchLocalTrendsData(false);  // Pass 'false' for the initial load without shuffling
        return view;
    }

    private void onItemClick(LocalTrendsData data) {
        Intent intent = new Intent(getContext(), LocalTrendsDetails.class);
        intent.putExtra("image", data.getImage());
        intent.putExtra("name", data.getName());
        intent.putExtra("place", data.getPlace());
        intent.putExtra("price", data.getPrice());
        intent.putExtra("ratings", data.getRatings());
        intent.putExtra("sold", data.getSold());
        intent.putExtra("promo", data.getPromo());
        intent.putExtra("link", data.getLink());
        startActivity(intent);
    }

    private void fetchLocalTrendsData(boolean shuffle) {
        FirebaseApp thirdApp = FirebaseApp.getInstance("gearupdataThirdApp");
        FirebaseFirestore db = FirebaseFirestore.getInstance(thirdApp);

        db.collection("shopee_products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        localTrendsList.clear();
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            for (QueryDocumentSnapshot document : result) {
                                LocalTrendsData data = new LocalTrendsData();
                                data.setImage(document.getString("image"));
                                data.setLink(document.getString("link"));
                                data.setName(document.getString("name"));
                                data.setPlace(document.getString("place"));

                                // Handle price
                                Object priceObj = document.get("price");
                                if (priceObj instanceof Number) {
                                    data.setPrice(((Number) priceObj).doubleValue());
                                } else if (priceObj instanceof String) {
                                    try {
                                        data.setPrice(Double.parseDouble((String) priceObj));
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "Invalid price format", e);
                                        data.setPrice(0.0);
                                    }
                                } else {
                                    data.setPrice(0.0);
                                }

                                // Handle ratings
                                Object ratingsObj = document.get("ratings");
                                if (ratingsObj instanceof Number) {
                                    data.setRatings(((Number) ratingsObj).doubleValue());
                                } else if (ratingsObj instanceof String) {
                                    try {
                                        data.setRatings(Double.parseDouble((String) ratingsObj));
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "Invalid ratings format", e);
                                        data.setRatings(0.0);
                                    }
                                } else {
                                    data.setRatings(0.0);
                                }

                                // Handle promo
                                Object promoObj = document.get("promo");
                                if (promoObj instanceof String) {
                                    data.setPromo((String) promoObj);
                                } else if (promoObj != null) {
                                    data.setPromo(promoObj.toString());
                                } else {
                                    data.setPromo("");  // Default value if promo is null
                                }

                                // Handle sale
                                Object saleObj = document.get("sale");
                                if (saleObj instanceof Number) {
                                    data.setSale(((Number) saleObj).intValue());
                                } else if (saleObj instanceof String) {
                                    try {
                                        data.setSale(Integer.parseInt((String) saleObj));
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "Invalid sale format", e);
                                        data.setSale(0);
                                    }
                                } else {
                                    data.setSale(0);  // Default if sale is null or unexpected type
                                }

                                data.setSold(document.getString("sold") != null ? document.getString("sold") : "0");

                                localTrendsList.add(data);
                            }

                            // Shuffle the list if needed
                            if (shuffle) {
                                Collections.shuffle(localTrendsList);
                            }

                            // Notify adapter about data change
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "No data found in shopee_products");
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }

                    // Stop the refreshing animation once data is loaded
                    swipeRefreshLayout.setRefreshing(false);
                });
    }
}

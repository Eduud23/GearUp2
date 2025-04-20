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
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "LocalTrendsFragment";

    private String searchQuery = ""; // 🔍 for filtering

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_trends, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_local_trends);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new LocalTrendsAdapter(localTrendsList, this::onItemClick);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(() -> fetchLocalTrendsData(true));
        fetchLocalTrendsData(false); // Initial load

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

    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase(); // Convert to lowercase for case-insensitive search
        fetchLocalTrendsData(false); // Re-fetch and filter based on new search
    }

    private void fetchLocalTrendsData(boolean shuffle) {
        FirebaseApp thirdApp = FirebaseApp.getInstance("gearupdataThirdApp");
        FirebaseFirestore db = FirebaseFirestore.getInstance(thirdApp);

        db.collection("shopee_products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<LocalTrendsData> rawDataList = new ArrayList<>();
                        QuerySnapshot result = task.getResult();

                        if (result != null) {
                            for (QueryDocumentSnapshot document : result) {
                                LocalTrendsData data = new LocalTrendsData();
                                data.setImage(document.getString("image"));
                                data.setLink(document.getString("link"));
                                data.setName(document.getString("name"));
                                data.setPlace(document.getString("place"));

                                // Parse price
                                parsePrice(document.get("price"), data);

                                // Parse ratings
                                parseRatings(document.get("ratings"), data);

                                // Parse promo
                                parsePromo(document.get("promo"), data);

                                // Handle sale format (either integer or percentage)
                                parseSale(document.get("sale"), data);

                                data.setSold(document.getString("sold") != null ? document.getString("sold") : "0");

                                rawDataList.add(data);
                            }

                            // Filter if there's a query
                            List<LocalTrendsData> finalList;
                            if (!searchQuery.isEmpty()) {
                                finalList = filterLocalTrends(rawDataList, searchQuery);
                            } else {
                                finalList = rawDataList;
                                if (shuffle) Collections.shuffle(finalList);
                            }

                            localTrendsList.clear();
                            localTrendsList.addAll(finalList);

                            // Ensure adapter is not null before notifying
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.e(TAG, "Adapter is null. Cannot notify data set change.");
                            }

                        } else {
                            Log.e(TAG, "No data found in shopee_products");
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }

                    // Ensure swipeRefreshLayout is not null before using it
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void parsePrice(Object priceObj, LocalTrendsData data) {
        if (priceObj instanceof String) {
            String priceString = (String) priceObj;
            // Remove commas (e.g., "1,190" becomes "1190")
            priceString = priceString.replace(",", "");

            try {
                data.setPrice(Double.parseDouble(priceString));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid price format", e);
                data.setPrice(0.0);
            }
        } else if (priceObj instanceof Number) {
            data.setPrice(((Number) priceObj).doubleValue());
        } else {
            data.setPrice(0.0);
        }
    }

    private void parseRatings(Object ratingsObj, LocalTrendsData data) {
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
    }

    private void parsePromo(Object promoObj, LocalTrendsData data) {
        if (promoObj instanceof String) {
            data.setPromo((String) promoObj);
        } else if (promoObj != null) {
            data.setPromo(promoObj.toString());
        } else {
            data.setPromo("");
        }
    }

    private void parseSale(Object saleObj, LocalTrendsData data) {
        if (saleObj instanceof Number) {
            data.setSale(((Number) saleObj).intValue());
        } else if (saleObj instanceof String) {
            try {
                // Handle sale in percentage format (e.g., "-40%")
                String saleString = (String) saleObj;
                if (saleString.contains("%")) {
                    saleString = saleString.replace("%", "");
                    data.setSale(Integer.parseInt(saleString));
                } else {
                    data.setSale(Integer.parseInt(saleString));
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid sale format", e);
                data.setSale(0);
            }
        } else {
            data.setSale(0);
        }
    }

    // Simplified search filter method
    // Filter local trends based on the search query using word-by-word matching
    private List<LocalTrendsData> filterLocalTrends(List<LocalTrendsData> rawData, String query) {
        List<LocalTrendsData> filtered = new ArrayList<>();

        // Split the search query into words by spaces
        String[] queryWords = query.trim().toLowerCase().split("\\s+");

        for (LocalTrendsData data : rawData) {
            int matchCount = 0;

            // Check each word in the query against each field in the data
            for (String word : queryWords) {
                if (containsIgnoreCase(data.getName(), word)) {
                    matchCount++;
                }
                if (containsIgnoreCase(data.getPlace(), word)) {
                    matchCount++;
                }
                if (containsIgnoreCase(String.valueOf(data.getPrice()), word)) {
                    matchCount++;
                }
                if (containsIgnoreCase(data.getPromo(), word)) {
                    matchCount++;
                }
                if (containsIgnoreCase(data.getSold(), word)) {
                    matchCount++;
                }
                if (containsIgnoreCase(String.valueOf(data.getRatings()), word)) {
                    matchCount++;
                }
            }

            // If there was a match, set the match count and add it to the filtered list
            if (matchCount > 0) {
                data.setMatchCount(matchCount);
                filtered.add(data);
            }
        }

        // Sort the filtered products by match count in descending order (higher match count first)
        Collections.sort(filtered, (d1, d2) -> Integer.compare(d2.getMatchCount(), d1.getMatchCount()));

        return filtered;
    }

    // Helper method to check if a field contains a word, ignoring case
    private boolean containsIgnoreCase(String fieldValue, String query) {
        return fieldValue != null && query != null && fieldValue.toLowerCase().contains(query);
    }
}

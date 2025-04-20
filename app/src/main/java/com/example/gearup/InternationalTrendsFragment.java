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

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class InternationalTrendsFragment extends Fragment {

    private static final String TAG = "InternationalTrends";
    private RecyclerView recyclerView;
    private PopularProductAdapter adapter;
    private final List<PopularProduct> productList = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private FirebaseFirestore db;

    private SwipeRefreshLayout swipeRefreshLayout;
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_international_trends, container, false);

        // Initialize the SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchProducts); // Set refresh listener to fetch products

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new PopularProductAdapter(productList, this::openProductDetail);
        recyclerView.setAdapter(adapter);

        try {
            FirebaseApp thirdApp = FirebaseApp.getInstance("gearupdataThirdApp");
            db = FirebaseFirestore.getInstance(thirdApp);
            Log.d(TAG, "✅ Connected to Firestore (gearupdataThirdApp)");
        } catch (IllegalStateException e) {
            Log.e(TAG, "❌ FirebaseApp 'gearupdataThirdApp' not found.", e);
            return view;
        }

        fetchProducts(); // Initial product fetch

        // Retrieve the search query passed from TrendsFragment
        if (getArguments() != null) {
            searchQuery = getArguments().getString("search_query", "");
        }

        return view;
    }

    // Fetch products from Firestore
    public void fetchProducts() {
        if (db == null) {
            Log.e(TAG, "❌ Firestore not initialized.");
            if (isAdded()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        executorService.execute(() -> db.collection("ebay_popular_product").get()
                .addOnCompleteListener(task -> {
                    if (!isAdded()) {
                        Log.w(TAG, "❗ Fragment is not attached. Skipping UI update.");
                        return;
                    }

                    if (task.isSuccessful()) {
                        List<PopularProduct> rawProducts = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            PopularProduct product = new PopularProduct(
                                    document.getString("title"),
                                    document.getString("price"),
                                    document.getString("image"),
                                    document.getString("link"),
                                    document.getString("condition"),
                                    document.getString("location"),
                                    String.valueOf(document.get("shipping")),
                                    String.valueOf(document.get("discount")),
                                    String.valueOf(document.get("rated")),
                                    document.getString("seller")
                            );
                            rawProducts.add(product);
                        }

                        List<PopularProduct> finalProducts;

                        // Apply filter if search query is present
                        if (!searchQuery.isEmpty()) {
                            finalProducts = filterProducts(rawProducts, searchQuery);
                        } else {
                            finalProducts = rawProducts;
                        }

                        Collections.shuffle(finalProducts);

                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                updateAdapter(finalProducts);  // ✅ No error, finalProducts is effectively final
                                swipeRefreshLayout.setRefreshing(false);
                            });
                        }

                    } else {
                        Log.e(TAG, "❌ Error fetching products", task.getException());
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    swipeRefreshLayout.setRefreshing(false));
                        }
                    }
                }));
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query;
        fetchProducts();
    }

    // Filter products based on the search query using simple substring matching
    private List<PopularProduct> filterProducts(List<PopularProduct> products, String query) {
        List<PopularProduct> filtered = new ArrayList<>();

        for (PopularProduct product : products) {
            if (
                    containsIgnoreCase(product.getTitle(), query) ||
                            containsIgnoreCase(product.getPrice(), query) ||
                            containsIgnoreCase(product.getImageUrl(), query) ||
                            containsIgnoreCase(product.getItemUrl(), query) ||
                            containsIgnoreCase(product.getCondition(), query) ||
                            containsIgnoreCase(product.getLocation(), query) ||
                            containsIgnoreCase(product.getShippingCost(), query) ||
                            containsIgnoreCase(product.getDiscount(), query) ||
                            containsIgnoreCase(product.getRated(), query) ||
                            containsIgnoreCase(product.getSeller(), query)
            ) {
                filtered.add(product);
            }
        }

        return filtered;
    }

    private boolean containsIgnoreCase(String fieldValue, String query) {
        return fieldValue != null && query != null && fieldValue.toLowerCase().contains(query.toLowerCase());
    }

    private void updateAdapter(List<PopularProduct> fetchedProducts) {
        productList.clear();
        productList.addAll(fetchedProducts);
        adapter.notifyDataSetChanged();
    }

    private void openProductDetail(PopularProduct product) {
        Intent intent = new Intent(requireContext(), PopularProductDetail.class);
        intent.putExtra("title", product.getTitle());
        intent.putExtra("price", product.getPrice());
        intent.putExtra("imageUrl", product.getImageUrl());
        intent.putExtra("itemUrl", product.getItemUrl());
        intent.putExtra("condition", product.getCondition());
        intent.putExtra("location", product.getLocation());
        intent.putExtra("shippingCost", product.getShippingCost());
        intent.putExtra("seller", product.getSeller());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}

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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrendsFragment extends Fragment {

    private static final String TAG = "TrendsFragment";
    private RecyclerView recyclerView;
    private PopularProductAdapter adapter;
    private List<PopularProduct> productList = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trends_buyer, container, false);

        // Initialize RecyclerView with GridLayoutManager (2 columns)
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new PopularProductAdapter(productList, null); // Initially, no click listener
        recyclerView.setAdapter(adapter);

        // Ensure FirebaseApp is initialized before getting Firestore
        try {
            FirebaseApp thirdApp = FirebaseApp.getInstance("gearupdataThirdApp");
            db = FirebaseFirestore.getInstance(thirdApp);
            Log.d(TAG, "✅ Successfully connected to Firestore (gearupdataThirdApp)");
        } catch (IllegalStateException e) {
            Log.e(TAG, "❌ Error: FirebaseApp 'gearupdataThirdApp' not found. Make sure it is initialized in MyApp.java", e);
            return view;
        }

        // Fetch products from Firestore
        fetchProducts();

        return view;
    }

    private void fetchProducts() {
        if (db == null) {
            Log.e(TAG, "❌ Firestore not initialized. Aborting fetch.");
            return;
        }

        executorService.execute(() -> {
            db.collection("ebay_popular_product").get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<PopularProduct> fetchedProducts = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PopularProduct product = new PopularProduct(
                                        document.getString("title"),
                                        document.getString("price"),
                                        document.getString("image"),
                                        document.getString("link"),
                                        document.getString("condition"),
                                        document.getString("location"),
                                        String.valueOf(document.get("shipping")), // Ensure shipping is String
                                        String.valueOf(document.get("discount")), // Ensure discount is String
                                        String.valueOf(document.get("rated")), // Ensure rated is String
                                        document.getString("seller")
                                );
                                fetchedProducts.add(product);
                            }

                            requireActivity().runOnUiThread(() -> updateAdapter(fetchedProducts));

                        } else {
                            Log.e(TAG, "❌ Error fetching products", task.getException());
                        }
                    });
        });
    }




    private void updateAdapter(List<PopularProduct> fetchedProducts) {
        productList.clear();
        productList.addAll(fetchedProducts);
        adapter.notifyDataSetChanged();

        // Now, set the item click listener after data is processed
        adapter = new PopularProductAdapter(productList, product -> {
            Intent intent = new Intent(requireContext(), PopularProductDetail.class);
            intent.putExtra("title", product.getTitle());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            intent.putExtra("itemUrl", product.getItemUrl());
            intent.putExtra("condition", product.getCondition());
            intent.putExtra("location", product.getLocation());
            intent.putExtra("shippingCost", product.getShippingCost());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}

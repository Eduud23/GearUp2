package com.example.gearup;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private RecyclerView recyclerSearchResults;
    private SearchProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;
    private String selectedCategory = "All";


    private double filterCenterLat = -1;
    private double filterCenterLng = -1;
    private float filterRadius = -1;
    private static final int REQUEST_MAP_FILTER = 1001;

    private static final String PREFS_NAME = "FilterPrefs";
    private static final String PREF_SELECTED_CATEGORY = "selectedCategory";
    private static final String PREF_MIN_RATING = "minRating";
    private float minRating = -1;
    private ImageView filterIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchInput = findViewById(R.id.et_search);
        recyclerSearchResults = findViewById(R.id.recycler_search_results);
        filterIcon = findViewById(R.id.filter_icon);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> onBackPressed());

        recyclerSearchResults.setLayoutManager(new GridLayoutManager(this, 2));

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedCategory = prefs.getString(PREF_SELECTED_CATEGORY, "All");
        minRating = prefs.getFloat(PREF_MIN_RATING, -1);

        searchInput = findViewById(R.id.et_search);

        adapter = new SearchProductAdapter(this, new ArrayList<>(), "Search", product -> {
            Intent intent = new Intent(SearchActivity.this, ProductDetailsBuyerActivity.class);
            intent.putExtra("PRODUCT", product);
            startActivity(intent);
        });

        recyclerSearchResults.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadProducts();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        filterIcon.setOnClickListener(v -> showFilterDialog());
    }

    private void loadProducts() {
        Log.d(TAG, "Starting to load products...");
        db.collectionGroup("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                productList.clear();
                List<Product> filteredProducts = new ArrayList<>();

                int totalDocs = task.getResult().size();
                Log.d(TAG, "Fetched " + totalDocs + " products from Firestore");

                if (totalDocs == 0) {
                    Log.w(TAG, "No products found in the database.");
                    return;
                }

                final int[] processedCount = {0};

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Product product = document.toObject(Product.class);
                    if (product == null) {
                        Log.w(TAG, "Skipped null product object");
                        processedCount[0]++;
                        continue;
                    }

                    product.setId(document.getId());

                    String sellerId = product.getSellerId();
                    if (sellerId == null || sellerId.isEmpty()) {
                        Log.w(TAG, "Product " + product.getId() + " has no seller ID.");
                        processedCount[0]++;
                        continue;
                    }

                    db.collection("sellers").document(sellerId).get()
                            .addOnSuccessListener(sellerDoc -> {
                                processedCount[0]++;
                                if (sellerDoc.exists()) {
                                    Double sellerLat = sellerDoc.getDouble("latitude");
                                    Double sellerLng = sellerDoc.getDouble("longitude");

                                    if (sellerLat == null || sellerLng == null) {
                                        Log.w(TAG, "Seller " + sellerId + " has null latitude/longitude.");
                                        checkIfAllProcessed(processedCount[0], totalDocs, filteredProducts);
                                        return;
                                    }

                                    // Store to product in case needed later
                                    product.setSellerLatitude(sellerLat);
                                    product.setSellerLongitude(sellerLng);

                                    if (filterCenterLat != -1 && filterCenterLng != -1 && filterRadius > 0) {
                                        float[] distanceResult = new float[1];
                                        Location.distanceBetween(filterCenterLat, filterCenterLng, sellerLat, sellerLng, distanceResult);

                                        Log.d(TAG, "Distance to seller " + sellerId + ": " + distanceResult[0] + "m");

                                        if (distanceResult[0] <= filterRadius) {
                                            filteredProducts.add(product);
                                            Log.d(TAG, "Product " + product.getId() + " added (within radius)");
                                        } else {
                                            Log.d(TAG, "Product " + product.getId() + " excluded (outside radius)");
                                        }
                                    } else {
                                        // No location filter set, include all
                                        filteredProducts.add(product);
                                        Log.d(TAG, "Product " + product.getId() + " added (no radius filter applied)");
                                    }
                                } else {
                                    Log.w(TAG, "Seller document not found for ID: " + sellerId);
                                }

                                checkIfAllProcessed(processedCount[0], totalDocs, filteredProducts);
                            })
                            .addOnFailureListener(e -> {
                                processedCount[0]++;
                                Log.e(TAG, "Error fetching seller " + sellerId, e);
                                checkIfAllProcessed(processedCount[0], totalDocs, filteredProducts);
                            });
                }
            } else {
                Log.e(TAG, "Failed to fetch products from Firestore", task.getException());
            }
        });
    }

    private void checkIfAllProcessed(int processed, int total, List<Product> filteredProducts) {
        Log.d(TAG, "Processed " + processed + " of " + total + " products.");

        if (processed >= total) {
            Log.d(TAG, "Finished processing all products. Filtered size: " + filteredProducts.size());

            fetchSellerProfileImages(filteredProducts, updatedProducts -> {
                productList.clear();
                productList.addAll(updatedProducts);
                filterProducts(searchInput.getText().toString().trim());
            });
        }
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        String[] keywords = query.toLowerCase().split("\\s+");

        for (Product product : productList) {
            // Filter by category
            if (!selectedCategory.equals("All") && !product.getCategory().equalsIgnoreCase(selectedCategory)) {
                continue;
            }

            // Filter by rating
            if (minRating != -1 && product.getStars() < minRating) {
                continue;
            }

            // Filter by location (if map filter is active)
            if (filterCenterLat != -1 && filterCenterLng != -1 && filterRadius > 0) {
                double sellerLat = product.getSellerLatitude();
                double sellerLng = product.getSellerLongitude();

                float[] distanceResult = new float[1];
                Location.distanceBetween(filterCenterLat, filterCenterLng, sellerLat, sellerLng, distanceResult);

                if (distanceResult[0] > filterRadius) {
                    continue; // Outside the radius
                }
            }

            // Relevance check
            int relevanceScore = calculateRelevance(product, keywords);
            if (relevanceScore > 0 || query.isEmpty()) {
                filteredList.add(product);
            }
        }

        // Sort by stars
        filteredList.sort((p1, p2) -> Double.compare(p2.getStars(), p1.getStars()));

        // Update RecyclerView visibility and adapter
        recyclerSearchResults.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);
        adapter.updateProductList(filteredList);
    }

    private int calculateRelevance(Product product, String[] keywords) {
        int score = 0;
        String name = product.getName() != null ? product.getName().toLowerCase() : "";
        String description = product.getDescription() != null ? product.getDescription().toLowerCase() : "";
        String brand = product.getBrand() != null ? product.getBrand().toLowerCase() : "";
        String category = product.getCategory() != null ? product.getCategory().toLowerCase() : "";
        String yearModel = product.getYearModel() != null ? product.getYearModel().toLowerCase() : "";

        for (String keyword : keywords) {
            if (name.contains(keyword)) score += 3;
            if (description.contains(keyword)) score += 2;
            if (brand.contains(keyword)) score += 1;
            if (category.contains(keyword)) score += 1;
            if (yearModel.contains(keyword)) score += 1;
        }

        return score;
    }

    private void showFilterDialog() {
        String[] categories = {"All", "Central Components", "Body", "Connectors", "Peripherals"};
        String[] ratings = {"All", "1", "2", "3", "4", "5"};

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedCategory = prefs.getString(PREF_SELECTED_CATEGORY, "All");
        float savedMinRating = prefs.getFloat(PREF_MIN_RATING, -1);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_category_rating, null);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category_select);
        Spinner ratingSpinner = dialogView.findViewById(R.id.spinner_rating_select);
        Button mapFilterBtn = dialogView.findViewById(R.id.btn_map_filter);

        categorySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));
        ratingSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ratings));

        int selectedCategoryIndex = java.util.Arrays.asList(categories).indexOf(savedCategory);
        categorySpinner.setSelection(selectedCategoryIndex != -1 ? selectedCategoryIndex : 0);
        int selectedRatingIndex = (savedMinRating == -1) ? 0 : (int) savedMinRating;
        ratingSpinner.setSelection(selectedRatingIndex);

        TextView resetMapFilterTv = dialogView.findViewById(R.id.tv_reset_map_filter);
        resetMapFilterTv.setOnClickListener(v -> {
            filterCenterLat = -1;
            filterCenterLng = -1;
            filterRadius = -1;

            Toast.makeText(this, "Map filter reset", Toast.LENGTH_SHORT).show();
            loadProducts(); // Reload with map filter cleared
        });


        mapFilterBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapFilterActivity.class);
            startActivityForResult(intent, REQUEST_MAP_FILTER);
        });

        new AlertDialog.Builder(this)
                .setTitle("Filter Options")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    selectedCategory = categorySpinner.getSelectedItem().toString();
                    String selectedRatingStr = ratingSpinner.getSelectedItem().toString();
                    minRating = selectedRatingStr.equals("All") ? -1 : Float.parseFloat(selectedRatingStr);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREF_SELECTED_CATEGORY, selectedCategory);
                    editor.putFloat(PREF_MIN_RATING, minRating);
                    editor.apply();

                    filterProducts(searchInput.getText().toString().trim());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Method to fetch seller profile images
    private void fetchSellerProfileImages(List<Product> products, Consumer<List<Product>> callback) {
        Log.d(TAG, "Fetching seller profile images for " + products.size() + " products");

        List<Product> updatedProducts = new ArrayList<>();
        int[] remainingRequests = {products.size()};

        for (Product product : products) {
            db.collection("sellers").document(product.getSellerId())
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            String profileImageUrl = userDoc.getString("profileImageUrl");
                            product.setSellerProfileImageUrl(profileImageUrl != null ? profileImageUrl : "");
                        } else {
                            product.setSellerProfileImageUrl("");
                        }
                        synchronized (updatedProducts) {
                            updatedProducts.add(product);
                        }
                        if (--remainingRequests[0] == 0) {
                            callback.accept(updatedProducts);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching seller profile image", e);
                        product.setSellerProfileImageUrl("");
                        synchronized (updatedProducts) {
                            updatedProducts.add(product);
                        }
                        if (--remainingRequests[0] == 0) {
                            callback.accept(updatedProducts);
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MAP_FILTER && resultCode == RESULT_OK && data != null) {
            filterCenterLat = data.getDoubleExtra("center_lat", -1);
            filterCenterLng = data.getDoubleExtra("center_lng", -1);
            filterRadius = data.getFloatExtra("radius", -1);


            Log.d(TAG, "Map filter applied - Lat: " + filterCenterLat + ", Lng: " + filterCenterLng + ", Radius: " + filterRadius);

            // ❗ This is the key change:
            loadProducts(); // Reload products with new radius/location filter
        }
    }


}

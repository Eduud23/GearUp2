package com.example.gearup;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
    private String selectedCategory = "";

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
        db.collectionGroup("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                productList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Product product = document.toObject(Product.class);
                    if (product != null) {
                        product.setId(document.getId());
                        productList.add(product);
                    }
                }
                // Now fetch the seller profile images after loading the products
                fetchSellerProfileImages(productList, updatedProducts -> {
                    filterProducts(searchInput.getText().toString().trim());
                });
            }
        });
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        String[] keywords = query.toLowerCase().split("\\s+");

        for (Product product : productList) {
            // Filter by category if it's not "All"
            if (!selectedCategory.equals("All") && !product.getCategory().equalsIgnoreCase(selectedCategory)) {
                continue;
            }

            // Filter by rating if minRating is set (not -1)
            if (minRating != -1 && product.getStars() < minRating) {
                continue;
            }

            // Calculate relevance score for the search query
            int relevanceScore = calculateRelevance(product, keywords);
            if (relevanceScore > 0) {
                filteredList.add(product);
            }
        }

        // Sort the filtered list by star rating (highest to lowest) after filtering
        filteredList.sort((p1, p2) -> Double.compare(p2.getStars(), p1.getStars()));

        // Show or hide the recycler view depending on the filtered list's size
        recyclerSearchResults.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);

        // Update the adapter with the filtered and sorted list
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

        // Load previously saved filter values
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedCategory = prefs.getString(PREF_SELECTED_CATEGORY, "All");
        float savedMinRating = prefs.getFloat(PREF_MIN_RATING, -1); // -1 means no rating filter

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_category_rating, null);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category_select);
        Spinner ratingSpinner = dialogView.findViewById(R.id.spinner_rating_select);

        categorySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));
        ratingSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ratings));

        // Set the previously saved category and rating as the default selected values
        int selectedCategoryIndex = java.util.Arrays.asList(categories).indexOf(savedCategory);
        categorySpinner.setSelection(selectedCategoryIndex != -1 ? selectedCategoryIndex : 0);

        int selectedRatingIndex = (savedMinRating == -1) ? 0 : (int) savedMinRating;
        ratingSpinner.setSelection(selectedRatingIndex);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Category & Rating")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    selectedCategory = categorySpinner.getSelectedItem().toString();
                    String selectedRatingStr = ratingSpinner.getSelectedItem().toString();
                    minRating = selectedRatingStr.equals("All") ? -1 : Float.parseFloat(selectedRatingStr);

                    // Save the selected filter values to SharedPreferences
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
}

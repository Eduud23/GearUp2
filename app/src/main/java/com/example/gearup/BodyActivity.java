package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BodyActivity extends AppCompatActivity implements SeeAllProductAdapter.OnProductClickListener {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private SeeAllProductAdapter adapter; // Adapter for displaying products
    private List<Product> productsList = new ArrayList<>(); // List for holding products
    private List<Product> filteredProductsList = new ArrayList<>(); // List for holding filtered products
    private EditText searchEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body); // Ensure correct layout name

        recyclerView = findViewById(R.id.recycler_view_body); // Ensure correct ID in the layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Set GridLayoutManager with 2 columns
        searchEditText = findViewById(R.id.et_search); // Find the search EditText

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });


        db = FirebaseFirestore.getInstance();

        // Check if products are passed via Intent
        if (getIntent() != null && getIntent().hasExtra("PRODUCT_LIST")) {
            // Retrieve the passed product list
            productsList = getIntent().getParcelableArrayListExtra("PRODUCT_LIST");
            if (productsList != null && !productsList.isEmpty()) {
                // Set the adapter if products are passed
                setAdapter(productsList);
            } else {
                // If the list is empty, load products from Firestore
                loadProducts();
            }
        } else {
            // If no list is passed, load products from Firestore
            loadProducts();
        }

        // Set up the search functionality
        setUpSearchFunctionality();
    }

    // Load products from Firestore that belong to "Body" category
    private void loadProducts() {
        db.collectionGroup("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productsList.clear(); // Clear previous data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            // Filter products by "Body" category
                            if (product != null && "Body".equals(product.getCategory())) {
                                productsList.add(product); // Add the product to the list
                            }
                        }
                        // After loading products, set the adapter
                        setAdapter(productsList);
                    } else {
                        Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                        Log.e("BodyActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    // Set the adapter for the RecyclerView after products are loaded
    private void setAdapter(List<Product> products) {
        filteredProductsList = new ArrayList<>(products); // Initially, the filtered list is the same as the full list
        adapter = new SeeAllProductAdapter(filteredProductsList, this); // Create the adapter
        recyclerView.setAdapter(adapter); // Set the adapter to the RecyclerView
    }

    // Method to filter products based on search query
    // Method to filter products based on search query (name, brand, and description)
    private void filterProducts(String query) {
        List<Product> filteredList = productsList.stream()
                .filter(product -> {
                    String queryLower = query.toLowerCase(); // Make query case-insensitive
                    // Check if the query matches the name, brand, or description of the product
                    return product.getName().toLowerCase().contains(queryLower) ||
                            product.getBrand().toLowerCase().contains(queryLower) ||
                            product.getDescription().toLowerCase().contains(queryLower);
                })
                .collect(Collectors.toList());

        // Update the filtered products list and notify the adapter
        filteredProductsList = filteredList;
        adapter.updateProducts(filteredProductsList); // Update the adapter with filtered list
    }


    // Set up the search functionality (TextWatcher for search)
    private void setUpSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String query = charSequence.toString();
                if (query.isEmpty()) {
                    // If the search query is empty, show all products
                    filteredProductsList = new ArrayList<>(productsList);
                    adapter.updateProducts(filteredProductsList);
                } else {
                    // Filter products based on the query
                    filterProducts(query);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action needed
            }
        });
    }

    @Override
    public void onProductClick(int position, String category) {
        // Get the clicked product from the list
        Product clickedProduct = filteredProductsList.get(position);

        // Create an Intent to navigate to the ProductDetailsBuyerActivity
        Intent intent = new Intent(this, ProductDetailsBuyerActivity.class);

        // Pass the clicked product to the next activity
        intent.putExtra("PRODUCT", clickedProduct);

        // Start the ProductDetailsBuyerActivity
        startActivity(intent);
    }
}

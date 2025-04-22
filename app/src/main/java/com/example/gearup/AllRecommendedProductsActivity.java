package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AllRecommendedProductsActivity extends AppCompatActivity implements SeeAllProductAdapter.OnProductClickListener {

    private RecyclerView recyclerView;
    private SeeAllProductAdapter productAdapter;
    private TextView labelRecommendedProducts;
    private ArrayList<Product> recommendedProductsList;
    private ArrayList<Product> filteredProductsList;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_recommended_products);

        // Initialize the RecyclerView, TextView, and EditText (search bar)
        recyclerView = findViewById(R.id.recyclerViewAllRecommendedProducts);
        labelRecommendedProducts = findViewById(R.id.label_recommended_products);
        searchEditText = findViewById(R.id.et_search);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        // Get the list of recommended products passed from the previous activity
        recommendedProductsList = getIntent().getParcelableArrayListExtra("RECOMMENDED_PRODUCTS");

        if (recommendedProductsList == null || recommendedProductsList.isEmpty()) {
            labelRecommendedProducts.setVisibility(View.GONE);
        } else {
            // Initialize the filtered list (starts with all products)
            filteredProductsList = new ArrayList<>(recommendedProductsList);

            // Set up RecyclerView with GridLayoutManager for 2 columns
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 columns
            recyclerView.setLayoutManager(gridLayoutManager);

            // Set up the adapter with the filtered list
            productAdapter = new SeeAllProductAdapter(filteredProductsList, this);
            recyclerView.setAdapter(productAdapter);
        }

        // Set up a text change listener on the search EditText to filter products
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Filter products when the text changes
                filterProducts(charSequence.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {
                // No action needed
            }
        });
    }

    // Filter the products based on the search query
    private void filterProducts(String query) {
        if (query.isEmpty()) {
            // If the search query is empty, show all products
            filteredProductsList.clear();
            filteredProductsList.addAll(recommendedProductsList);
        } else {
            // Otherwise, filter products by name (you can add more criteria like category, brand, etc.)
            filteredProductsList = (ArrayList<Product>) recommendedProductsList.stream()
                    .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Notify the adapter that the data has changed
        productAdapter.updateProducts(filteredProductsList);
    }

    @Override
    public void onProductClick(int position, String category) {
        // Handle product click
        Product clickedProduct = filteredProductsList.get(position);

        // Log the product category (optional)
        String productCategory = clickedProduct.getCategory();
        System.out.println("Product clicked: " + productCategory);

        // Open product details activity
        Intent intent = new Intent(this, ProductDetailsBuyerActivity.class);
        intent.putExtra("PRODUCT", clickedProduct);
        startActivity(intent);
    }
}

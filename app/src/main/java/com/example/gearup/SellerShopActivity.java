package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SellerShopActivity extends AppCompatActivity implements SellerShopAdapter.OnProductClickListener {
    private TextView shopNameTextView;
    private RecyclerView productsRecyclerView;
    private SellerShopAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;
    private String sellerId;
    private Spinner categorySpinner;  // Spinner for selecting categories

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_shop);

        // Initialize UI components
        shopNameTextView = findViewById(R.id.tv_shop_name);
        productsRecyclerView = findViewById(R.id.rv_products);
        categorySpinner = findViewById(R.id.spinner_categories);  // Spinner for category selection

        // Set GridLayoutManager with 2 columns for the RecyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        productsRecyclerView.setLayoutManager(gridLayoutManager);

        // Initialize the adapter with the product list and a click listener
        productAdapter = new SellerShopAdapter(productList, this);  // Pass 'this' for the listener
        productsRecyclerView.setAdapter(productAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve the sellerId passed from the previous activity
        sellerId = getIntent().getStringExtra("SELLER_ID");

        if (sellerId != null && !sellerId.isEmpty()) {
            // Load seller information and products from Firestore
            loadSellerInfo(sellerId);
            loadSellerProducts(sellerId, "All");  // Initially load all products
        } else {
            Toast.makeText(this, "Seller ID not provided", Toast.LENGTH_SHORT).show();
            finish();  // Close the activity if the seller ID is not available
        }

        // Set up the category Spinner
        setupCategorySpinner();
    }

    private void setupCategorySpinner() {
        // Create a list of categories
        String[] categories = {"All", "Central Components", "Body", "Connectors", "Peripherals"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        categorySpinner.setAdapter(adapter);

        // Set up an item selected listener to filter products based on the selected category
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCategory = parentView.getItemAtPosition(position).toString();
                loadSellerProducts(sellerId, selectedCategory);  // Load products based on selected category
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                loadSellerProducts(sellerId, "All");  // If nothing is selected, load all products
            }
        });
    }

    // Method to load the seller information (e.g., shop name)
    private void loadSellerInfo(String sellerId) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String shopName = documentSnapshot.getString("shopName");
                            if (shopName != null && !shopName.isEmpty()) {
                                shopNameTextView.setText(shopName);  // Set the shop name in the TextView
                            } else {
                                Toast.makeText(SellerShopActivity.this, "Shop name not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SellerShopActivity.this, "Shop not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SellerShopActivity.this, "Error getting shop info", Toast.LENGTH_SHORT).show();
                    Log.e("SellerShopActivity", "Error getting shop info", e);
                });
    }

    // Method to load the seller's products from Firestore based on selected category
    private void loadSellerProducts(String sellerId, String category) {
        // If "All" is selected, query for all products, otherwise filter by category
        if (category.equals("All")) {
            db.collection("users").document(sellerId)
                    .collection("products")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        productList.clear();  // Clear the previous product list
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                product.setId(document.getId()); // Set Firestore document ID
                                productList.add(product);  // Add the product to the list
                            }
                        }
                        productAdapter.notifyDataSetChanged();  // Notify the adapter to refresh the RecyclerView
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SellerShopActivity.this, "Error getting products", Toast.LENGTH_SHORT).show();
                        Log.e("SellerShopActivity", "Error getting products", e);
                    });
        } else {
            db.collection("users").document(sellerId)
                    .collection("products")
                    .whereEqualTo("category", category)  // Filter products by selected category
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        productList.clear();  // Clear the previous product list
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                product.setId(document.getId()); // Set Firestore document ID
                                productList.add(product);  // Add the product to the list
                            }
                        }
                        productAdapter.notifyDataSetChanged();  // Notify the adapter to refresh the RecyclerView
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SellerShopActivity.this, "Error getting products", Toast.LENGTH_SHORT).show();
                        Log.e("SellerShopActivity", "Error getting products", e);
                    });
        }
    }

    @Override
    public void onProductClick(Product product) {
        // Handle product click: open ProductDetailsBuyer activity
        if (product != null) {
            Intent intent = new Intent(SellerShopActivity.this, ProductDetailsBuyerFragment.class);
            intent.putExtra("PRODUCT", product);  // Pass the selected product to the next activity
            startActivity(intent);  // Start the ProductDetailsBuyer activity
        }
    }
}

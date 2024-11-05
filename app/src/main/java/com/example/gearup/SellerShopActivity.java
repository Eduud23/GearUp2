package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
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

public class SellerShopActivity extends AppCompatActivity {
    private TextView shopNameTextView;
    private RecyclerView productsRecyclerView;
    private SellerShopAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;
    private String sellerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_shop);

        // Initialize UI components
        shopNameTextView = findViewById(R.id.tv_shop_name);
        productsRecyclerView = findViewById(R.id.rv_products);

        // Set GridLayoutManager with 2 columns for the RecyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        productsRecyclerView.setLayoutManager(gridLayoutManager);

        // Initialize the adapter with the product list and a click listener
        productAdapter = new SellerShopAdapter(productList, new SellerShopAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(int position, Product product) {
                // Handle product click event
                if (product != null) {
                    Log.d("SellerShopActivity", "Product clicked: " + product.getName());
                    Intent intent = new Intent(SellerShopActivity.this, ProductDetailsBuyer.class);
                    intent.putExtra("PRODUCT", product);  // Pass the selected product to ProductDetailsBuyer activity
                    startActivity(intent);  // Start the ProductDetailsBuyer activity
                } else {
                    Log.e("SellerShopActivity", "Product data is null");
                    Toast.makeText(SellerShopActivity.this, "Product data is missing", Toast.LENGTH_SHORT).show();
                }
            }
        });
        productsRecyclerView.setAdapter(productAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve the sellerId passed from the previous activity
        sellerId = getIntent().getStringExtra("SELLER_ID");

        if (sellerId != null && !sellerId.isEmpty()) {
            // Load seller information and products from Firestore
            loadSellerInfo(sellerId);
            loadSellerProducts(sellerId);
        } else {
            Toast.makeText(this, "Seller ID not provided", Toast.LENGTH_SHORT).show();
            finish();  // Close the activity if the seller ID is not available
        }
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

    // Method to load the seller's products from Firestore
    private void loadSellerProducts(String sellerId) {
        db.collection("users").document(sellerId)
                .collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();  // Clear the previous product list
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            productList.add(product);  // Add the product to the list
                        }
                    }
                    Log.d("SellerShopActivity", "Loaded " + productList.size() + " products");
                    productAdapter.notifyDataSetChanged();  // Notify the adapter to refresh the RecyclerView
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SellerShopActivity.this, "Error getting products", Toast.LENGTH_SHORT).show();
                    Log.e("SellerShopActivity", "Error getting products", e);
                });
    }
}

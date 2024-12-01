package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class PeripheralsActivity extends AppCompatActivity implements SeeAllProductAdapter.OnProductClickListener {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private SeeAllProductAdapter adapter; // Adapter for displaying products
    private List<Product> productsList = new ArrayList<>(); // List for holding products

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peripherals); // Ensure layout name matches

        recyclerView = findViewById(R.id.recycler_view_peripherals); // Ensure ID is correct

        // Set GridLayoutManager with 2 columns
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        db = FirebaseFirestore.getInstance();

        // First, check if PRODUCTS_LIST exists in the Intent
        if (getIntent() != null && getIntent().hasExtra("PRODUCT_LIST")) {
            productsList = getIntent().getParcelableArrayListExtra("PRODUCT_LIST");
            if (productsList != null && !productsList.isEmpty()) {
                setAdapter(); // Set the adapter if products are passed
            } else {
                loadProducts(); // If the list is empty, load products from Firestore
            }
        } else {
            loadProducts(); // If no PRODUCT_LIST exists, load products from Firestore
        }
    }

    private void loadProducts() {
        // Firestore query to get products
        db.collectionGroup("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productsList.clear(); // Clear the list to avoid duplicates
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            // Filter products by "Peripherals" category
                            if (product != null && "Peripherals".equals(product.getCategory())) {
                                productsList.add(product); // Add the product to the list
                            }
                        }

                        // Log the number of products loaded
                        Log.d("PeripheralsActivity", "Products loaded: " + productsList.size());

                        // After loading the data, set the adapter
                        setAdapter();
                    } else {
                        // Handle the error if the Firestore query fails
                        Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                        Log.e("PeripheralsActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void setAdapter() {
        // Create the adapter with the product list and set it on the RecyclerView
        adapter = new SeeAllProductAdapter(productsList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onProductClick(int position, String category) {
        // Get the clicked product from the list
        Product clickedProduct = productsList.get(position);

        // Create an Intent to navigate to ProductDetailsBuyerActivity
        Intent intent = new Intent(this, ProductDetailsBuyerActivity.class);

        // Pass the clicked product to the next activity
        intent.putExtra("PRODUCT", clickedProduct);

        // Start the ProductDetailsBuyerActivity
        startActivity(intent);
    }
}

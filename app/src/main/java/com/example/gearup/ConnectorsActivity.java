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

public class ConnectorsActivity extends AppCompatActivity implements SeeAllProductAdapter.OnProductClickListener {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private SeeAllProductAdapter adapter; // Adapter for displaying products
    private List<Product> productsList = new ArrayList<>(); // List for holding products

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectors); // Make sure the layout is correct

        recyclerView = findViewById(R.id.recycler_view_connectors);
        // Set GridLayoutManager with 2 columns
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        db = FirebaseFirestore.getInstance();

        // Check if products are passed from another activity (e.g., HomeFragmentBuyer)
        if (getIntent() != null && getIntent().hasExtra("PRODUCT_LIST")) {
            // Retrieve the passed product list
            productsList = getIntent().getParcelableArrayListExtra("PRODUCT_LIST");
            if (productsList != null && !productsList.isEmpty()) {
                // Set the adapter immediately if products are passed
                setAdapter();
            } else {
                // If the list is empty, load products from Firestore
                loadProducts();
            }
        } else {
            // If no list is passed, load products from Firestore
            loadProducts();
        }
    }

    // Load products from Firestore that belong to the "Connectors" category
    private void loadProducts() {
        db.collectionGroup("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productsList.clear(); // Clear previous data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            // Filter products by "Connectors" category
                            if (product != null && "Connectors".equals(product.getCategory())) {
                                productsList.add(product); // Add the product to the list
                            }
                        }
                        // After loading products, set the adapter
                        setAdapter();
                    } else {
                        Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                        Log.e("ConnectorsActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    // Set the adapter for the RecyclerView after products are loaded
    private void setAdapter() {
        // Only set the adapter if the products list is not empty
        if (!productsList.isEmpty()) {
            adapter = new SeeAllProductAdapter(productsList, this); // Create the adapter
            recyclerView.setAdapter(adapter); // Set the adapter to the RecyclerView
        } else {
            Toast.makeText(this, "No products found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProductClick(int position, String category) {
        // Get the clicked product from the list
        Product clickedProduct = productsList.get(position);

        // Create an Intent to navigate to the ProductDetailsBuyerActivity
        Intent intent = new Intent(this, ProductDetailsBuyerActivity.class);

        // Pass the clicked product to the next activity
        intent.putExtra("PRODUCT", clickedProduct);

        // Start the ProductDetailsBuyerActivity
        startActivity(intent);
    }
}

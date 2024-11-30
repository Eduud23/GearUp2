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

public class CentralComponentsActivity extends AppCompatActivity implements SeeAllProductAdapter.OnProductClickListener {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private SeeAllProductAdapter adapter; // Adapter for displaying products
    private List<Product> productsList = new ArrayList<>(); // List for holding products

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central_components);

        recyclerView = findViewById(R.id.recycler_view_central_components);

        // Set GridLayoutManager with 2 columns
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        db = FirebaseFirestore.getInstance();

        // Check if products are passed from another activity (e.g., HomeFragmentBuyer)
        if (getIntent() != null && getIntent().hasExtra("PRODUCT_LIST")) {
            // Retrieve the passed product list
            productsList = getIntent().getParcelableArrayListExtra("PRODUCT_LIST");
            if (productsList != null && !productsList.isEmpty()) {
                setAdapter(); // Set the adapter if products are passed
            } else {
                loadProducts(); // Load products from Firestore if no list is passed
            }
        } else {
            loadProducts(); // Load products from Firestore if no list is passed
        }
    }

    // Load products from Firestore that belong to "Central Components" category
    private void loadProducts() {
        db.collectionGroup("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productsList.clear(); // Clear previous data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            if (product != null && "Central Components".equals(product.getCategory())) {
                                productsList.add(product); // Add matching products to the list
                            }
                        }
                        setAdapter(); // Set the adapter after filtering products
                    } else {
                        Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                        // Log error for debugging
                        Log.e("CentralComponentsActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    // Set the adapter for the RecyclerView
    private void setAdapter() {
        adapter = new SeeAllProductAdapter(productsList, this); // Create adapter
        recyclerView.setAdapter(adapter); // Set adapter to RecyclerView
    }

    @Override
    public void onProductClick(int position, String category) {
        Product clickedProduct = productsList.get(position); // Get the clicked product

        // Create an Intent to navigate to ProductDetailsBuyerActivity
        Intent intent = new Intent(this, ProductDetailsBuyerActivity.class);

        // Pass the clicked product to the next activity
        intent.putExtra("PRODUCT", clickedProduct);

        // Start ProductDetailsBuyerActivity
        startActivity(intent);
    }
}

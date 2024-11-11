package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
        // Set GridLayoutManager with 3 columns
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        db = FirebaseFirestore.getInstance();
        loadProducts(); // Load only "Central Components" products
    }

    private void loadProducts() {
        db.collectionGroup("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            // Filter products by "Central Components" category
                            if (product != null && "Central Components".equals(product.getCategory())) {
                                productsList.add(product);
                            }
                        }
                        setAdapter(); // Set the adapter after filtering
                    } else {
                        Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                        // Log the error for debugging
                        Log.e("CentralComponentsActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void setAdapter() {
        adapter = new SeeAllProductAdapter(productsList, this); // Use SeeAllProductAdapter
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onProductClick(int position, String category) {
        Product clickedProduct = productsList.get(position);
        Intent intent = new Intent(this, ProductDetailsBuyerFragment.class);
        intent.putExtra("PRODUCT", clickedProduct);
        startActivity(intent);
    }
}

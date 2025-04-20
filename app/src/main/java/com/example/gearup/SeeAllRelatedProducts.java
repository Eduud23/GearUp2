package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SeeAllRelatedProducts extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private RelatedProductsAdapter adapter;
    private List<Product> relatedProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_related_products);

        recyclerView = findViewById(R.id.recycler_related_products);
        db = FirebaseFirestore.getInstance();

        String category = getIntent().getStringExtra("category");
        String currentProductId = getIntent().getStringExtra("productId");

        fetchRelatedProducts(category, currentProductId);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void fetchRelatedProducts(String category, String currentProductId) {
        if (category == null || category.isEmpty()) return;

        db.collectionGroup("products")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String productId = doc.getId();
                        if (!productId.equals(currentProductId)) {
                            Product product = doc.toObject(Product.class);
                            product.setId(productId);
                            relatedProducts.add(product);
                        }
                    }
                    setupRecyclerView();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load related products", Toast.LENGTH_SHORT).show();
                    Log.e("SeeAllRelatedProducts", "Error", e);
                });
    }

    private void setupRecyclerView() {
        adapter = new RelatedProductsAdapter(relatedProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }
}

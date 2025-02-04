package com.example.gearup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private RecyclerView recyclerSearchResults;
    private SearchProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchInput = findViewById(R.id.search_input);
        recyclerSearchResults = findViewById(R.id.recycler_search_results);

        // Set the GridLayoutManager with 2 columns for the RecyclerView
        recyclerSearchResults.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize adapter with an empty list (no products initially)
        adapter = new SearchProductAdapter(new ArrayList<>(), "Search", product -> {
            // Handle product click (optional)
        });
        recyclerSearchResults.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadProducts();

        searchInput.requestFocus();

        filterProducts("");

        // Search functionality with text watcher
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    filterProducts(s.toString().trim());
                } else {
                    adapter.updateProductList(new ArrayList<>());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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
            }
        });
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        String[] keywords = query.toLowerCase().split("\\s+");

        for (Product product : productList) {
            int relevanceScore = calculateRelevance(product, keywords);
            if (relevanceScore > 0) {
                filteredList.add(product);
            }
        }

        filteredList.sort((p1, p2) -> Integer.compare(calculateRelevance(p2, keywords), calculateRelevance(p1, keywords)));

        adapter.updateProductList(filteredList);
    }

    private int calculateRelevance(Product product, String[] keywords) {
        int score = 0;
        String name = product.getName();
        String description = product.getDescription();

        if (name != null) {
            name = name.toLowerCase();
        }

        if (description != null) {
            description = description.toLowerCase();
        }

        for (String keyword : keywords) {
            if (name != null && name.contains(keyword)) score += 3;
            if (description != null && description.contains(keyword)) score += 1;
        }

        return score;
    }
}
